package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.fileserver.FileServerNode;
import org.jboss.netty.channel.*;

/**
 * @author: sunimal
 */
public class PongOperation extends AbstractOperation {

    public PongOperation(ChannelHandlerContext channelHandlerContext,MessageEvent evt) {
        //DropItPacket dropPkt = new DropItPacket(Constants.PONG.toString());
        // Send back the reponse
        packet=new DropItPacket(Constants.PONG.toString());
        ctx=channelHandlerContext;
        e=evt;
        //Channel channel = e.getChannel();
        //ChannelFuture channelFuture = Channels.future(e.getChannel());
        //ChannelEvent responseEvent = new DownstreamMessageEvent(channel, channelFuture, dropPkt, channel.getRemoteAddress());
        //ctx.sendDownstream(responseEvent);
    }

}
