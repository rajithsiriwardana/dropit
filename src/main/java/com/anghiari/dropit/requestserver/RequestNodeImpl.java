package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
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
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author rajith
 * @version ${Revision}
 */
public class RequestNodeImpl implements RequestNode {
    private ServerBootstrap bootstrap_rs;
    private ArrayList<String> activeFilesList;
    private ArrayList<InetSocketAddress> activeRSList;
    private ObjectHandler objectHandler;

    public void start(String ip, int port, int nbconn) {
        this.activeFilesList = new ArrayList<String>();
        //temporary - populate the files list
        this.activeFilesList.add("random" + new Random().nextInt() + ".txt");

        // Start server with Nb of active threads = 2*NB CPU + 1 as maximum.
        ChannelFactory factory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool(), Runtime.getRuntime()
                .availableProcessors() * 2 + 1);
        final ChannelGroup channelGroup = new DefaultChannelGroup(RequestNodeImpl.class.getName());
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectDecoder(),
                        new CompatibleObjectEncoder(), new ObjectHandler(channelGroup, activeFilesList));
            }

            ;
        });

        // *** Start the Netty running ***
        System.out.println("DropIt server started");
        // Create the monitor

        // Add the parent channel to the group
        Channel channel = bootstrap.bind(new InetSocketAddress(ip, port));
        channelGroup.add(channel);

        // Setup communication between the RequestServer nodes
        this.bootstrap_rs = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        this.bootstrap_rs.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectDecoder(),//CompatibleObjectDecoder might not work if the client side is not using netty CompatibleObjectDecoder for decoding.
                        new CompatibleObjectEncoder(),
                        new ObjectHandler(channelGroup, activeFilesList)
                );
            }
        });
/*
        Channel acceptor = this.bootstrap_rs.bind(new InetSocketAddress(ip, port + 1));
        if (acceptor.isBound()) {
            System.err.println("+++ SERVER - bound to " + ip + ":" + (port + 1));

        } else {
            System.err.println("+++ SERVER - Failed to bind to *: "
                    + (port + 1));
            this.bootstrap_rs.releaseExternalResources();
        }
        // *** Start the Netty running ***
        System.out.println("Gossip server started");
        initGossipProtocol();*/
    }

    private void initGossipProtocol() {
        GossipClientRunAtInterval gossipClientRunAtInterval = new GossipClientRunAtInterval(5000, this);
        gossipClientRunAtInterval.start();
    }

    public void startGossiping() {
        System.out.println("gossiping");
        DropItPacket packet = new DropItPacket(Constants.GOSSIP.toString());
        packet.setAttribute(Constants.GOS_LIST.toString(), this.activeFilesList);
        //select a request server node at random
        Random r = new Random();
        int index = r.nextInt(this.activeRSList.size());
        ChannelGroup channelGroup = new DefaultChannelGroup(RequestNodeImpl.class.getName());
        sendMessage(packet, activeRSList.get(index), new ObjectHandler(channelGroup, activeFilesList));
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

    public ArrayList<InetSocketAddress> getActiveRSList() {
        return activeRSList;
    }

    public void setActiveRSList(ArrayList<InetSocketAddress> activeRSList) {
        this.activeRSList = activeRSList;
    }
}
