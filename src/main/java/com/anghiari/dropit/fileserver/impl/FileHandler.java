package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.commons.Configurations;
import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
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
		} else if (Constants.RES_GET_FILENODE.toString().equalsIgnoreCase(
				method)) {
			// call the response method for node position request here.
			new ResGetFilenodeOperation(pkt).sendRequest();
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

        }

		super.messageReceived(ctx, e);
	}
}
