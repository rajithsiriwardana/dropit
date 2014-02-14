package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.commons.Configurations;
import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.operations.*;

import org.jboss.netty.channel.*;

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

		// System.out.println("method " + method);

		if (Constants.PING.toString().equalsIgnoreCase(method)) {
			// System.out.println("came here " + method);
			PongOperation pongOperation = new PongOperation(ctx, e);
			pongOperation.sendResponse();
			// respondToPing(ctx,e);
		} else if (Constants.PONG.toString().equalsIgnoreCase(method)) {

			// System.out.println("PONG received");
		} else if (Constants.STORE.toString().equalsIgnoreCase(method)) {
			byte[] fileByteArray = pkt.getData();
			// Modify path with the folder to save files
			File file = new File(Configurations.FOLDER_PATH + pkt.getAttribute(Constants.FILE_NAME
					.toString()));
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(fileByteArray);
			AckStoreOperation ackStoreOperation = new AckStoreOperation(ctx, e, file.getName());
			ackStoreOperation.sendResponse();
		} else if (Constants.RETRIEVE.toString().equalsIgnoreCase(method)) {
			File file = new File(Configurations.FOLDER_PATH +  pkt.getAttribute(Constants.FILE_NAME
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
			TransferOperation transferOperation = new TransferOperation(ctx, e,
					filedata, file.getName());
			transferOperation.sendResponse();
		} else if (Constants.REQ_JOIN_NODE.toString().equalsIgnoreCase(method)) {
			new ReqJoinNodeOperation(pkt).sendResponse();
		} else if (Constants.RES_JOIN_NODE.toString().equalsIgnoreCase(method)) {
			new ResJoinNodeOperation(pkt).sendRequest();
		} else if (Constants.REQ_JOIN_FINAL.toString().equalsIgnoreCase(method)) {
			new ReqJoinFinalOperation(pkt).sendResponse();
		} else if (Constants.SET_PREDECESSOR.toString()
				.equalsIgnoreCase(method)) {
			new SetPredOperation(pkt).sendResponse();
		} else if (Constants.SET_SUCCESSOR.toString().equalsIgnoreCase(method)) {
			new SetSuccOperation(pkt).sendResponse();
		} else if (Constants.DELETE.toString().equalsIgnoreCase(method)) {
            File file = new File(Configurations.FOLDER_PATH +  pkt.getAttribute(Constants.FILE_NAME
                    .toString()));
            if(file.exists()){
                if(file.delete()){
                    new AckDeleteOperation(ctx,e).sendResponse();
                }
            }

        }else if(Constants.FND_SUSC.toString().equalsIgnoreCase(method)){
            System.out.println("==========CAME TO FING SUCC===========");
            FindSuccessorOperation findOperation = new FindSuccessorOperation(ctx, e, pkt);
            findOperation.sendResponse();
        }else if(Constants.RES_SUSC.toString().equalsIgnoreCase(method)){
            System.out.println("==========FIND SUCC REPLY CAME===========");


        }else if(Constants.FND_SUSC_INT.toString().equalsIgnoreCase(method)){

        }else if(Constants.RES_SUSC_INT.toString().equalsIgnoreCase(method)){

        }
        super.messageReceived(ctx, e);
	}


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {

        //handling successor
        handledNode.getSuccessors().remove(0);
//    	System.out.println("Reset the successor list, new size is" + handledNode.getSuccessors().size());

        System.out.println("Connection exception, server might be down");
        Channels.close(e.getChannel());
    }
}