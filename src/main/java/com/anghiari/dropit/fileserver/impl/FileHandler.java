package com.anghiari.dropit.fileserver.impl;


import com.anghiari.dropit.commons.DropItPacket;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: amila
 * Date: 1/20/14
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */

public class FileHandler extends SimpleChannelHandler {
    public void messageReceived(ChannelHandlerContext ctx,MessageEvent e) throws Exception {
        DropItPacket pkt = (DropItPacket)e.getMessage();
        //TODO: Use data in pkt and send to required file
        super.messageReceived(ctx, e);
    }
}

