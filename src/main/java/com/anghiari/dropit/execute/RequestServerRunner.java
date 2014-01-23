package com.anghiari.dropit.execute;

import com.anghiari.dropit.requestserver.RequestNode;
import com.anghiari.dropit.requestserver.RequestNodeImpl;

import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * @author rajith
 * @version ${Revision}
 */
public class RequestServerRunner {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: " +
                    RequestServerRunner.class.getSimpleName() + " <port> <nb of connections>");
            return;
        }
        // Parse options.
        int port = Integer.parseInt(args[0]);
        int nbconn = Integer.parseInt(args[1]);

//        ArrayList<InetSocketAddress> rsList1 = new ArrayList<InetSocketAddress>();
//        for(int i=0;i<5;++i){
//            rsList1.add(new InetSocketAddress("127.0.0.1",port + i));
//        }
//        for(int i=0;i<5;++i){
//            RequestNode node = new RequestNodeImpl();
//            node.start(port + i, nbconn);
//            node.setActiveRSList(rsList1);
//        }
        ArrayList<InetSocketAddress> rsList1 = new ArrayList<InetSocketAddress>();
        ArrayList<InetSocketAddress> rsList2 = new ArrayList<InetSocketAddress>();
        rsList1.add(new InetSocketAddress("127.0.0.1", port + 10));
        rsList2.add(new InetSocketAddress("127.0.0.1", port));
        RequestNode node = new RequestNodeImpl();
        node.start(port, nbconn);
        node.setActiveRSList(rsList1);
        RequestNode node2 = new RequestNodeImpl();
        node2.start(port + 10, nbconn);
        node2.setActiveRSList(rsList2);
    }
}
