package com.anghiari.dropit.fileserver;



/**
 * @author: sunimal
 */
public interface FileServerNode {
    public void bootServer(String ip, int port);

    public  int findSuccessor(int hashVal);

}
