package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.commons.FileNodeList;
import com.anghiari.dropit.fileserver.FileServerNode;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author: sunimal
 */
public class FileServerNodeImpl implements FileServerNode{

	private ServerBootstrap bootstrap;
	
	private ArrayList<FileNode> successorList;
	
    public void bootServer(String ip, int port) {
    	
    	successorList = new ArrayList<FileNode>();
    	successorList.add(FileNodeList.getFileNodeList().get(1));
    	
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
        
        Channel acceptor = this.bootstrap.bind(new InetSocketAddress(ip, port));
        if (acceptor.isBound()) {
            System.err.println("+++ SERVER - bound to *: "+port);

        } else {
            System.err.println("+++ SERVER - Failed to bind to *: "+port);
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
	
	/**
	 * Checking whether the successor is alive
	 * @param ip
	 * @param port
	 */
	public void pingSuccessor(String ip, int port){
		
		System.out.println("PING to Successor " + ip+" "+port );
	
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

		InetSocketAddress addressToConnectTo = new InetSocketAddress(ip, port);
		ChannelFuture cf = clientBootstrap.connect(addressToConnectTo);

		final DropItPacket dropPacket = new DropItPacket(Constants.PING.name());
		
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
		
//			Channel channel = cf.getChannel();
//			channel.write(dropPacket);
		
	}

	

}
