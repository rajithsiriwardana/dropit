package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * @author: sunimal
 */
public class AckDeleteOperation extends AbstractOperation{

    public AckDeleteOperation(ChannelHandlerContext channelHandlerContext,MessageEvent evt) {
        // Send back the response
        packet=new DropItPacket(Constants.ACK_DELETE.toString());
        ctx=channelHandlerContext;
        e=evt;
    }

}
