package com.anghiari.dropit.requestserver.service;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import com.anghiari.dropit.commons.DropItPacket;

/**
 * @author madhawa
 */

public class ServerClientHandler extends SimpleChannelUpstreamHandler {

	private static final Logger logger = Logger.getLogger(ServerClientHandler.class.getName());
	private final AtomicInteger isSend = new AtomicInteger(1);
	final BlockingQueue<DropItPacket> answer = new LinkedBlockingQueue<DropItPacket>();

	/**
	 * FileServer Object with the HashValue
	 */
	DropItPacket dropItPacket;

	/**
	 * Constructor
	 */
	public ServerClientHandler(DropItPacket dropItPacket) {

		this.dropItPacket = dropItPacket;
	}

	/**
	 * Method to wait for the final response object from the file server
	 * 
	 * @return the final dropItPacket object
	 */
	public DropItPacket getRespond() {
		for (;;) {
			try {
				return answer.take();
			} catch (InterruptedException e) {
				// Ignore.
			}
		}
	}

	/**
	 * Add the ObjectXxcoder to the Pipeline
	 */
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		e.getChannel().getPipeline().addFirst("decoder", new ObjectDecoder());
		e.getChannel().getPipeline().addAfter("decoder", "encoder", new ObjectEncoder());
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {

		generatePingTraffic(e);
	}

	@Override
	public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) {
		generatePingTraffic(e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		DropItPacket dropItPacketRes = (DropItPacket) e.getMessage();
		if (dropItPacketRes != null) {
			dropItPacket = dropItPacketRes;
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		if (e.getCause() instanceof IOException) {
			logger.log(Level.WARNING, "IOException from downstream.");
		} else {
			logger.log(Level.WARNING, "Unexpected exception from downstream.", e.getCause());
		}
		// Offer default object
		answer.offer(dropItPacket);
		Channels.close(e.getChannel());
	}

	/**
	 * Called when Channel is connected or when the write is enabled again
	 * 
	 * @param e
	 */
	private void generatePingTraffic(ChannelStateEvent e) {
		if (isSend.intValue() > 0) {
			Channel channel = e.getChannel();
			sendPacketTraffic(channel);
		}
	}


	private void sendPacketTraffic(Channel channel) {
		if ((channel.getInterestOps() & Channel.OP_WRITE) == 0) {
			if (dropItPacket == null) {
				logger.log(Level.WARNING, "Close channel");
				channel.close().addListener(new ChannelFutureListener() {
					public void operationComplete(ChannelFuture future) {
						answer.offer(dropItPacket);
					}
				});
				return;
			}
			isSend.decrementAndGet();
			Channels.write(channel, dropItPacket);
		}
	}

}
