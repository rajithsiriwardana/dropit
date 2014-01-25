package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.fileserver.FileServerNode;

/**
 * 
 * @author chinthaka316
 * initial call to the request server
 */
public class ReqServerForNodeOperation extends AbstractOperation{

    public ReqServerForNodeOperation(FileServerNode fileServerNode) {
        fileServer=fileServerNode;
        packet = new DropItPacket(Constants.GET_FILENODE.toString());
    }
    
	public void sendRequest(){
		
		this.fileServer.requestNodePosition(packet);
	}
	
}
