package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.KeyId;
import com.anghiari.dropit.operations.PongOperation;
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
            if (Constants.PUT.toString().equalsIgnoreCase(pptmp.getMethod()) ||
                    Constants.GET.toString().equalsIgnoreCase(pptmp.getMethod())) {
                System.out.println(pptmp.getMethod() + " received!");

                KeyId id = DHTUtils
                        .generateKeyId((String) pptmp.getAttribute(Constants.FILE_NAME.toString()));
                ServerClient client = new ServerClient();
                //TODO: handle sendHash method
                sendResponse(ctx, e, client.sendHash(id, Constants.valueOf(pptmp.getMethod()),
                        (String) pptmp.getAttribute(Constants.FILE_PATH.toString()),
                        (String) pptmp.getAttribute(Constants.FILE_NAME.toString())));
                //add file name to the list only if it's a PUT method
/*                if (Constants.PUT.toString().equalsIgnoreCase(pptmp.getMethod())) {
                    this.activeFilesList.add(String.valueOf(pptmp.getAttribute(Constants.FILE_NAME.toString())));
                }*/
            } else if (Constants.GOSSIP.toString().equalsIgnoreCase(pptmp.getMethod())) {
                System.out.println(pptmp.getMethod() + " received!");

                //get the list from the gossip msg
                ArrayList<String> receivedList = (ArrayList<String>) pptmp.getAttribute(Constants.GOS_LIST.toString());
                //merge two lists
                for (String fileName : receivedList) {
                    if (!this.activeFilesList.contains(fileName)) {
                        this.activeFilesList.add(fileName);
                    }
                }
                System.out.println("Received size: " + receivedList.size() + " | Size after gossiping: " + activeFilesList.size());

            } else if (Constants.SEARCH.toString().equalsIgnoreCase(pptmp.getMethod())) {
                System.out.println(pptmp.getMethod() + " received!");
                String keyWord = (String) pptmp.getAttribute(Constants.FILE_NAME.toString());
                ArrayList<String> searchRes = new ArrayList<String>();
                //go through the active list and add to the search result if it contains the key word
                for (String fileName : activeFilesList) {
                    if (fileName.toLowerCase().contains(keyWord.toLowerCase())) {
                        searchRes.add(fileName);
                    }
                }
                DropItPacket packet = new DropItPacket(Constants.RES_SEARCH.toString());
                packet.setAttribute(Constants.SEARCH_RESULTS.toString(), searchRes);
                sendResponse(ctx, e, packet);
            } else if (Constants.ACK_FILE_SAVED.toString().equalsIgnoreCase(pptmp.getMethod())) {
                this.activeFilesList.add(String.valueOf(pptmp.getAttribute(Constants.FILE_NAME.toString())));
            } else if (Constants.GET_FILENODE.toString().equalsIgnoreCase(pptmp.getMethod())) {
                System.out.println(pptmp.getMethod() + " received!");

                DropItPacket packet = new DropItPacket(Constants.RES_GET_FILENODE.toString());
                packet.setAttribute(Constants.INET_ADDRESS.toString(), NodeFactory.getNode());
                sendResponse(ctx, e, packet);
            } else if (Constants.PING.toString().equalsIgnoreCase(pptmp.getMethod())) {
                System.out.println(pptmp.getMethod() + " received!");

                PongOperation pongOperation = new PongOperation(ctx, e);
                pongOperation.sendResponse();
            } else {
                System.out.println(pptmp.getMethod() + " received! Yet no reply!");
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