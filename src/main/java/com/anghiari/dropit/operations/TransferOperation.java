package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * @author: sunimal
 */
public class TransferOperation extends AbstractOperation {
    public TransferOperation(ChannelHandlerContext channelHandlerContext, MessageEvent evt) {
        packet = new DropItPacket(Constants.TRANSFER.toString());
        ctx = channelHandlerContext;
        e = evt;
    }
}
