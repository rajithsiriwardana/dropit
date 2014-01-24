package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.KeyId;
import com.anghiari.dropit.requestserver.service.DHTUtils;
import com.anghiari.dropit.requestserver.service.NodeFactory;
import com.anghiari.dropit.requestserver.service.ServerClient;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author madhawa
 * @author rajith
 */
@ChannelPipelineCoverage("one")
public class ObjectHandler extends SimpleChannelHandler {
    private ArrayList<String> activeFilesList;

    private static final Logger logger = Logger.getLogger(ObjectHandler.class
            .getName());

    /**
     * Channel Group
     */
    private ChannelGroup channelGroup = null;

    /**
     * Constructor
     *
     * @param channelGroup
     */
    public ObjectHandler(ChannelGroup channelGroup, ArrayList<String> activeFilesList) {
        this.channelGroup = channelGroup;
        this.activeFilesList = activeFilesList;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        channelGroup.add(ctx.getChannel());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        DropItPacket pptmp = (DropItPacket) e.getMessage();
        if (pptmp != null) {
            if (Constants.PUT.toString().equalsIgnoreCase(pptmp.getMethod())) {
                KeyId id = DHTUtils
                        .generateKeyId((String) pptmp.getAttribute(Constants.FILE_NAME.toString()));
                ServerClient client = new ServerClient();
                sendResponse(ctx, e, client.sendHash(id));
                //add file name to the list
                this.activeFilesList.add(String.valueOf(pptmp.getAttribute(Constants.FILE_NAME.toString())));
            } else if (Constants.GOSSIP.toString().equalsIgnoreCase(pptmp.getMethod())) {
                System.out.println("GOSSIP received");
                //get the list from the gossip msg
                ArrayList<String> receivedList = (ArrayList<String>) pptmp.getAttribute(Constants.GOS_LIST.toString());
                System.out.println("Received " + receivedList.get(0) + " size" + receivedList.size());
                //merge two lists
                for (String fileName : receivedList) {
                    if (!this.activeFilesList.contains(fileName)) {
                        this.activeFilesList.add(fileName);
                    }
                }
//                for(String file : this.activeFilesList){
//                    System.out.println(">>>My list: " + file);
//                }
            } else if (Constants.GET_FILENODE.toString().equalsIgnoreCase(pptmp.getMethod())) {
                DropItPacket packet = new DropItPacket(Constants.RES_GET_FILENODE.toString());
                packet.setAttribute(Constants.INET_ADDRESS.toString(), NodeFactory.getNode());
                sendResponse(ctx, e, packet);
            } else {
                super.messageReceived(ctx, e);
            }
        }
    }

    public void sendResponse(ChannelHandlerContext ctx, MessageEvent e, DropItPacket packet) {
        Channel channel = e.getChannel();
        ChannelFuture channelFuture = Channels.future(e.getChannel());
        ChannelEvent responseEvent =
                new DownstreamMessageEvent(channel, channelFuture, packet, channel.getRemoteAddress());
        ctx.sendDownstream(responseEvent);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(Level.WARNING, "Unexpected exception from downstream.", e
                .getCause());
        Channels.close(e.getChannel());
    }
}