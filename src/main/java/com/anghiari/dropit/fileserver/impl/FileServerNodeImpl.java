package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.commons.*;
import com.anghiari.dropit.fileserver.FileServerNode;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileServerNodeImpl implements FileServerNode {

	private ServerBootstrap bootstrap;
	private ServerBootstrap bootstrap_ring;

	private FileNode node;
	private FileNode predecessor;
	private ArrayList<FileNode> successors;
	private ArrayList<FileNode> fingers;

	private static final Logger logger = Logger
			.getLogger(FileServerNodeImpl.class.getName());

	public void bootServer(FileNode node) {
		this.node = node;
		successors = new ArrayList<FileNode>();
		successors.add(FileNodeList.getFileNodeList().get(1));

		// initSuccessors();
		initFingers();

		/**
		 * setup the channel for Outside Communication
		 */

		this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		/* Set up the pipeline factory. */
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
						new CompatibleObjectDecoder(),// ObjectDecoder
																				// might
																				// not
																				// work
																				// if
																				// the
																				// client
																				// side
																				// is
																				// not
																				// using
																				// netty
																				// ObjectDecoder
																				// for
																				// decoding.
						new CompatibleObjectEncoder(), new FileHandler());
			};
		});

		/**
		 * setup the channel for Ring Communication
		 */

		this.bootstrap_ring = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));

		// Set up the pipeline factory.
		this.bootstrap_ring.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
						new CompatibleObjectDecoder(),// ObjectDecoder
																				// might
																				// not
																				// work
																				// if
																				// the
																				// client
																				// side
																				// is
																				// not
																				// using
																				// netty
																				// ObjectDecoder
																				// for
																				// decoding.
						new CompatibleObjectEncoder(), new FileHandler());
			};
		});

		/**
		 * Bind and start to accept incoming connections.
		 */
		Channel acceptor = this.bootstrap.bind(new InetSocketAddress(node
				.getIp(), node.getPort()));

		if (acceptor.isBound()) {
			System.err.println("+++ SERVER - bound to *: " + node.getPort());

		} else {
			System.err.println("+++ SERVER - Failed to bind to *: "
					+ node.getPort());
			this.bootstrap.releaseExternalResources();
		}

		/**
		 * Bind and start to accept incoming connections.
		 */
		Channel acceptor_ring = this.bootstrap.bind(new InetSocketAddress(node
				.getIp(), node.getPort_ring()));

		if (acceptor_ring.isBound()) {
			System.err.println("+++ SERVER - bound to *: " + node.getPort_ring());

		} else {
			System.err.println("+++ SERVER - Failed to bind to *: "
					+ node.getPort_ring());
			this.bootstrap_ring.releaseExternalResources();
		}

		/**
		 * Start the heart beat
		 */
		initRunAtInterval();
	}

	private void initFingers() {
		/* TEMPORARY IMPLEMENTATION. */
		fingers = new ArrayList<FileNode>();
		for (int i = 0; i < Constants.KEY_SPACE; i++) {
			fingers.add(node);
		}
	}

	private void initSuccessors() {
		/* TEMPORARY IMPLEMENTATION. */
		successors = new ArrayList<FileNode>();
		for (int i = 0; i < Constants.SUCCESSOR_LIST_SIZE; i++) {
			successors.add(node);
		}
	}

	private void initRunAtInterval() {

		FileServerRunAtInterval intervalExecutor = new FileServerRunAtInterval(
				5000, this);

		intervalExecutor.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.anghiari.dropit.fileserver.FileServerNode#findSuccessor(int)
	 * Each node has a findSuccessor method, return ip of the fileserver which
	 * has the file
	 */

	public int findSuccessor(int hashVal) {
		// TODO Auto-generated method stub

		return 0;
	}
	
    public FileNode findSuccessor(KeyId key){
        /*If my predecessor key > key ==> ask my predecessor to find keys' successor
        * If my key > key > my predecessor key ==> i'm the successor
        * If key > my key ==> ask my successor to find keys' successor*/

        /*Try to find the closestPredecessor from my finger list. If I'm the closestPredecessor => return my successor.
        * Else ask my successor to find the keys' successor.*/

        long givenKeyValue = key.getHashId();

        FileNode closestPredecessor = getClosestPredecessor(givenKeyValue);
        if(node.equals(closestPredecessor)){
            return getSuccessor();
        }

        //Send message to my successor to get the keys' successor
        FileNode mySuccessor = getSuccessor();
        DropItPacket packet = new DropItPacket(Constants.FND_SUSC.toString());
        packet.setAttribute(Constants.KEY_ID.toString(), key);
        sendMessage(packet, mySuccessor);

        return null;
    }

    /*Scan this nodes finger list to find the closest predecessor for the given key.*/
    private FileNode getClosestPredecessor(long key){
        long myKey = node.getKey().getHashId();
        long currentFingerKey;
        for(int i = fingers.size()-1; i>=0; i--){
            currentFingerKey = fingers.get(i).getKey().getHashId();
            if (currentFingerKey != myKey
                    && isAfterXButBeforeOrEqualY(key, currentFingerKey, myKey)) {
                return fingers.get(i);
            }
        }

        return node;
    }

    private boolean isAfterXButBeforeOrEqualY(long id, long x, long y) {
        if (x == y)
            return true;
        if (y < id) {
            return x > y && x < id;
        } else {
            return x < y && x < id || x > y && x > id;
        }
    }

    private long retrieveKeyValue(FileNode predecessor) {

        //TODO: Use dropit protocol to get the ket of given file node
        return 0;
    }

    public void sendMessage(DropItPacket packet, FileNode node){
        System.out.println("Sending MSG; Method:" + packet.getMethod()+ ", Node IP:"+ node.getIp() +", Port:" + node.getPort());

        Executor bossPool = Executors.newCachedThreadPool();
        Executor workerPool = Executors.newCachedThreadPool();
        ChannelFactory channelFactory = new NioClientSocketChannelFactory(bossPool, workerPool);
        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectEncoder(),
                        new CompatibleObjectDecoder(),//ObjectDecoder might not work if the client side is not using netty ObjectDecoder for decoding.
                        new FileHandler());
            }
        };
        ClientBootstrap clientBootstrap = new ClientBootstrap(channelFactory);
        clientBootstrap.setPipelineFactory(pipelineFactory);

        InetSocketAddress addressToConnectTo = new InetSocketAddress(node.getIp(), node.getPort());
        ChannelFuture cf = clientBootstrap.connect(addressToConnectTo);
        final DropItPacket dropPacket = packet;
        try{
	        cf.addListener(new ChannelFutureListener(){
	            public void operationComplete(ChannelFuture future) throws Exception {
	                // check to see if we succeeded
	                if(future.isSuccess()) {
	                    Channel channel = future.getChannel();
	                    channel.write(dropPacket);
	                    // asynchronous
	                }
	            }
	        });
        }catch(Exception e){
        	//Handle Exception
        }
    }
    
    public void sendMessage(DropItPacket packet, FileNode node, final SimpleChannelHandler handler){
        System.out.println("Sending MSG; Method:" + packet.getMethod()+ ", Node IP:"+ node.getIp() +", Port:" + node.getPort());

        Executor bossPool = Executors.newCachedThreadPool();
        Executor workerPool = Executors.newCachedThreadPool();
        ChannelFactory channelFactory = new NioClientSocketChannelFactory(bossPool, workerPool);
        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new CompatibleObjectEncoder(),
                        new CompatibleObjectDecoder(),//ObjectDecoder might not work if the client side is not using netty ObjectDecoder for decoding.
                        handler);
            }
        };
        ClientBootstrap clientBootstrap = new ClientBootstrap(channelFactory);
        clientBootstrap.setPipelineFactory(pipelineFactory);

        InetSocketAddress addressToConnectTo = new InetSocketAddress(node.getIp(), node.getPort());
        ChannelFuture cf = clientBootstrap.connect(addressToConnectTo);
        final DropItPacket dropPacket = packet;
        cf.addListener(new ChannelFutureListener(){
            public void operationComplete(ChannelFuture future) throws Exception {
                // check to see if we succeeded
                if(future.isSuccess()) {
                    Channel channel = future.getChannel();
                    channel.write(dropPacket);
                    // asynchronous
                }
            }
        });
        
    }

    private FileNode getSuccessor(){
        return successors.get(0);
    }

    private void pingSuccessor(){
        DropItPacket dropItPacket = new DropItPacket(Constants.PING.name());
        sendMessage(dropItPacket, successors.get(0));
    }

    /**
	 * Checking whether the successor is alive
	 * 
	 * @param ip
	 * @param port
	 */
	public void pingSuccessor(String ip, int port) {

		System.out.println("PING to Successor " + ip + " " + port);

		DropItPacket dropItPacket = new DropItPacket(Constants.PING.name());
		sendMessage(dropItPacket, new FileNode(ip, port));

	}

	/**
	 * Check whether the successor is alive
	 */
	public void stabilize() {
		
		DropItPacket packet = new DropItPacket(Constants.PING.toString());
		this.sendMessage(packet, node, new RingCommunicationHandler());
		System.out.println("Stabilized");
	}

	/**
	 * Called periodically. Checks whether the predecessor has failed.
	 */
	public void checkPredecessor() {

		if (this.getPredecessor() != null) {

		} else {
			logger.log(Level.WARNING, "Predecessor is not set");
			this.setPredecessor(null);
		}
	}

	private FileNode getPredecessor() {
		return predecessor;
	}

	private void setPredecessor(FileNode predecessor) {
		this.predecessor = predecessor;
	}

	public int requestNodePosition(DropItPacket packet) {

		sendMessage(packet, node);
		return 0;
	}

}

/**
 * Handles the response message sent from the request server.
 * 
 * @author Sanka
 * 
 */
class NodeConnectionHandler {
	public void handleResponse(String[] address) {
		if (address.length < 2) {
			throw new IllegalStateException("Address format is unacceptable.");
		}
		String host = address[0];
		int port = Integer.parseInt(address[1]);

		
	}
}
