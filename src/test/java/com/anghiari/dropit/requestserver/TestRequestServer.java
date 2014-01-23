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
    	String [] args = new String[]{"localhost","8005","150"};
    	
        // Parse options.
        String host = "localhost";
        int port = 8005;
        int nbMessage = 256;

        int size = 16384;

        ChannelFactory factory = new NioClientSocketChannelFactory(Executors  
                .newCachedThreadPool(), Executors.newCachedThreadPool(), 3);  
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        ChannelGroup channelGroup = new DefaultChannelGroup(
                TestRequestServer.class.getName());
        RequestHandler handler = new RequestHandler(nbMessage, size);

        bootstrap.getPipeline().addLast("handler", handler);  
        bootstrap.setOption("tcpNoDelay", true);  
        bootstrap.setOption("keepAlive", true);  
        bootstrap.setOption("reuseAddress", true);  
        bootstrap.setOption("connectTimeoutMillis", 100);  
  
        Channel channel = bootstrap.connect(new InetSocketAddress(host, port))
                .awaitUninterruptibly().getChannel();  

        channelGroup.add(channel);
        DropItPacket packet = handler.getResponse();
        //TODO Assert
        factory.releaseExternalResources();
    }  
}  