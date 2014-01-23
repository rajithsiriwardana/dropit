package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author rajith
 * @version ${Revision}
 */
public class RequestNodeImpl implements RequestNode {
    private ServerBootstrap bootstrap_rs;
    private ArrayList<String> activeFilesList;
    private ArrayList<InetSocketAddress> activeRSList;

    public void start(int port, int nbconn) {
        this.activeFilesList = new ArrayList<String>();
        // Start server with Nb of active threads = 2*NB CPU + 1 as maximum.
        ChannelFactory factory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool(), Runtime.getRuntime()
                .availableProcessors() * 2 + 1);

        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        // Create the global ChannelGroup
        final ChannelGroup channelGroup = new DefaultChannelGroup(RequestNodeImpl.class.getName());
        // Create the blockingQueue to wait for a limited number of client
        BlockingQueue<Integer> answer = new LinkedBlockingQueue<Integer>();
        // 200 threads max, Memory limitation: 1MB by channel, 1GB global, 100
        // ms of timeout
        OrderedMemoryAwareThreadPoolExecutor pipelineExecutor =
                new OrderedMemoryAwareThreadPoolExecutor(200, 1048576, 1073741824, 100,
                        TimeUnit.MILLISECONDS,
                        Executors.defaultThreadFactory());

        bootstrap.setPipelineFactory(new PipelineFactory(channelGroup,
                pipelineExecutor, answer, nbconn));
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("child.reuseAddress", true);
        bootstrap.setOption("child.connectTimeoutMillis", 100);
        bootstrap.setOption("readWriteFair", true);

        // *** Start the Netty running ***
        System.out.println("DropIt server started");
        // Create the monitor

        // Add the parent channel to the group
        Channel channel = bootstrap.bind(new InetSocketAddress(port));
        channelGroup.add(channel);

        // Setup communication between the RequestServer nodes
        this.bootstrap_rs = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        this.bootstrap_rs.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectDecoder(),//ObjectDecoder might not work if the client side is not using netty ObjectDecoder for decoding.
                        new CompatibleObjectEncoder(),
                        new ObjectHandler(channelGroup)
                );
            }

            ;
        });
        initGossipProtocol();
    }

    private void initGossipProtocol() {
        GossipClientRunAtInterval gossipClientRunAtInterval = new GossipClientRunAtInterval(1000, this);
        gossipClientRunAtInterval.start();
    }

    public void startGossiping() {
        System.out.println("gossiping");
        DropItPacket packet = new DropItPacket(Constants.GOSSIP.toString());
        packet.setAttribute(Constants.GOS_LIST.toString(), this.activeFilesList);

    }

    public ArrayList<InetSocketAddress> getActiveRSList() {
        return activeRSList;
    }

    public void setActiveRSList(ArrayList<InetSocketAddress> activeRSList) {
        this.activeRSList = activeRSList;
    }
}
