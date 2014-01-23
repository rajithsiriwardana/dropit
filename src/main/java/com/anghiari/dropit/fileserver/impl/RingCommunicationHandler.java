package com.anghiari.dropit.fileserver.impl;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.operations.PongOperation;

public class RingCommunicationHandler extends SimpleChannelHandler {


	public void messageReceived(ChannelHandlerContext ctx,MessageEvent e) throws Exception {
        DropItPacket pkt = (DropItPacket)e.getMessage();
        String method =pkt.getMethod();
        
        System.out.println("RingCommunicationHandler came here "+method );
        
        if(Constants.PING.toString().equalsIgnoreCase(method)){
        	System.out.println("RingCommunicationHandler came here "+method );
            PongOperation pongOperation=new PongOperation(ctx,e);
            pongOperation.sendResponse();
        	//respondToPing(ctx,e);
        }
        super.messageReceived(ctx, e);
    }
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
    		throws Exception {
    	
    	//handle successor
    	System.out.println("Connection exception, server might be down");
    	Channels.close(e.getChannel());
    }
    
    
}
