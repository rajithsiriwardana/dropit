package com.anghiari.dropit.requestserver.service;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.KeyId;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author madhawa
 * 
 */
public class ServerClient {
	String host;
	int port;

	public DropItPacket sendHash(KeyId keyId) throws Exception {
		String[] hostdetails = NodeFactory.getNode();
		
		this.host = hostdetails[0];
		this.port = Integer.parseInt(hostdetails[1]);
		
		/*
		 * create DrobIt instance and set the attributes 
		*/
		DropItPacket hashPacket=new DropItPacket(Constants.FND_SUSC.toString());
		hashPacket.setAttribute(Constants.KEY_ID.toString(), keyId);
		
		ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool(), 3);
		// Create the bootstrap
		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		// Create the global ChannelGroup
		ChannelGroup channelGroup = new DefaultChannelGroup(ServerClient.class.getName());
		// Create the associated Handler
		ServerClientHandler handler = new ServerClientHandler(hashPacket);

		// Add the handler to the pipeline and set some options
		bootstrap.getPipeline().addLast("handler", handler);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("connectTimeoutMillis", 100);

		// *** Start the Netty running ***
		System.out.println("*** Start the Netty running ***");
		// Connect to the server, wait for the connection and get back the
		// channel
		Channel channel = bootstrap.connect(new InetSocketAddress(host, port))
				.awaitUninterruptibly().getChannel();
		// Add the parent channel to the group
		channelGroup.add(channel);
		// Wait for the response from the fileServer
		DropItPacket respondHashPacket = handler.getRespond();

		// *** Start the Netty shutdown ***

		// Now close all channels
		System.out.println("close channelGroup");
		channelGroup.close().awaitUninterruptibly();
		// Now release resources
		System.out.println("close external resources");
		factory.releaseExternalResources();
		return respondHashPacket;
	}
}
