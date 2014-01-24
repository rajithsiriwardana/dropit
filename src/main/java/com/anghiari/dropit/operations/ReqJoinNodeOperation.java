package com.anghiari.dropit.operations;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.fileserver.FileServerNode;
import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;

/**
 * This operation is when a new node is trying to connect by a request to the
 * node pointed by the request server.
 * 
 * @author Sanka
 * 
 */
public class ReqJoinNodeOperation extends AbstractOperation {
	private DropItPacket responsePacket;
	private FileNode node;
	private RingSetupHandler setupHandler;

	public ReqJoinNodeOperation(DropItPacket pkt) {
		responsePacket = new DropItPacket(Constants.RES_JOIN_NODE.toString());
		node = (FileNode) pkt.getAttribute(Constants.INET_ADDRESS.toString());
		setupHandler = new RingSetupHandler(this.fileServer);
	}

	private void setupNodeInRing() {

	}

	@Override
	public void sendResponse() {
		this.fileServer.sendMessage(responsePacket, node, setupHandler);
	}
}

class RingSetupHandler extends SimpleChannelHandler {
	private FileServerNodeImpl handledNode;

	public RingSetupHandler(final FileServerNode nodeImpl) {
		this.handledNode = (FileServerNodeImpl) nodeImpl;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		DropItPacket pkt = (DropItPacket) e.getMessage();
		String method = pkt.getMethod();

		if (Constants.RES_SUSC.toString().equalsIgnoreCase(method)) {
			DropItPacket packet = new DropItPacket(
					Constants.REQ_JOIN_NODE.toString());
			FileNode node = (FileNode) pkt.getAttribute(Constants.INET_ADDRESS
					.toString());
			handledNode.sendMessage(packet, node, this);
		} else if (Constants.RES_JOIN_NODE.toString().equalsIgnoreCase(method)) {

		}
		super.messageReceived(ctx, e);
	}
}