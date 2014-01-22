package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.fileserver.FileServerNode;

/**
 * @author: sunimal
 */
public class PingOperation extends AbstractOperation{


    public PingOperation(FileServerNode fileServerNode, String remoteIP, int remotePort) {
        fileServer=fileServerNode;
        packet = new DropItPacket(Constants.PING.name());
        ip=remoteIP;
        port=remotePort;
    }

}
