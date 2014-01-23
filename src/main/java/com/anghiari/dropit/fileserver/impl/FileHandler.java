package com.anghiari.dropit.fileserver.impl;


import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.operations.PongOperation;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * User: amila
 * @author chinthaka316
 */

public class FileHandler extends SimpleChannelHandler {
	
    public void messageReceived(ChannelHandlerContext ctx,MessageEvent e) throws Exception {
        DropItPacket pkt = (DropItPacket)e.getMessage();
        //TODO: Use data in pkt and send to required file
        
        String method =pkt.getMethod();
        
        System.out.println("method "+method);
       
        if(Constants.PING.toString().equalsIgnoreCase(method)){
        	System.out.println("came here "+method );
            PongOperation pongOperation=new PongOperation(ctx,e);
            pongOperation.sendResponse();
        	//respondToPing(ctx,e);
        }
        else if(Constants.PONG.toString().equalsIgnoreCase(method)){
        	
        	System.out.println("PONG received");
        }	
        
        super.messageReceived(ctx, e);
    }
    
    
    /**
     * Respond to the PING request
//     */
//    public void respondToPing(ChannelHandlerContext ctx,MessageEvent e){
//
//    	DropItPacket dropPkt = new DropItPacket(Constants.PONG.toString());
//    	 // Send back the reponse
//    	  Channel channel = e.getChannel();
//    	  ChannelFuture channelFuture = Channels.future(e.getChannel());
//    	  ChannelEvent responseEvent = new DownstreamMessageEvent(channel, channelFuture, dropPkt, channel.getRemoteAddress());
//    	  ctx.sendDownstream(responseEvent);
//
//    }

}

