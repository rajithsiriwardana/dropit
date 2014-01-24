package com.anghiari.dropit.requestserver;

import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * @author rajith
 * @version ${Revision}
 */
public interface RequestNode {

    public void start(String ip, int port, int nbconn);

    public void setActiveRSList(ArrayList<InetSocketAddress> activeRSList);
}
