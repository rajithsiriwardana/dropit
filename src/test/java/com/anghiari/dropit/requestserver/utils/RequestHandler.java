package com.anghiari.dropit.requestserver.utils;

import com.anghiari.dropit.commons.DropItPacket;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author rajith
 * @version ${Revision}
 */
@ChannelPipelineCoverage("one")  
public class RequestHandler extends SimpleChannelHandler {

    /**
     * Is there any packets to send (at least 1 at starting)
     */  
    private final AtomicInteger isPing = new AtomicInteger(1);

    /** 
     * Return value for the caller 
     */  
    final BlockingQueue<DropItPacket> answer = new LinkedBlockingQueue<DropItPacket>();
  
    /** 
     * Dropit packet
     */  
    DropItPacket pp;
  
    /** 
     * Method to wait for the final PingPong object 
     * @return the final PingPong object 
     */  
    public DropItPacket getResponse() {
        for (;;) {  
            try {  
                return answer.take();  
            } catch (InterruptedException e) {  
                // Ignore.  
            }  
        }  
    }  
  
    /** 
     * Constructor 
     * @param nbMessage 
     * @param size 
     */  
    public RequestHandler(int nbMessage, int size) {
        if (nbMessage < 0) {  
            throw new IllegalArgumentException("nbMessage: " + nbMessage);  
        }  
        pp = new DropItPacket("PUT");
    }  
  
    /** 
     * Add the ObjectXcoder to the Pipeline
     */  
    @Override  
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {  
        e.getChannel().getPipeline().addFirst("decoder", new ObjectDecoder());  
        e.getChannel().getPipeline().addAfter("decoder", "encoder",  
                new ObjectEncoder());  
    }  
  
    /** 
     * Start sending
     */  
    @Override  
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {  
        generatePingTraffic(e);
    }  
  
    @Override
    public void channelInterestChanged(ChannelHandlerContext ctx,  
            ChannelStateEvent e) {
        generatePingTraffic(e);
    }  
  
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {  
        DropItPacket pptmp = (DropItPacket) e.getMessage();
        if (pptmp != null) {  
            pp = pptmp;  
            isPing.incrementAndGet();  
            generatePingTraffic(e);
        }  
    }  
  
    @Override  
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {  
        answer.offer(pp);
        Channels.close(e.getChannel());  
    }  
  
    /** 
     * Called when Channel is connected or when the write is enabled again 
     * @param e 
     */  
    private void generatePingTraffic(ChannelStateEvent e) {
        if (isPing.intValue() > 0) {  
            Channel channel = e.getChannel();  
            sendPingTraffic(channel);
        }  
    }  
  
    /** 
     * Called when a Pong message was received
     * @param e 
     */  
    private void generatePingTraffic(MessageEvent e) {
        if (isPing.intValue() > 0) {  
            Channel channel = e.getChannel();  
            sendPingTraffic(channel);
        }  
    }  
  
    /** 
     * Truly sends the Ping message if any (if not the last one)
     * @param channel 
     */  
    private void sendPingTraffic(Channel channel) {
        if ((channel.getInterestOps() & Channel.OP_WRITE) == 0) {  
            DropItPacket sendpp = new DropItPacket("PUT");
            sendpp.setAttribute("FILE_NAME", "test.txt");
            isPing.decrementAndGet();
            Channels.write(channel, sendpp);  
        }  
    }
}