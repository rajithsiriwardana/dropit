package com.anghiari.dropit.requestserver.utils;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * @author rajith
 * @version ${Revision}
 */
public class ResponseHandler extends SimpleChannelHandler {

    private DropItPacket packet;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        DropItPacket pptmp = (DropItPacket) e.getMessage();
        if (pptmp != null) {
            if (Constants.RES_PUT.toString().equalsIgnoreCase(pptmp.getMethod())) {
                  packet = pptmp;
            } else {
                super.messageReceived(ctx, e);
            }
        }
    }

    public DropItPacket getResponse(){
        return packet;
    }
}
