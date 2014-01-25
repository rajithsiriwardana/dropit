package com.anghiari.dropit.fileserver.impl;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.fileserver.FileServerNode;
import com.anghiari.dropit.operations.ResGetFilenodeOperation;

public class RingSetupHandler extends SimpleChannelHandler {
	private FileServerNodeImpl handledNode;

	public RingSetupHandler(FileServerNodeImpl node) {
		handledNode = node;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		DropItPacket pkt = (DropItPacket) e.getMessage();
		String method = pkt.getMethod();
		
		System.out.println(method);
		
		//Respond from Server Node in the ring to the new node
		if (Constants.RES_SUSC.toString().equalsIgnoreCase(method)) {
			System.out.println("Respond recieved for FIND_SUSC");
			DropItPacket packet = new DropItPacket(
					Constants.REQ_JOIN_NODE.toString());
			FileNode node = (FileNode) pkt.getAttribute(Constants.INET_ADDRESS
					.toString());
			System.out.println(node.getIp());
			packet.setAttribute(Constants.INET_ADDRESS.toString(), node);
			//handledNode.sendMessage(packet, node, this);
			
		} else if (Constants.RES_JOIN_NODE.toString().equalsIgnoreCase(method)) {
			DropItPacket packet = new DropItPacket(
					Constants.REQ_JOIN_FINAL.toString());

		} else if (Constants.ACK_PREDECESSOR.toString()
				.equalsIgnoreCase(method)) {

		} else if (Constants.ACK_SUCCESSOR.toString().equalsIgnoreCase(method)) {

		}else if (Constants.RES_GET_FILENODE.toString().equalsIgnoreCase(
				method)) {
			// call the response method for node position request here.
			new ResGetFilenodeOperation(pkt, (FileServerNode)handledNode,this).sendRequest();
		}
		super.messageReceived(ctx, e);
	}
}