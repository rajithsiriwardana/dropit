package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.requestserver.service.ServerClient;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gayashan
 */
public class FSResponseHandler extends SimpleChannelHandler {
    private ObjectHandler objectHandler;
    private static final Logger logger = Logger.getLogger(ObjectHandler.class
            .getName());
    private ChannelGroup channelGroup = null;

    public FSResponseHandler(ChannelGroup channelGroup, ObjectHandler objectHandler) {
        this.channelGroup = channelGroup;
        this.objectHandler = objectHandler;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        channelGroup.add(ctx.getChannel());
        System.err.println("Connected to" + e.getChannel().getRemoteAddress());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        DropItPacket pptmp = (DropItPacket) e.getMessage();
        if (pptmp != null) {
            if (Constants.RES_SUSC.toString().equalsIgnoreCase(pptmp.getMethod())) {
                System.out.println(pptmp.getMethod() + " received to FS!");

                FileNode fileNode = (FileNode) pptmp.getAttribute(Constants.FILE_NODE.toString());
                ServerClient serverClient = new ServerClient();
                DropItPacket sendToClient = serverClient.sendHash(((DropItPacket) pptmp.getAttribute(Constants.RECVD_PACKET.toString())).getMethod().toString(),
                        (String) pptmp.getAttribute(Constants.RECVD_PATH.toString()),
                        (String) pptmp.getAttribute(Constants.RECVD_FNAME.toString()),
                        fileNode.getIp(),
                        fileNode.getPort());
                this.objectHandler.sendResponse(sendToClient);
                System.out.println("Response sent to " +
                        sendToClient.getAttribute(Constants.NODE_IP.toString()) + ":" +
                        sendToClient.getAttribute(Constants.NODE_PORT.toString()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(Level.WARNING, "Unexpected exception from downstream.", e
                .getCause());
        Channels.close(e.getChannel());

    }
}
