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
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @author rajith
 * @version ${Revision}
 */
public class RequestNodeImpl implements RequestNode {
    private ServerBootstrap bootstrap_rs;
    private ArrayList<String> activeFilesList;
    private ArrayList<InetSocketAddress> activeRSList;
    private ObjectHandler objectHandler;

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
        this.objectHandler = new ObjectHandler(channelGroup, new ArrayList<String>());

        // Create the blockingQueue to wait for a limited number of client
        BlockingQueue<Integer> answer = new LinkedBlockingQueue<Integer>();
        // 200 threads max, Memory limitation: 1MB by channel, 1GB global, 100
        // ms of timeout
        OrderedMemoryAwareThreadPoolExecutor pipelineExecutor =
                new OrderedMemoryAwareThreadPoolExecutor(200, 1048576, 1073741824, 100,
                        TimeUnit.MILLISECONDS,
                        Executors.defaultThreadFactory());

        PipelineFactory pipelineFactory = new PipelineFactory(channelGroup,
                pipelineExecutor, answer, nbconn, this.objectHandler);
        bootstrap.setPipelineFactory(pipelineFactory);
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
                        new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),//ObjectDecoder might not work if the client side is not using netty ObjectDecoder for decoding.
                        new ObjectEncoder(),
                        new ObjectHandler(channelGroup, activeFilesList)
                );
            }
        });

        Channel acceptor = this.bootstrap_rs.bind(new InetSocketAddress("127.0.0.1", port + 1));
        if (acceptor.isBound()) {
            System.err.println("+++ SERVER - bound to *: " + (port));

        } else {
            System.err.println("+++ SERVER - Failed to bind to *: "
                    + (port));
            this.bootstrap_rs.releaseExternalResources();
        }
        // *** Start the Netty running ***
        System.out.println("Gossip server started");
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
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),//ObjectDecoder might not work if the client side is not using netty ObjectDecoder for decoding.
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
