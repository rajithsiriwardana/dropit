package com.anghiari.dropit.fileserver;


import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;

/**
 * @author: sunimal
 */
public interface FileServerNode {
    public void bootServer(FileNode node);

    public  int findSuccessor(int hashVal);

    public void sendMessage(DropItPacket packet, FileNode node);

}
