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

    public FindSuccessorOperation(FileServerNode fileServerNode, ChannelHandlerContext channelHandlerContext,MessageEvent evt, DropItPacket incomingPacket) {
        this.fileServer = fileServerNode;
        ctx=channelHandlerContext;
        e=evt;
        packet = incomingPacket;
    }

//    public void sendResponse(String method){
//
//        KeyId keyId = (KeyId)packet.getAttribute(Constants.KEY_ID.toString());
//        System.out.println(">>>>>>>>>>>>>>>>>>>FINDING SUCCESSOR FOR KEY: " + keyId.getHashId() +" <<<<<<<<<<<<<<<<<<<<<<");
//        FileNode node = this.fileServer.findSuccessor(packet);
//
//        if(node != null){
//            System.out.println(">>>>>>>>>>>>>>>>>>>FOUND SUCCESSOR FOR KEY: " + keyId.getHashId() + "NODE:" + node.getPort_ring()+ " KEY: "+ node.getKey().getHashId() +" <<<<<<<<<<<<<<<<<<<<<<");
//            DropItPacket outPacket;
//
//            if(Constants.FND_SUSC.toString().equalsIgnoreCase(method)){
//                outPacket = new DropItPacket(Constants.RES_SUSC.toString());
//
//            }
//            else{
//                outPacket = new DropItPacket(Constants.RES_SUSC_INT.toString());
//                int finger = ((Integer)packet.getAttribute(Constants.FINGER.toString())).intValue();
//                outPacket.setAttribute(Constants.FINGER.toString(), finger);
//            }
//            outPacket.setAttribute(Constants.FILE_NODE.toString(), node);
//            FileNode requester = (FileNode)packet.getAttribute(Constants.REQ_NODE.toString());
//
//
//            outPacket.setAttribute(Constants.REQ_NODE.toString(), requester);
////            FileNode requester = (FileNode)packet.getAttribute(Constants.REQ_NODE.toString());
//            System.out.println("************SENDING REPLY FOR KEY:" + keyId.getHashId() +" TO: " + requester.getPort() + "************");
//            System.out.println(outPacket.getMethod());
//            fileServer.sendMessage(outPacket,requester);
//        }

        public void sendResponse(String method){

            KeyId keyId = (KeyId)packet.getAttribute(Constants.KEY_ID.toString());
            System.out.println(">>>>>>>>>>>>>>>>>>>FINDING SUCCESSOR FOR KEY: " + keyId.getHashId() +" <<<<<<<<<<<<<<<<<<<<<<");
            FileNode node = this.fileServer.findSuccessor(packet);

            if(node != null){
                System.out.println(">>>>>>>>>>>>>>>>>>>FOUND SUCCESSOR FOR KEY: " + keyId.getHashId() + "NODE:" + node.getPort_ring()+ " KEY: "+ node.getKey().getHashId() +" <<<<<<<<<<<<<<<<<<<<<<");
//                DropItPacket outPacket;


                if(Constants.FND_SUSC.toString().equalsIgnoreCase(method)){
                    packet.setMethod(Constants.RES_SUSC.toString());

                }
                else{
                    packet.setMethod(Constants.RES_SUSC_INT.toString());
//                    int finger = ((Integer)packet.getAttribute(Constants.FINGER.toString())).intValue();
//                    outPacket.setAttribute(Constants.FINGER.toString(), finger);
                }
                packet.setAttribute(Constants.FILE_NODE.toString(), node);
                FileNode requester = (FileNode)packet.getAttribute(Constants.REQ_NODE.toString());


//                outPacket.setAttribute(Constants.REQ_NODE.toString(), requester);
//            FileNode requester = (FileNode)packet.getAttribute(Constants.REQ_NODE.toString());
                System.out.println("************SENDING REPLY FOR KEY:" + keyId.getHashId() +" TO: " + requester.getPort() + "************");
                System.out.println(packet.getMethod());
                fileServer.sendMessage(packet,requester);
            }

        }

}
