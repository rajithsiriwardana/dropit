package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * @author: sunimal
 */
public class TransferOperation extends AbstractOperation {
    public TransferOperation(ChannelHandlerContext channelHandlerContext, MessageEvent evt, byte[] filedata, String filename) {
        packet = new DropItPacket(Constants.TRANSFER.toString());
        packet.setAttribute(Constants.FILE_NAME.toString(), filename);
        if(filedata!=null){
            packet.setData(filedata);
            packet.setAttribute(Constants.FILE_EXISTS.toString(), true);
        } else {
            packet.setAttribute(Constants.FILE_EXISTS.toString(), false);
        }
        ctx = channelHandlerContext;
        e = evt;
    }
}
