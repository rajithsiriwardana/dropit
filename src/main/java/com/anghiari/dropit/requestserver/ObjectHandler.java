package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;

import java.util.logging.Level;
import java.util.logging.Logger;
  
/**
 * @author madhawa
 * @author rajith
 * 
 */  
@ChannelPipelineCoverage("one")
public class ObjectHandler extends SimpleChannelHandler {
  
    private static final Logger logger = Logger.getLogger(ObjectHandler.class
            .getName());

    private DropItPacket dropItPacket;

    /**
     * Channel Group
     */
    private ChannelGroup channelGroup = null;

    /**
     * Constructor
     * @param channelGroup
     */
    public ObjectHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
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
            if(Constants.PUT.toString().equalsIgnoreCase(pptmp.getMethod())){

            } else {
                super.messageReceived(ctx, e);
            }
        }  
    }  
  
    @Override  
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {  
        logger.log(Level.WARNING, "Unexpected exception from downstream.", e  
                .getCause());  
        System.out.println(e.toString());
        Channels.close(e.getChannel());  
    }
}