package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.commons.KeyId;
import com.anghiari.dropit.fileserver.FileServerNode;
import org.jboss.netty.channel.*;

/**
 * User: amila
 */
public class FindSuccessorOperation extends AbstractOperation{

    public FindSuccessorOperation(ChannelHandlerContext channelHandlerContext,MessageEvent evt, DropItPacket incomingPacket) {
        ctx=channelHandlerContext;
        e=evt;
        packet = incomingPacket;
    }

    public void sendResponse(){

        KeyId keyId = (KeyId)packet.getAttribute(Constants.KEY_ID.toString());
        System.out.println(">>>>>>>>>>>>>>>>>>>FINDING SUCCESSOR FOR KEY: " + keyId.getHashId() +" <<<<<<<<<<<<<<<<<<<<<<");
        FileNode node = this.fileServer.findSuccessor(keyId);
        System.out.println(">>>>>>>>>>>>>>>>>>>FOUND SUCCESSOR FOR KEY: " + keyId.getHashId() + "NODE:" + node.getPort_ring()+ " KEY: "+ node.getKey().getHashId() +" <<<<<<<<<<<<<<<<<<<<<<");
        DropItPacket outPacket = new DropItPacket(Constants.RES_SUSC.toString());
        // Send out a dropit Packet
        Channel channel = e.getChannel();
        ChannelFuture channelFuture = Channels.future(e.getChannel());
        ChannelEvent responseEvent = new DownstreamMessageEvent(channel, channelFuture, outPacket, channel.getRemoteAddress());
        ctx.sendDownstream(responseEvent);

    }

}