package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.commons.KeyId;
import com.anghiari.dropit.fileserver.FileServerNode;
import org.jboss.netty.channel.*;

/**
 * User: amila
 */
public class FindSuccessorOperation extends AbstractOperation{

    public FindSuccessorOperation(ChannelHandlerContext channelHandlerContext,MessageEvent evt, DropItPacket incomingPacket) {
        ctx=channelHandlerContext;
        e=evt;
        packet = incomingPacket;
    }

    public void sendResponse(String method){

        KeyId keyId = (KeyId)packet.getAttribute(Constants.KEY_ID.toString());
        System.out.println(">>>>>>>>>>>>>>>>>>>FINDING SUCCESSOR FOR KEY: " + keyId.getHashId() +" <<<<<<<<<<<<<<<<<<<<<<");
        FileNode node = this.fileServer.findSuccessor(packet);

        if(node != null){
            System.out.println(">>>>>>>>>>>>>>>>>>>FOUND SUCCESSOR FOR KEY: " + keyId.getHashId() + "NODE:" + node.getPort_ring()+ " KEY: "+ node.getKey().getHashId() +" <<<<<<<<<<<<<<<<<<<<<<");
            DropItPacket outPacket;

            if(Constants.FND_SUSC.toString().equalsIgnoreCase(method)){
                outPacket = new DropItPacket(Constants.RES_SUSC.toString());
            }
            else{
                outPacket = new DropItPacket(Constants.RES_SUSC_INT.toString());
            }
            outPacket.setAttribute(Constants.FILE_NODE.toString(), node);
            FileNode requester = (FileNode)packet.getAttribute(Constants.REQ_NODE.toString());
            fileServer.sendMessage(this.packet,requester);
        }

    }

}
