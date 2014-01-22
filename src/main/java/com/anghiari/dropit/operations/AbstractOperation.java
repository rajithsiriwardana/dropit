package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.fileserver.FileServerNode;
import org.jboss.netty.channel.*;

/**
 * @author: sunimal
 */
public abstract class AbstractOperation {

    FileServerNode fileServer;
    String ip;
    int port;
    DropItPacket packet;
    ChannelHandlerContext ctx;
    MessageEvent e;

    //Sending responses to incoming requests
    public void sendResponse(){

        //DropItPacket dropPkt = new DropItPacket(Constants.PONG.toString());
        // Send out a dropit Packet
        Channel channel = e.getChannel();
        ChannelFuture channelFuture = Channels.future(e.getChannel());
        ChannelEvent responseEvent = new DownstreamMessageEvent(channel, channelFuture, packet, channel.getRemoteAddress());
        ctx.sendDownstream(responseEvent);

    }

    //Sending newly initiated messages
    public void sendRequest(){
        fileServer.sendMessage(this.packet,new FileNode(ip,port));
    }
}
