package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.commons.*;
import com.anghiari.dropit.operations.*;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * User: amila
 * 
 * @author chinthaka316
 */

public class FileHandler extends SimpleChannelHandler {

    private FileServerNodeImpl handledNode;

    public FileHandler(FileServerNodeImpl node) {
        this.handledNode = node;
    }

    public FileHandler() {
    }

	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		DropItPacket pkt = (DropItPacket) e.getMessage();
		// TODO: Use data in pkt and send to required file

		String method = pkt.getMethod();

//		System.out.println("(((((((((((((((((method " + method +" : " + handledNode.getNode().getPort()+"))))))))))))))))))))))))");

		if (Constants.PING.toString().equalsIgnoreCase(method)) {
			// System.out.println("came here " + method);
			PongOperation pongOperation = new PongOperation(ctx, e);
			pongOperation.sendResponse();
			// respondToPing(ctx,e);
		} else if (Constants.PONG.toString().equalsIgnoreCase(method)) {

			// System.out.println("PONG received");
		} else if(Constants.GET_PREDECESSOR.toString().equalsIgnoreCase(method)){
            FileNode requester = (FileNode)pkt.getAttribute(Constants.REQ_NODE.toString());

            pkt.setMethod(Constants.RES_GET_PREDECESSOR.toString());
            pkt.setAttribute(Constants.FILE_NODE.toString(), handledNode.getPredecessor());

            handledNode.sendMessage(pkt, requester);

        } else if(Constants.RES_GET_PREDECESSOR.toString().equalsIgnoreCase(method)){
            FileNode succPredecessor = (FileNode)pkt.getAttribute(Constants.FILE_NODE.toString());
            FileNode myPredecessor = handledNode.getPredecessor();

            if(succPredecessor.getKey().getHashId() != myPredecessor.getKey().getHashId()){
                /*Successors' Predecessor has changed.*/
                handledNode.setSuccessor(succPredecessor);
                pkt.setMethod(Constants.SET_PREDECESSOR.toString());
                /*Notify my new successor to set its predecessor as me*/
                pkt.setAttribute(Constants.FILE_NODE.toString(), handledNode.getNode());
                handledNode.sendMessage(pkt, succPredecessor);
            }

        } else if (Constants.SET_PREDECESSOR.toString().equalsIgnoreCase(method)) {
            FileNode newPredecessor = (FileNode)pkt.getAttribute(Constants.FILE_NODE.toString());
            if(newPredecessor!=null){
                handledNode.setPredecessor(newPredecessor);
            }
        } else if(Constants.SUCC_ALIVE.toString().equalsIgnoreCase(method)){
            FileNode requester = (FileNode)pkt.getAttribute(Constants.REQ_NODE.toString());
            pkt.setMethod(Constants.LIVE_SUCC.toString());

            handledNode.sendMessage(pkt, requester);
        } else if(Constants.LIVE_SUCC.toString().equalsIgnoreCase(method)){
            /*Successor is alive!*/
            handledNode.succAlive = true;
        } else if(Constants.PRED_ALIVE.toString().equalsIgnoreCase(method)){
            FileNode requester = (FileNode)pkt.getAttribute(Constants.REQ_NODE.toString());
            pkt.setMethod(Constants.LIVE_PRED.toString());
            handledNode.sendMessage(pkt, requester);
        } else if(Constants.LIVE_PRED.toString().equalsIgnoreCase(method)){
            handledNode.predAlive = true;
        } else if (Constants.STORE.toString().equalsIgnoreCase(method)) {
            String fileName = (String)pkt.getAttribute(Constants.FILE_NAME.toString());
            System.out.println("\n___________________________________________________________________________________");
            System.out.println("Node " + handledNode.getNode().getPort() + " trying to store file " + fileName);
            byte[] fileByteArray = pkt.getData();
            /*Create another copy of bytearray to send for replication*/
//            byte[] clone = new byte[fileByteArray.length];
//            System.arraycopy(fileByteArray, 0, clone, 0, fileByteArray.length);

			// Modify path with the folder to save files
			File file = new File(Configurations.FOLDER_PATH+handledNode.getNode().getPort()+"/" + fileName);
			if (!file.exists()) {
//				file.createNewFile();
                file.getParentFile().mkdirs();
			}
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(fileByteArray);

            System.out.println("Node " + handledNode.getNode().getPort() + " Stored file " + fileName);
            /*Replicating in Successors*/
            FileNode succ = handledNode.getSuccessor();
//            pkt.setData(clone);
            if(succ!=null){
//                System.out.println("Sending to replicate!");
                pkt.setMethod(Constants.REPLICATE.toString());
                handledNode.sendMessage(pkt, succ);
            }

			AckStoreOperation ackStoreOperation = new AckStoreOperation(ctx, e, file.getName());
			ackStoreOperation.sendResponse();
        } else if (Constants.REPLICATE.toString().equalsIgnoreCase(method)){
            String fileName = (String)pkt.getAttribute(Constants.FILE_NAME.toString());
            System.out.println("Node " + handledNode.getNode().getPort() +" Replicating file " + fileName);
            byte[] fileByteArray = pkt.getData();
            // Modify path with the folder to save files
            File file = new File(Configurations.FOLDER_PATH+handledNode.getNode().getPort()+"/" + fileName);
            if (!file.exists()) {
//                file.createNewFile();
                file.getParentFile().mkdirs();
            }
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(fileByteArray);
		} else if (Constants.RETRIEVE.toString().equalsIgnoreCase(method)) {
            System.out.println("\n___________________________________________________________________________________");
            System.out.println("Retrieveing file "+pkt.getAttribute(Constants.FILE_NAME.toString()) + "!");
            File file = new File(Configurations.FOLDER_PATH+handledNode.getNode().getPort()+"/" +  pkt.getAttribute(Constants.FILE_NAME
					.toString()));
            byte[] filedata = new byte[(int) file.length()];
            if(file.exists()){
                // Modify path with the folder to save files
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(
                        fileInputStream);
                bufferedInputStream.read(filedata, 0, filedata.length);
                bufferedInputStream.close();
            }
            System.out.println("Sending file!");
            TransferOperation transferOperation = new TransferOperation(ctx, e,
					filedata, file.getName());
			transferOperation.sendResponse();
		} else if (Constants.REQ_JOIN_NODE.toString().equalsIgnoreCase(method)) {
			new ReqJoinNodeOperation(pkt).sendResponse();
		} else if (Constants.RES_JOIN_NODE.toString().equalsIgnoreCase(method)) {
			new ResJoinNodeOperation(pkt).sendRequest();
		} else if (Constants.REQ_JOIN_FINAL.toString().equalsIgnoreCase(method)) {
			new ReqJoinFinalOperation(pkt).sendResponse();
		} else if (Constants.SET_SUCCESSOR.toString().equalsIgnoreCase(method)) {
			new SetSuccOperation(pkt).sendResponse();
		} else if (Constants.DELETE.toString().equalsIgnoreCase(method)) {
            File file = new File(Configurations.FOLDER_PATH+handledNode.getNode().getPort()+"/" +  pkt.getAttribute(Constants.FILE_NAME
                    .toString()));
            if(file.exists()){
                if(file.delete()){
                    new AckDeleteOperation(ctx,e).sendResponse();
                }
            }

        }else if(Constants.FND_SUSC.toString().equalsIgnoreCase(method)){
            FileNode r = (FileNode)pkt.getAttribute(Constants.REQ_NODE.toString());
            long key = ((KeyId)pkt.getAttribute(Constants.KEY_ID.toString())).getHashId();
//            System.out.println("\n___________________________________________________________________________________");
            System.out.println("Node " + handledNode.getNode().getPort() +" Finding Successor for Key: "+ key +", Requester: " + r.getIp() +":" + r.getPort());
            FindSuccessorOperation findOperation = new FindSuccessorOperation(handledNode, ctx, e, pkt);
            findOperation.sendResponse(method);
//            DropItPacket packet = new DropItPacket(Constants.RES_SUSC.toString());
//            handledNode.sendMessage(packet, r);
        }else if(Constants.RES_SUSC.toString().equalsIgnoreCase(method)){
//            System.out.println("==========FIND SUCC REPLY CAME===========");
        }else if(Constants.FND_SUSC_INT.toString().equalsIgnoreCase(method)){
//            System.out.println("==========CAME TO FIND SUCC INT===========");
            FindSuccessorOperation findOperation = new FindSuccessorOperation(handledNode, ctx, e, pkt);
            findOperation.sendResponse(method);
        }else if(Constants.RES_SUSC_INT.toString().equalsIgnoreCase(method)){
//            System.out.println("==========FIND SUCC INT REPLY CAME===========");
            FileNode newFinger = (FileNode)pkt.getAttribute(Constants.FILE_NODE.toString());
            int finger = ((Integer)pkt.getAttribute(Constants.FINGER.toString())).intValue();
            if(newFinger != null){
                handledNode.setFinger(finger, newFinger);
            }
        }



    super.messageReceived(ctx, e);
//	e.getChannel().close();
    }

}
