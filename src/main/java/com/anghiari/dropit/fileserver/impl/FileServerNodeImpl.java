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
	int nextFingerToUpdate;

	private static final Logger logger = Logger
			.getLogger(FileServerNodeImpl.class.getName());

	public void bootServer(FileNode node) {
		this.node = node;
		setSuccessors(new ArrayList<FileNode>());
		nextFingerToUpdate = 0;
		initSuccessors();
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
				return Channels.pipeline(new CompatibleObjectDecoder(),// ObjectDecoder
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
				return Channels.pipeline(new CompatibleObjectDecoder(),// ObjectDecoder
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
		 * Bind and start the ring communication
		 */
		Channel acceptor_ring = this.bootstrap_ring.bind(new InetSocketAddress(
				node.getIp(), node.getPort_ring()));

		if (acceptor_ring.isBound()) {
			System.err.println("+++ SERVER - bound to *: "
					+ node.getPort_ring());

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
		String ip = "127.0.0.1";

		int[] intPorts = Configurations.intPorts;
		int[] extPorts = Configurations.extPorts;
		int[] keys = Configurations.fileNodeKeys;
		int numberOfNodes = intPorts.length;

		long myKey = node.getKey().getHashId();
		FileNode[] nodes = new FileNode[numberOfNodes];

		for (int i = 0; i < numberOfNodes; i++) {
			nodes[i] = new FileNode(ip, extPorts[i], intPorts[i], new KeyId(
					keys[i]));
		}

		int pos = 0;
		for (int i = 0; i < numberOfNodes; i++) {
			if (myKey == keys[i]) {
				pos = i + 1;
				break;
			}
		}

		getSuccessors().add(
				pos < numberOfNodes ? nodes[pos++] : nodes[pos++
						- numberOfNodes]);
		getSuccessors().add(
				pos < numberOfNodes ? nodes[pos++] : nodes[pos++
						- numberOfNodes]);
		getSuccessors().add(
				pos < numberOfNodes ? nodes[pos++] : nodes[pos++
						- numberOfNodes]);

		System.out.println("My Successors");
		for (int i = 0; i < Constants.SUCCESSOR_LIST_SIZE; i++) {
			System.out
					.println(" " + getSuccessors().get(i).getPort()
							+ "with key "
							+ getSuccessors().get(i).getKey().getHashId());
		}
	}

	private void initRunAtInterval() {

		FileServerRunAtInterval intervalExecutor = new FileServerRunAtInterval(
				1000, this);

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

	public FileNode findSuccessor(KeyId key) {
		/*
		 * If my predecessor key > key ==> ask my predecessor to find keys'
		 * successor If my key > key > my predecessor key ==> i'm the successor
		 * If key > my key ==> ask my successor to find keys' successor
		 */

		/*
		 * Try to find the closestPredecessor from my finger list. If I'm the
		 * closestPredecessor => return my successor. Else ask
		 * closestPredecessor to find the keys' successor.
		 */

		long givenKeyValue = key.getHashId();

		FileNode closestPredecessor = getClosestPredecessor(givenKeyValue);
		if (node.equals(closestPredecessor)) {
			return getSuccessor();
		}

		/* Send message to closestPredecessor to get the keys' successor */
		DropItPacket packet = new DropItPacket(Constants.FND_SUSC.toString());
		packet.setAttribute(Constants.KEY_ID.toString(), key);
		sendMessage(packet, closestPredecessor);

		// TODO: HAndle this
		return null;
	}

	/*
	 * Scan this nodes finger list to find the closest predecessor for the given
	 * key.
	 */
	private FileNode getClosestPredecessor(long key) {
		long myKey = node.getKey().getHashId();
		long currentFingerKey;
		for (int i = fingers.size() - 1; i >= 0; i--) {
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

		// TODO: Use dropit protocol to get the ket of given file node
		return 0;
	}

	public void sendMessage(DropItPacket packet, FileNode node) {
		System.out.println("Sending MSG; Method:" + packet.getMethod()
				+ ", Node IP:" + node.getIp() + ", Port:" + node.getPort());

		Executor bossPool = Executors.newCachedThreadPool();
		Executor workerPool = Executors.newCachedThreadPool();
		ChannelFactory channelFactory = new NioClientSocketChannelFactory(
				bossPool, workerPool);
		ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new CompatibleObjectEncoder(),
						new CompatibleObjectDecoder(),// ObjectDecoder might not
														// work if the client
														// side is not using
														// netty ObjectDecoder
														// for decoding.
						new FileHandler());
			}
		};
		ClientBootstrap clientBootstrap = new ClientBootstrap(channelFactory);
		clientBootstrap.setPipelineFactory(pipelineFactory);

		InetSocketAddress addressToConnectTo = new InetSocketAddress(
				node.getIp(), node.getPort());
		ChannelFuture cf = clientBootstrap.connect(addressToConnectTo);
		final DropItPacket dropPacket = packet;
		try {
			cf.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future)
						throws Exception {
					// check to see if we succeeded
					if (future.isSuccess()) {
						Channel channel = future.getChannel();
						channel.write(dropPacket);
						// asynchronous
					}
				}
			});
		} catch (Exception e) {
			// Handle Exception
		}
	}

	public void sendMessage(DropItPacket packet, FileNode node,
			final SimpleChannelHandler handler) {
		System.out.println("Sending MSG From Overloaded Method:"
				+ packet.getMethod() + ", Node IP:" + node.getIp() + ", Port:"
				+ node.getPort_ring());

		Executor bossPool = Executors.newCachedThreadPool();
		Executor workerPool = Executors.newCachedThreadPool();
		ChannelFactory channelFactory = new NioClientSocketChannelFactory(
				bossPool, workerPool);
		ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new CompatibleObjectEncoder(),
						new CompatibleObjectDecoder(),// ObjectDecoder might not
														// work if the client
														// side is not using
														// netty ObjectDecoder
														// for decoding.
						handler);
			}
		};
		ClientBootstrap clientBootstrap = new ClientBootstrap(channelFactory);
		clientBootstrap.setPipelineFactory(pipelineFactory);

		System.out.println("ring port line 314 " + node.getPort_ring());

		InetSocketAddress addressToConnectTo = new InetSocketAddress(
				node.getIp(), node.getPort_ring());
		ChannelFuture cf = clientBootstrap.connect(addressToConnectTo);
		final DropItPacket dropPacket = packet;
		cf.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future)
					throws Exception {
				// check to see if we succeeded
				if (future.isSuccess()) {
					Channel channel = future.getChannel();
					channel.write(dropPacket);
					// asynchronous
				}
			}
		});

	}

	public FileNode getSuccessor() {
		return getSuccessors().get(0);
	}

	private void pingSuccessor() {
		DropItPacket dropItPacket = new DropItPacket(Constants.PING.name());
		sendMessage(dropItPacket, getSuccessors().get(0));
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
		this.sendMessage(packet, this.getSuccessor(),
				new RingCommunicationHandler(this));
		System.out.println("Stabilized " + this.node.getPort_ring());
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

	/**
	 * Called periodically. Refreshes the finger table entries.
	 * nextFingerToUpdate stores the index of the next finger to fix.
	 */
	public void fixFingers() {
		fixFinger(nextFingerToUpdate);
		nextFingerToUpdate = (nextFingerToUpdate + 1) % fingers.size();
		System.out.println("-----MY FINGERS------- " + node.getPort_ring()
				+ " " + node.getKey().getHashId());
		for (int i = 0; i < fingers.size(); i++) {
			System.out.println("ME: " + node.getPort_ring() + " FInger at " + i
					+ ", PORT: " + fingers.get(i).getPort_ring() + ", KEY: "
					+ fingers.get(i).getKey().getHashId());
		}
	}

	public void fixFinger(int finger) {
		// Iface iface;
		// try {
		// iface = clientFactory.get(node.getTNode());
		// } catch (RetryFailedException e) {
		// // We should always be able to connect to our own service. If not,
		// its serious
		// logger.severe("FixFingers [" + node.getPort_ring()
		// + "] failed to get client to itself.");
		//
		// // Skip this finger. It will get updated on the next go throw the
		// finger table.
		// return;
		// }

		// Keep as separate variable: Be careful of some weird java issues with
		// overflowing ints
		long base = node.getKey().getHashId();
		long pow = 0x0000000000000001L << finger;
		long id = base + pow;

		KeyId keyId = new KeyId(id);
		System.out.println("Finding node for KEY:" + id);
		FileNode updatedFinger = findSuccessor(keyId);
		System.out.println("Found node for key:" + id + ",  "
				+ updatedFinger.getPort_ring() + "with key "
				+ updatedFinger.getKey().getHashId());
		System.out.println("" + node.getPort_ring()
				+ "------CHANGING FINGER at " + finger + " to "
				+ updatedFinger.getPort_ring() + " "
				+ updatedFinger.getKey().getHashId());
		setFinger(finger, updatedFinger);

	}

	public void setFinger(int i, FileNode n) {
		if (i < 0 || i >= fingers.size())
			return;
		// if (i == 0) {
		// setSuccessor(n);
		// return;
		// }
		fingers.set(i, n);
	}

	public FileNode getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(FileNode predecessor) {
		this.predecessor = predecessor;
	}

	public int requestNodePosition(DropItPacket packet) {

		sendMessage(packet, node);
		return 0;
	}

	public ArrayList<FileNode> getSuccessors() {
		return successors;
	}

	public void setSuccessors(ArrayList<FileNode> successors) {
		this.successors = successors;
	}

	public void setSuccessor(FileNode node) {
		// Add the node to the immediate successor
		successors.add(0, node);
		
		// Remove the last successor to keep the succesor list to the size 3
		successors.remove(successors.size()-1);
	}

	public FileNode getNode() {
		return node;
	}
}
