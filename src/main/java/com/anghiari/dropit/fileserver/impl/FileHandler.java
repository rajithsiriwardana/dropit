package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.operations.AckStoreOperation;
import com.anghiari.dropit.operations.PongOperation;
import com.anghiari.dropit.operations.ReqJoinNodeOperation;
import com.anghiari.dropit.operations.ResJoinNodeOperation;
import com.anghiari.dropit.operations.TransferOperation;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
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

//		System.out.println("method " + method);

		if (Constants.PING.toString().equalsIgnoreCase(method)) {
//			System.out.println("came here " + method);
			PongOperation pongOperation = new PongOperation(ctx, e);
			pongOperation.sendResponse();
			// respondToPing(ctx,e);
		} else if (Constants.PONG.toString().equalsIgnoreCase(method)) {

//			System.out.println("PONG received");
		} else if (Constants.RES_GET_FILENODE.toString().equalsIgnoreCase(
				method)) {
			// call the response method for node position request here.
			new NodeConnectionHandler().handleResponse((String[]) pkt
					.getAttribute(Constants.KEY_ID.toString()));
		}
        else if(Constants.STORE.toString().equalsIgnoreCase(method)){
            byte[] fileByteArray=pkt.getData();
            //Modify path with the folder to save files
            File file = new File((String)pkt.getAttribute(Constants.FILE_NAME.toString()));
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(fileByteArray);
            AckStoreOperation ackStoreOperation=new AckStoreOperation(ctx,e);
            ackStoreOperation.sendResponse();
        }
        else if(Constants.RETRIEVE.toString().equalsIgnoreCase(method)){
            File file = new File((String)pkt.getAttribute(Constants.FILE_NAME.toString()));
            byte[] filedata = new byte[(int) file.length()];
            //Modify path with the folder to save files
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(filedata, 0, filedata.length);
            TransferOperation transferOperation=new TransferOperation(ctx,e);
            transferOperation.sendResponse();
        }else if(Constants.REQ_JOIN_NODE.toString().equalsIgnoreCase(method)){
        	new ReqJoinNodeOperation(pkt).sendResponse();
        }else if (Constants.RES_JOIN_NODE.toString().equalsIgnoreCase(method)){
        	new ResJoinNodeOperation(pkt).sendRequest();
        }
		super.messageReceived(ctx, e);
	}
}
