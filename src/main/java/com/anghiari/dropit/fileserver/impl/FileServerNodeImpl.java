package com.anghiari.dropit.fileserver.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import com.anghiari.dropit.fileserver.FileServerNode;

/**
 * @author: sunimal
 */
public class FileServerNodeImpl implements FileServerNode{

	private ServerBootstrap bootstrap;

    private FileNode node;
    private FileNode predecessor;
    private ArrayList<FileNode> successors;
    private ArrayList<FileNode> fingers;
	
    public void bootServer(FileNode node) {
    	this.node = node;
    	successors = new ArrayList<FileNode>();
    	successors.add(FileNodeList.getFileNodeList().get(1));
    	
        //setup the server
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),//ObjectDecoder might not work if the client side is not using netty ObjectDecoder for decoding.
                        new ObjectEncoder(),
                        new FileHandler()
                );
            };
        });

        // Bind and start to accept incoming connections.
        
        Channel acceptor = this.bootstrap.bind(new InetSocketAddress(node.getIp(), node.getPort()));
        if (acceptor.isBound()) {
            System.err.println("+++ SERVER - bound to *: "+node.getPort());

        } else {
            System.err.println("+++ SERVER - Failed to bind to *: "+node.getPort());
            this.bootstrap.releaseExternalResources();
        }
    }
    
    
    /*
     * (non-Javadoc)
     * @see com.anghiari.dropit.fileserver.FileServerNode#findSuccessor(int)
     * Each node has a findSuccessor method, return ip of the fileserver which has the file
     */
	public int findSuccessor(int hashVal) {
		// TODO Auto-generated method stub
		
		return 0;
	}

    public FileNode findSuccessor(KeyId key){
        /*If my predecessor key > key ==> ask my predecessor to find keys' successor
        * If my key > key > my predecessor key ==> i'm the successor
        * If key > my key ==> ask my successor to find keys' successor*/
        long givenKeyValue = key.getHashId();
        long predecessorKeyValue = retrieveKeyValue(predecessor);
        long myKeyValue = node.getKey().getHashId();

        if(predecessorKeyValue > givenKeyValue){
            //TODO: Use dropit protocol to call findSuccessor of my predecessor and return the response
        }
        else if (myKeyValue > givenKeyValue){
            return node;
        }
        else{
            //TODO: Use dropit protocol to call findSuccessor of my immediate successor and return the response
        }
        return null;
    }

    private long retrieveKeyValue(FileNode predecessor) {

        //TODO: Use dropit protocol to get the ket of given file node
        return 0;
    }

    private void sendMessage(DropItPacket packet, FileNode node){
        System.out.println("Sending MSG; Method:" + packet.getMethod()+ ", Node IP:"+ node.getIp() +", Port:" + node.getPort());

        Executor bossPool = Executors.newCachedThreadPool();
        Executor workerPool = Executors.newCachedThreadPool();
        ChannelFactory channelFactory = new NioClientSocketChannelFactory(bossPool, workerPool);
        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),//ObjectDecoder might not work if the client side is not using netty ObjectDecoder for decoding.
                        new FileHandler());
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

    private void pingSuccessor(){
        DropItPacket dropItPacket = new DropItPacket(Constants.PING.name());
        sendMessage(dropItPacket, successors.get(0));
    }

    /**
	 * Checking whether the successor is alive
	 * @param ip
	 * @param port
	 */
	public void pingSuccessor(String ip, int port){
		
		System.out.println("PING to Successor " + ip+" "+port );

        DropItPacket dropItPacket = new DropItPacket(Constants.PING.name());
        sendMessage(dropItPacket, new FileNode(ip, port));
		
	}

	

}
