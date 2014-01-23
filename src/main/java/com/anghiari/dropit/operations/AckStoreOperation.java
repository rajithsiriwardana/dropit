package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * @author: sunimal
 */
public class AckStoreOperation extends AbstractOperation{
    public AckStoreOperation(ChannelHandlerContext channelHandlerContext,MessageEvent evt) {
        // Send back the reponse
        packet=new DropItPacket(Constants.ACK_STORE.toString());
        ctx=channelHandlerContext;
        e=evt;
    }
}
