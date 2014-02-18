package com.anghiari.dropit.fileserver.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.anghiari.dropit.commons.*;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.CompatibleObjectDecoder;
import org.jboss.netty.handler.codec.serialization.CompatibleObjectEncoder;

import com.anghiari.dropit.fileserver.FileServerNode;

public class FileServerNodeImpl implements FileServerNode {

	private ServerBootstrap bootstrap;
	private ServerBootstrap bootstrap_ring;
    private BlockingRequestManager blockingManager;
	private FileNode node;
	private FileNode predecessor;
	private ArrayList<FileNode> successors;
	private ArrayList<FileNode> fingers;
	int nextFingerToUpdate;
    boolean succAlive;
    boolean predAlive;

	private static final Logger logger = Logger
			.getLogger(FileServerNodeImpl.class.getName());

	public void bootServer(FileNode newNode, boolean status){

		this.node = newNode;
        blockingManager = new BlockingRequestManager();
		setSuccessors(new ArrayList<FileNode>());
		nextFingerToUpdate = 0;
        succAlive = true;
        predAlive = true;
		initFingers();
        if(status){
            initSuccessorsAndPredecessor();
        }

		/**
		 * setup the channel for Outside Communication
		 */

		this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
        this.bootstrap.setOption("connectTimeoutMillis",80000);
        final FileHandler fileHandler=new FileHandler(this);
		/* Set up the pipeline factory. */
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new CompatibleObjectDecoder(),
						new CompatibleObjectEncoder(), fileHandler);
			};
		});

		/**
		 * Bind and start to accept incoming connections.
		 */
		Channel acceptor = this.bootstrap.bind(new InetSocketAddress(newNode
                .getIp(), newNode.getPort()));

		if (acceptor.isBound()) {
			System.err.println("+++ SERVER - bound to *: " + newNode.getPort());

		} else {
			System.err.println("+++ SERVER - Failed to bind to *: "
					+ newNode.getPort());
			this.bootstrap.releaseExternalResources();
		}

		/**
		 * Start the heart beat
		 */
		initRunAtInterval();
	}

	private void initFingers() {
		fingers = new ArrayList<FileNode>();
		for (int i = 0; i < Constants.KEY_SPACE; i++) {
			fingers.add(node);
		}
	}

	private void initSuccessorsAndPredecessor() {
		/* TEMPORARY IMPLEMENTATION. */
		String ip = Configurations.ip;

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


        predecessor = nodes[pos==1? numberOfNodes-1: pos-2];

		successors.add(
                pos < numberOfNodes ? nodes[pos++] : nodes[pos++
                        - numberOfNodes]);
		successors.add(
                pos < numberOfNodes ? nodes[pos++] : nodes[pos++
                        - numberOfNodes]);
		successors.add(
                pos < numberOfNodes ? nodes[pos++] : nodes[pos++
                        - numberOfNodes]);

        System.out.println("_________________________________________________________________________________________");
        System.out.println("Successors: " + node.getPort() );
		for (int i = 0; i < Constants.SUCCESSOR_LIST_SIZE; i++) {
			System.out
					.println("Me: " + node.getPort() + ", Succ at "+ i + " is " + getSuccessors().get(i).getPort()
							+ " : "
							+ getSuccessors().get(i).getKey().getHashId());
		}
        System.out.println("" + node.getPort() + " Predecessor: " + predecessor.getPort() + " : " + predecessor.getKey().getHashId());
    }

	private void initRunAtInterval() {

		FileServerRunAtInterval intervalExecutor = new FileServerRunAtInterval(
				Constants.FS_INTERVAL, this);

		intervalExecutor.start();
	}

	public FileNode findSuccessor(KeyId key) {

		/*
		 * Try to find the closestPredecessor from my finger list. If I'm the
		 * closestPredecessor => return my successor. Else ask
		 * closestPredecessor to find the keys' successor.
		 */

		long givenKeyValue = key.getHashId();
//        System.out.println(">>>>>>>>INSIDE FIND SUCCESSOR: FINDING FOR KEY:"+ givenKeyValue +"<<<<<<<<");
		FileNode closestPredecessor = getClosestPredecessor(givenKeyValue);
		if (node.equals(closestPredecessor)) {
			return getSuccessor();
		}

		/* Send message to closestPredecessor to get the keys' successor */
//        System.out.println(">>>>>>>>Asking Closest Predessor to Find SUccessor<<<<<<<<");
        DropItPacket packet = new DropItPacket(Constants.FND_SUSC.toString());
		packet.setAttribute(Constants.KEY_ID.toString(), key);

		sendMessage(packet, closestPredecessor);
//        System.out.println(">>>>>>>>PREDECESSOR REPLIED :D<<<<<<<<");
        return null;
	}

    public FileNode findSuccessor(DropItPacket inPacket){
        KeyId key = (KeyId)inPacket.getAttribute(Constants.KEY_ID.toString());
        long givenKeyValue = key.getHashId();
//        System.out.println(">>>>>>>>INSIDE FIND SUCCESSOR: FINDING FOR KEY:"+ givenKeyValue +"<<<<<<<<");
        FileNode closestPredecessor = getClosestPredecessor(givenKeyValue);
        if (node.equals(closestPredecessor)) {
            return getSuccessor();
        }

		/* Send message to closestPredecessor to get the keys' successor */
//        System.out.println(">>>>>>>>Asking Closest Predessor to Find SUccessor<<<<<<<<");
        sendMessage(inPacket, closestPredecessor);
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

        if(packet != null && node!=null){
    //		System.out.println("Sending MSG; Method:" + packet.getMethod()
    //				+ ", Node IP:" + node.getIp() + ", Port:" + node.getPort());

            Executor bossPool = Executors.newCachedThreadPool();
            Executor workerPool = Executors.newCachedThreadPool();
            ChannelFactory channelFactory = new NioClientSocketChannelFactory(
                    bossPool, workerPool);
            final FileHandler fileHandler=new FileHandler(this);
            ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() throws Exception {
                    return Channels.pipeline(new CompatibleObjectEncoder(),
                            new CompatibleObjectDecoder(), fileHandler);
                }
            };
            ClientBootstrap clientBootstrap = new ClientBootstrap(channelFactory);
            clientBootstrap.setOption("connectTimeoutMillis",80000);
            clientBootstrap.setPipelineFactory(pipelineFactory);

            InetSocketAddress addressToConnectTo = new InetSocketAddress(
                    node.getIp(), node.getPort());
            ChannelFuture cf = clientBootstrap.connect(addressToConnectTo);
            final DropItPacket dropPacket = packet;

    //        System.out.println("!!!!!!Packet mtd: " + dropPacket.getMethod() + " " + dropPacket.toString());
    //        System.out.println("!!!!!!Sending to: " + node.getIp());

            try {
                cf.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future)
                            throws Exception {
                        // check to see if we succeeded
                        if (future.isSuccess()) {
                            Channel channel = future.getChannel();
                            channel.write(dropPacket);
                            // asynchronous
                            if(!dropPacket.getMethod().equalsIgnoreCase(Constants.REPLICATE.toString()))
                                channel.close();
                        }
                    }
                });
            } catch (Exception e) {
                // Handle Exception
            }
        }
	}

	public void sendMessage(DropItPacket packet, FileNode node,
			final SimpleChannelHandler handler) {
//		System.out.println("Sending MSG From Overloaded Method:"
//				+ packet.getMethod() + ", Node IP:" + node.getIp() + ", Port:"
//				+ node.getPort_ring());

		Executor bossPool = Executors.newCachedThreadPool();
		Executor workerPool = Executors.newCachedThreadPool();
		ChannelFactory channelFactory = new NioClientSocketChannelFactory(
				bossPool, workerPool);
		ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new CompatibleObjectEncoder(),
						new CompatibleObjectDecoder(), handler);
			}
		};
		ClientBootstrap clientBootstrap = new ClientBootstrap(channelFactory);
        clientBootstrap.setOption("connectTimeoutMillis", 80000);
		clientBootstrap.setPipelineFactory(pipelineFactory);

//		System.out.println("ring port line 314 " + node.getPort_ring());

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


	/**
	 * Check whether the successor is alive, and fixes predecessor if it has changed.
	 */
	public void stabilize() {

        /*First Check if my successor has failed. If so set the successor to the next in Successor list!
        * If succAlive is false -> successor has failed. Remove it.
        * Send a 'SUCCALIVE msg to successor. If successor is alive it will send a LIVESUCC msg. If a LIVESUCC comes
        * set succAlive to 'true'. Change succAlive to 'false' for the next iteration.*/

        if(!succAlive){
            successors.remove(0);
        }
        DropItPacket packet2 = new DropItPacket(Constants.SUCC_ALIVE.toString());
        sendMessage(packet2, getSuccessor());
        succAlive = false;

        /*Find my successors predecessor. If its me, then OK. If not set my successor to new Node and
        * notify him to set his predecessor as me.*/
        DropItPacket packet = new DropItPacket(Constants.GET_PREDECESSOR.toString());
        packet.setAttribute(Constants.REQ_NODE.toString(), node);
        sendMessage(packet, getSuccessor());

	}


    /**
	 * Called periodically. Checks whether the predecessor has failed.
	 */
	public void checkPredecessor() {

        if(!predAlive){
            predecessor = node;
        }
        DropItPacket packet = new DropItPacket(Constants.PRED_ALIVE.toString());
        packet.setAttribute(Constants.REQ_NODE.toString(), node);
        sendMessage(packet, predecessor);
        predAlive = false;
	}

	/**
	 * Called periodically. Refreshes the finger table entries.
	 * nextFingerToUpdate stores the index of the next finger to fix.
	 */
	public void fixFingers() {
		fixFinger(nextFingerToUpdate);
		nextFingerToUpdate = (nextFingerToUpdate + 1) % fingers.size();
        System.out.println("\n______________________________________________________________________________________");
        System.out.println("My Fingers " + node.getPort()
				+ " " + node.getKey().getHashId());
        for (int i = 0; i < fingers.size(); i++) {
			System.out.println("ME: " + node.getPort() + " Finger at " + i
					+ ", PORT: " + fingers.get(i).getPort() + ", KEY: "
					+ fingers.get(i).getKey().getHashId());
		}
        System.out.println("ME: " + node.getPort() + ", Successor: " + getSuccessor().getPort());
        System.out.println("ME: " + node.getPort() + ", Predecessor: " + getPredecessor().getPort());
        System.out.println("Next finger to update: " + nextFingerToUpdate);

	}

	public void fixFinger(int finger) {
		long base = node.getKey().getHashId();
//		long pow = 0x0000000000000001L << finger;
        long pow = (long)Math.pow(2.0f, finger);
		long id = base + pow;

		KeyId keyId = new KeyId(id);
//		System.out.println("" + node.getPort_ring() + ": "+ node.getKey().getHashId() + "FINGER: " + finger
//                + "------Finding node for KEY:" + id);
//		FileNode updatedFinger = findSuccessor(keyId);
        findSuccessorAndUpdateFinger(finger, keyId);
	}

    private void findSuccessorAndUpdateFinger(int finger, KeyId keyId) {
        FileNode updatedFinger = null;
        long givenKeyValue = keyId.getHashId();
//        System.out.println(">>>>>>>>INSIDE FIND AND UPDATE FINGERS: FINDING FOR KEY:"+ givenKeyValue +"<<<<<<<<");
        FileNode closestPredecessor = getClosestPredecessor(givenKeyValue);
        if (node.equals(closestPredecessor)) {
            updatedFinger = getSuccessor();
        }

        if(updatedFinger != null){
//            System.out.println("" + node.getPort_ring()
//				+ "------CHANGING FINGER at " + finger + " to "
//				+ updatedFinger.getPort_ring() + " "
//				+ updatedFinger.getKey().getHashId());
            setFinger(finger, updatedFinger);
        }else{
//            System.out.println("INSIDE FIX FINGERS AND UPDATE: ASKING FROM PREDECESSOR!: "+ closestPredecessor.getPort());
            DropItPacket outPacket = new DropItPacket(Constants.FND_SUSC_INT.toString());
            outPacket.setAttribute(Constants.KEY_ID.toString(), keyId);
            outPacket.setAttribute(Constants.FINGER.toString(), finger);
            outPacket.setAttribute(Constants.REQ_NODE.toString(), node);
            sendMessage(outPacket, closestPredecessor);
        }
    }

    public void setFinger(int i, FileNode n) {

        if(i != 0 && fingers.get(i).getKey().getHashId() != n.getKey().getHashId()){
            System.out.println("FileNode :" + node.getPort() + " changing finger at " + i +" to " + n.getPort() + " with key " + n.getKey().getHashId());
        }

		if (i < 0 || i >= fingers.size())
			return;
		if (i == 0) {
            setSuccessor(n);
//            return;
		}
		fingers.set(i, n);
	}

	public FileNode getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(FileNode predecessor) {
		this.predecessor = predecessor;
	}

	
	/**
	 * This method is called by the newly connected node to to the request server
	 */
	public int requestNodePosition(DropItPacket packet) {
		sendMessageToRequestServer(packet, Constants.REQUEST_SERVER_LIST[0], new RingSetupHandler(this));
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
        if(node!=null && node.getKey().getHashId() != successors.get(0).getKey().getHashId()){
            successors.add(0, node);

            // Remove the last successor to keep the succesor list to the size 3
            if(successors.size()>3)
                successors.remove(successors.size() - 1);
        }
	}

	public FileNode getNode() {
		return node;
	}
	
	public void sendMessageToRequestServer(DropItPacket packet, FileNode node,
			final SimpleChannelHandler handler) {
		
		Executor bossPool = Executors.newCachedThreadPool();
		Executor workerPool = Executors.newCachedThreadPool();
		ChannelFactory channelFactory = new NioClientSocketChannelFactory(
				bossPool, workerPool);
		ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new CompatibleObjectEncoder(),
						new CompatibleObjectDecoder(),
						handler);
			}
		};
		ClientBootstrap clientBootstrap = new ClientBootstrap(channelFactory);
		clientBootstrap.setPipelineFactory(pipelineFactory);

		System.out.println("ring port line 314 " + node.getPort());

		InetSocketAddress addressToConnectTo = new InetSocketAddress(
				node.getIp(), node.getPort());
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
}
