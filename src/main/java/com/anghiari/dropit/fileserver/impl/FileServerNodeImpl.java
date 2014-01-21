package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.fileserver.FileServerNode;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author: sunimal
 */
public class FileServerNodeImpl implements FileServerNode{

	private ServerBootstrap bootstrap;
	
    public void bootServer(String ip, int port) {
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
                        new FileHandler()
                );
            };
        });

        // Bind and start to accept incoming connections.
        
        Channel acceptor = this.bootstrap.bind(new InetSocketAddress(ip, port));
        if (acceptor.isBound()) {
            System.err.println("+++ SERVER - bound to *:12345");

        } else {
            System.err.println("+++ SERVER - Failed to bind to *:12345");
            this.bootstrap.releaseExternalResources();
        }
    }


}
