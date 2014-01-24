package com.anghiari.dropit.requestserver.service;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.KeyId;

/**
 * @author madhawa
 */
public class ServerClient {
    String host;
    int port;

    public DropItPacket sendHash(KeyId keyId, Constants method, String path, String fileName) throws Exception {
        String[] hostdetails = NodeFactory.getNode();

        this.host = hostdetails[0];
        this.port = Integer.parseInt(hostdetails[1]);

		/*
		 * create DrobIt instance and set the attributes 
		*/
		/*DropItPacket hashPacket=new DropItPacket(Constants.FND_SUSC.toString());
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
		factory.releaseExternalResources();*/
        DropItPacket respondHashPacket = null;
        if (Constants.PUT.toString().equalsIgnoreCase(method.toString())) {
            respondHashPacket = new DropItPacket(Constants.RES_PUT.toString());
            respondHashPacket.setAttribute(Constants.NODE_IP.toString(), host);
            respondHashPacket.setAttribute(Constants.NODE_PORT.toString(), port);
            respondHashPacket.setAttribute(Constants.FILE_PATH.toString(), path);
            respondHashPacket.setAttribute(Constants.FILE_NAME.toString(), fileName);
        } else if (Constants.GET.toString().equalsIgnoreCase(method.toString())) {
            respondHashPacket = new DropItPacket(Constants.RES_GET.toString());
            respondHashPacket.setAttribute(Constants.NODE_IP.toString(), host);
            respondHashPacket.setAttribute(Constants.NODE_PORT.toString(), port);
            respondHashPacket.setAttribute(Constants.FILE_PATH.toString(), path);
            respondHashPacket.setAttribute(Constants.FILE_NAME.toString(), fileName);
        }
        System.out.println("Packet created! " + respondHashPacket.getAttribute(Constants.NODE_IP.toString()));
        return respondHashPacket;
    }
}
