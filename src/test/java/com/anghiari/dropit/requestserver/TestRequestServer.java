package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.execute.RequestServerRunner;
import com.anghiari.dropit.requestserver.utils.RequestHandler;
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
public class TestRequestServer {

    @Before
    public void setEnvironment(){
        RequestServerRunner.main(new String[]{"8005", "100"});
    }

    @Test
    public void testServer () throws Exception {
        // Print usage if no argument is specified. 
    	String [] args = new String[]{"localhost","8005","150"};
    	
        // Parse options.
        String host = args[0];  
        int port = Integer.parseInt(args[1]);  
        int nbMessage;  
  
        if (args.length >= 3) {  
            nbMessage = Integer.parseInt(args[2]);  
        } else {  
            nbMessage = 256;  
        }  
        int size = 16384;  
        if (args.length == 4) {  
            size = Integer.parseInt(args[3]);  
        }  
  
        // *** Start the Netty configuration ***  
  
        // Start client with Nb of active threads = 3 as maximum.  
        ChannelFactory factory = new NioClientSocketChannelFactory(Executors  
                .newCachedThreadPool(), Executors.newCachedThreadPool(), 3);  
        // Create the bootstrap  
        ClientBootstrap bootstrap = new ClientBootstrap(factory);  
        // Create the global ChannelGroup  
        ChannelGroup channelGroup = new DefaultChannelGroup(
                TestRequestServer.class.getName());
        // Create the associated Handler  
        RequestHandler handler = new RequestHandler(nbMessage, size);
  
        // Add the handler to the pipeline and set some options  
        bootstrap.getPipeline().addLast("handler", handler);  
        bootstrap.setOption("tcpNoDelay", true);  
        bootstrap.setOption("keepAlive", true);  
        bootstrap.setOption("reuseAddress", true);  
        bootstrap.setOption("connectTimeoutMillis", 100);  
  
        // *** Start the Netty running ***  
  
        // Connect to the server, wait for the connection and get back the channel  
        Channel channel = bootstrap.connect(new InetSocketAddress(host, port))  
                .awaitUninterruptibly().getChannel();  
        // Add the parent channel to the group  
        channelGroup.add(channel);  
        // Wait for the PingPong to finish  
        DropItPacket packet = handler.getPingPong();
        System.out  
                .println("Result: " + packet.toString() + " for 2x" +
                        nbMessage + " messages and " + size +  
                        " bytes as size of array");  
  
        // *** Start the Netty shutdown ***  
  
        // Now close all channels  
        System.out.println("close channelGroup");  
        channelGroup.close().awaitUninterruptibly();  
        // Now release resources  
        System.out.println("close external resources");  
        factory.releaseExternalResources();  
    }  
}  