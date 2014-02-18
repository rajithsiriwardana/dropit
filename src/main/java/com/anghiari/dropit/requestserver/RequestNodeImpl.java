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
 * @author gayashan
 * @version ${Revision}
 */
public class RequestNodeImpl implements RequestNode {
    private ServerBootstrap exBootstrap;  //external communication
    private ServerBootstrap inBootstrap;   //internal communication, within request server nodes
    private ArrayList<String> activeFilesList;
    private ArrayList<InetSocketAddress> activeRSList;

    public void start(String ip, int port, int nbconn) {
        this.activeFilesList = new ArrayList<String>();
        //temporary - populate the files list
/*        this.activeFilesList.add("random" + new Random().nextInt() + ".txt");
        this.activeFilesList.add("random" + new Random().nextInt() + ".txt");*/

        final ChannelGroup channelGroup = new DefaultChannelGroup(RequestNodeImpl.class.getName());
        this.exBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));
        final ObjectHandler objectHandler = new ObjectHandler(channelGroup, activeFilesList);
        exBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectDecoder(),
                        new CompatibleObjectEncoder(),
//                        new ObjectHandler(channelGroup, activeFilesList));
                        objectHandler);
            }

            ;
        });

        // *** Start the Netty running ***
        System.out.println("DropIt server started");

        // Add the parent channel to the group
        Channel channel = exBootstrap.bind(new InetSocketAddress(ip, port));
        channelGroup.add(channel);

        // Setup communication between the RequestServer nodes for gossiping
        this.inBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        this.inBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectDecoder(),//CompatibleObjectDecoder might not work if the client side is not using netty CompatibleObjectDecoder for decoding.
                        new CompatibleObjectEncoder(),
                        new ObjectHandler(channelGroup, activeFilesList)
                );
            }
        });

        Channel acceptor = this.inBootstrap.bind(new InetSocketAddress(ip, port + 1));
        channelGroup.add(acceptor);
        if (acceptor.isBound()) {
            System.err.println("GOSSIP SERVER - bound to " + ip + ":" + (port + 1));

        } else {
            System.err.println("GOSSIP SERVER - Failed to bind to *: "
                    + (port + 1));
            this.inBootstrap.releaseExternalResources();
        }
        // *** Start the Netty running ***
        System.out.println("Gossip server started");
        initGossipProtocol();
    }

    private void initGossipProtocol() {
        GossipClientRunAtInterval gossipClientRunAtInterval = new GossipClientRunAtInterval(Constants.GOSSIP_INTERVAL, this);
        gossipClientRunAtInterval.start();
    }

    public void startGossiping() {
//        System.out.println("gossiping");
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
