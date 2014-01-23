package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.execute.RequestServerRunner;
import com.anghiari.dropit.requestserver.service.ServerClient;
import com.anghiari.dropit.requestserver.service.ServerClientHandler;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author rajith
 * @version ${Revision}
 */
public class RequestServerTest {

    @Before
    public void setupEnvironment(){
        RequestServerRunner.main(new String[]{"8005", "100"});
    }

    @Test
    public void testServer(){

        DropItPacket packet=new DropItPacket(Constants.PUT.toString());
        packet.setAttribute(Constants.FILE_NAME.toString(), "test.txt");

        ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool(), 3);
        // Create the bootstrap
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        // Create the global ChannelGroup
        ChannelGroup channelGroup = new DefaultChannelGroup(ServerClient.class.getName());
        // Create the associated Handler
        ServerClientHandler handler = new ServerClientHandler(packet);

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
        Channel channel = bootstrap.connect(new InetSocketAddress("localhost", 8005))
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


    }
}
