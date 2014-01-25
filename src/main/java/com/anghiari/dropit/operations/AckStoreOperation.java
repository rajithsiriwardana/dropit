package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * @author: sunimal
 */
public class AckStoreOperation extends AbstractOperation{
    public AckStoreOperation(ChannelHandlerContext channelHandlerContext, MessageEvent evt, String fileName) {
        // Send back the reponse
        packet=new DropItPacket(Constants.ACK_STORE.toString());
        packet.setAttribute(Constants.FILE_NAME.toString(), fileName);
        ctx=channelHandlerContext;
        e=evt;
    }
}
