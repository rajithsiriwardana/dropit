package com.anghiari.dropit.requestserver.service;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.commons.KeyId;
import com.anghiari.dropit.requestserver.FSResponseHandler;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author gayashan
 */
public class NodeFinder {
    private ServerBootstrap bootstrap;

    public void setup(String fsip, int fsport, String rsip, int rsport, KeyId id, DropItPacket received, final ObjectHandler objectHandler) {
        final ChannelGroup channelGroup = new DefaultChannelGroup(NodeFinder.class.getName());
        this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        final FSResponseHandler fsResponseHandler = new FSResponseHandler(channelGroup, objectHandler);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectDecoder(),
                        new CompatibleObjectEncoder(),
                        fsResponseHandler
                );
            }
        });

        Channel channel = this.bootstrap.bind(new InetSocketAddress(rsip, rsport + 2));
        channelGroup.add(channel);
        if (channel.isBound()) {
            System.out.println("Request server bound to " + rsip + ":" + (rsport + 2));
        } else {
            bootstrap.releaseExternalResources();
        }
        callFindSuccessor(fsip, fsport, rsip, rsport, id, received, objectHandler);
        bootstrap.releaseExternalResources();
    }

    private void callFindSuccessor(String fsip, int fsport, String rsip, int rsport, KeyId id, DropItPacket received, ObjectHandler objectHandler) {
        DropItPacket packet = new DropItPacket(Constants.FND_SUSC.toString());
        packet.setAttribute(Constants.REQ_NODE.toString(), new FileNode(rsip, rsport));
        packet.setAttribute(Constants.KEY_ID.toString(), id);
        packet.setAttribute(Constants.RECVD_PACKET.toString(), received.getMethod().toString());
        packet.setAttribute(Constants.RECVD_PATH.toString(), received.getAttribute(Constants.FILE_PATH.toString()));
        packet.setAttribute(Constants.RECVD_FNAME.toString(), received.getAttribute(Constants.FILE_NAME.toString()));
        ChannelGroup channelGroup = new DefaultChannelGroup(NodeFinder.class.getName());
        FSResponseHandler fsResponseHandler = new FSResponseHandler(channelGroup, objectHandler);
        sendMessage(packet, new InetSocketAddress(fsip, fsport), fsResponseHandler);
    }

    private void sendMessage(DropItPacket packet, InetSocketAddress inetSocketAddress, final FSResponseHandler fsResponseHandler) {
        Executor bossPool = Executors.newCachedThreadPool();
        Executor workerPool = Executors.newCachedThreadPool();
        ChannelFactory channelFactory = new NioClientSocketChannelFactory(bossPool, workerPool);
        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectEncoder(),
                        new CompatibleObjectDecoder(),//CompatibleObjectDecoder might not work if the client side is not using netty CompatibleObjectDecoder for decoding.
                        fsResponseHandler);
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
