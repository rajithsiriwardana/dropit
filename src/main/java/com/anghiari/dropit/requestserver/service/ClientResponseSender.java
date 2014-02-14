package com.anghiari.dropit.requestserver.service;

import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.requestserver.ObjectHandler;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.CompatibleObjectDecoder;
import org.jboss.netty.handler.codec.serialization.CompatibleObjectEncoder;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author gayashan
 */
public class ClientResponseSender {
    private ServerBootstrap bootstrap;

    public void setup(InetSocketAddress clientAddress, DropItPacket packet, String rsip, int rsport, final ArrayList<String> activeFilesList) {
        final ChannelGroup channelGroup = new DefaultChannelGroup(ClientResponseSender.class.getName());
        this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));
        this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectDecoder(),
                        new CompatibleObjectEncoder(),
                        new ObjectHandler(channelGroup, activeFilesList)
                );
            }
        });

        Channel channel = this.bootstrap.bind(new InetSocketAddress(rsip, rsport));
        channelGroup.add(channel);
        System.out.println("Request server bound to client " + rsip + ":" + rsport);
        sendRsponseToClient(packet, clientAddress, activeFilesList);
        bootstrap.releaseExternalResources();
    }

    private void sendRsponseToClient(DropItPacket packet, InetSocketAddress clientAddress, ArrayList<String> activeFilesList) {
        ChannelGroup channelGroup = new DefaultChannelGroup(ClientResponseSender.class.getName());
        sendMessage(packet, clientAddress, new ObjectHandler(channelGroup, activeFilesList));
    }

    private void sendMessage(DropItPacket packet, InetSocketAddress inetSocketAddress, final ObjectHandler objectHandler) {
        Executor bossPool = Executors.newCachedThreadPool();
        Executor workerPool = Executors.newCachedThreadPool();
        ChannelFactory channelFactory = new NioClientSocketChannelFactory(bossPool, workerPool);
        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectEncoder(),
                        new CompatibleObjectDecoder(),//CompatibleObjectDecoder might not work if the client side is not using netty CompatibleObjectDecoder for decoding.
                        objectHandler);
            }
        };
        ClientBootstrap clientBootstrap = new ClientBootstrap(channelFactory);
        clientBootstrap.setPipelineFactory(pipelineFactory);

        ChannelFuture cf = clientBootstrap.connect(inetSocketAddress);
        final DropItPacket dropPacket = packet;
        cf.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                // check to see if we succeeded
                if (future.isSuccess()) {
                    Channel channel = future.getChannel();
                    channel.write(dropPacket);
                    // asynchronous
                }
            }
        });
    }
}
