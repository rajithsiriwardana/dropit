package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.operations.FindSuccessorOperation;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.fileserver.FileServerNode;
import com.anghiari.dropit.operations.PongOperation;

/**
 * 
 * @author chinthaka316
 *
 */

public class RingCommunicationHandler extends SimpleChannelHandler {

	private FileServerNodeImpl handledNode;
	
	public RingCommunicationHandler(FileServerNodeImpl node) {
		this.handledNode = node;
	}

	public void messageReceived(ChannelHandlerContext ctx,MessageEvent e) throws Exception {
        DropItPacket pkt = (DropItPacket)e.getMessage();
        String method =pkt.getMethod();

//        System.out.println("RingCommunicationHandler came here "+method );
        
        if(Constants.PING.toString().equalsIgnoreCase(method)){
//        	System.out.println("RingCommunicationHandler came here "+method );
            PongOperation pongOperation=new PongOperation(ctx,e);
            pongOperation.sendResponse();
        	//respondToPing(ctx,e);
        }else if(Constants.FND_SUSC.toString().equalsIgnoreCase(method)){
            System.out.println("==========CAME TO FING SUCC===========");
            FindSuccessorOperation findOperation = new FindSuccessorOperation(handledNode, ctx, e, pkt);
            findOperation.sendResponse();
        }else if(Constants.RES_SUSC.toString().equalsIgnoreCase(method)){
            System.out.println("==========FIND SUCC REPLY CAME===========");


        }else if(Constants.FND_SUSC_INT.toString().equalsIgnoreCase(method)){

        }else if(Constants.RES_SUSC_INT.toString().equalsIgnoreCase(method)){

        }

        super.messageReceived(ctx, e);
    }
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
    		throws Exception {
    	
    	//handling successor
    	handledNode.getSuccessors().remove(0);
//    	System.out.println("Reset the successor list, new size is" + handledNode.getSuccessors().size());
    	
    	System.out.println("Connection exception, server might be down");
    	Channels.close(e.getChannel());
    }
    
    
}
