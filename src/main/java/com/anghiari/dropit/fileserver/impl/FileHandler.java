package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.commons.*;
import com.anghiari.dropit.operations.*;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.io.*;

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

	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		DropItPacket pkt = (DropItPacket) e.getMessage();
		// TODO: Use data in pkt and send to required file

		String method = pkt.getMethod();
        Constants mtd = Constants.valueOf(method);

        switch (mtd){
            case PING: {
                break;
            }
            case PONG: {
                System.out.println("Node: " + handledNode.getNode().getPort() + " Pong received!"); break;
            }
            case GET_PREDECESSOR: {
                FileNode requester = (FileNode)pkt.getAttribute(Constants.REQ_NODE.toString());

                pkt.setMethod(Constants.RES_GET_PREDECESSOR.toString());
                pkt.setAttribute(Constants.FILE_NODE.toString(), handledNode.getPredecessor());

                handledNode.sendMessage(pkt, requester);

                break;
            }
            case RES_GET_PREDECESSOR: {
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
                break;
            }
            case SET_PREDECESSOR: {
                FileNode newPredecessor = (FileNode)pkt.getAttribute(Constants.FILE_NODE.toString());
                if(newPredecessor!=null){
                    handledNode.setPredecessor(newPredecessor);
                }
                break;
            }
            case SUCC_ALIVE: {

                FileNode requester = (FileNode)pkt.getAttribute(Constants.REQ_NODE.toString());
//                System.out.println("------- Node " + handledNode.getNode().getPort());
//                System.out.println(" received SuccAlive from "  + requester.getPort());
                pkt.setMethod(Constants.LIVE_SUCC.toString());
                pkt.setAttribute(Constants.FILE_NODE.toString(), handledNode.getNode());

                handledNode.sendMessage(pkt, requester);

                break;
            }
            case LIVE_SUCC: {
                /*Successor is alive!*/
                handledNode.succAlive = true;
//                System.out.println("------ Node " + handledNode.getNode().getPort() + " got reply from " +
//                        ((FileNode)pkt.getAttribute(Constants.FILE_NODE.toString())).getPort());
                break;
            }
            case PRED_ALIVE: {
                FileNode requester = (FileNode)pkt.getAttribute(Constants.REQ_NODE.toString());
                pkt.setMethod(Constants.LIVE_PRED.toString());
                handledNode.sendMessage(pkt, requester);
                break;
            }
            case LIVE_PRED: {
                handledNode.predAlive = true;
                break;
            }
            case FND_SUSC_INT:{
                FindSuccessorOperation findOperation = new FindSuccessorOperation(handledNode, ctx, e, pkt);
                findOperation.sendResponse(method);
                break;
            }
            case RES_SUSC_INT:{
                FileNode newFinger = (FileNode)pkt.getAttribute(Constants.FILE_NODE.toString());
                int finger = ((Integer)pkt.getAttribute(Constants.FINGER.toString())).intValue();
                if(newFinger != null){
                    handledNode.setFinger(finger, newFinger);
                }
                break;
            }
            case FND_SUSC:{
                FileNode r = (FileNode)pkt.getAttribute(Constants.REQ_NODE.toString());
                long key = ((KeyId)pkt.getAttribute(Constants.KEY_ID.toString())).getHashId();
//                System.out.println("\n___________________________________________________________________________________");
                System.out.println("Node " + handledNode.getNode().getPort() +" Finding Successor for Key: "+ key +", Requester: " + r.getIp() +":" + r.getPort());
                FindSuccessorOperation findOperation = new FindSuccessorOperation(handledNode, ctx, e, pkt);
                findOperation.sendResponse(method);
                break;
            }
            case STORE: {
                storeFile(ctx, e, pkt);
                break;
            }
            case REPLICATE: {
                replicate(pkt);
                break;
            }
            case RETRIEVE: {
                retrieve(ctx, e, pkt);
                break;
            }
            case REQ_JOIN_NODE: {
                new ReqJoinNodeOperation(pkt).sendResponse();
                break;
            }
            case RES_JOIN_NODE: {
                new ResJoinNodeOperation(pkt).sendRequest();
                break;
            }
            case REQ_JOIN_FINAL: {
                new ReqJoinFinalOperation(pkt).sendResponse();
                break;
            }
            case SET_SUCCESSOR: {
                new SetSuccOperation(pkt).sendResponse();
                break;
            }
            case DELETE: {
                File file = new File(Configurations.FOLDER_PATH+handledNode.getNode().getPort()+"/" +  pkt.getAttribute(Constants.FILE_NAME
                        .toString()));
                if(file.exists()){
                    if(file.delete()){
                        new AckDeleteOperation(ctx,e).sendResponse();
                    }
                }
                break;
            }
        }

        super.messageReceived(ctx, e);
    //	e.getChannel().close();
    }

    private void retrieve(ChannelHandlerContext ctx, MessageEvent e, DropItPacket pkt) throws IOException {
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
        TransferOperation transferOperation = new TransferOperation(ctx, e, filedata, file.getName());
        transferOperation.sendResponse();

        System.out.println("File " + file.getName() + " sent to " + e.getRemoteAddress().toString());
    }

    private void replicate(DropItPacket pkt) {
        String fileName = (String)pkt.getAttribute(Constants.FILE_NAME.toString());
        byte[] fileByteArray = pkt.getData();
        try{
            saveFile(fileByteArray, fileName);
            System.out.println("Node " + handledNode.getNode().getPort() +" Replicated file " + fileName);
        }catch (IOException e){
            System.err.println("FileServer " + handledNode.getNode().getPort() + " failed to save file " + fileName);
        }
    }

    private void storeFile(ChannelHandlerContext ctx, MessageEvent e, DropItPacket pkt){
        String fileName = (String)pkt.getAttribute(Constants.FILE_NAME.toString());
        System.out.println("\n___________________________________________________________________________________");
        System.out.println("Node " + handledNode.getNode().getPort() + " trying to store file " + fileName);
        byte[] fileByteArray = pkt.getData();

        try{
            saveFile(fileByteArray, fileName);
            System.out.println("Node " + handledNode.getNode().getPort() + " Stored file " + fileName);
        }catch (IOException err){
            System.err.println("FileServer " + handledNode.getNode().getPort() + " failed to save file " + fileName);
        }
        /*Replicating in Successors*/
        FileNode succ = handledNode.getSuccessor();
//            pkt.setData(clone);
        if(succ!=null){
//                System.out.println("Sending to replicate!");
            pkt.setMethod(Constants.REPLICATE.toString());
            handledNode.sendMessage(pkt, succ);
        }

        /*Sending success response to the client!*/
        AckStoreOperation ackStoreOperation = new AckStoreOperation(ctx, e, fileName);
        ackStoreOperation.sendResponse();
    }

    private void saveFile(byte[] fileByteArray, String fileName) throws IOException{
        File file = new File(Configurations.FOLDER_PATH+handledNode.getNode().getPort()+"/" + fileName);
        if (!file.exists()) {
//                file.createNewFile();
            file.getParentFile().mkdirs();
        }
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(fileByteArray);
    }

}
