package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.execute.RequestServerRunner;
import com.anghiari.dropit.requestserver.service.ServerClient;
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
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        ChannelGroup channelGroup = new DefaultChannelGroup(ServerClient.class.getName());
        ResponseHandler handler = new ResponseHandler();

        bootstrap.getPipeline().addLast("handler", handler);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("connectTimeoutMillis", 100);

        Channel channel = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8005))
                .awaitUninterruptibly().getChannel();
        channelGroup.add(channel);

        DropItPacket respondHashPacket = handler.getResponse();

        channelGroup.close().awaitUninterruptibly();
        factory.releaseExternalResources();

        //TODO complete test

    }
}
