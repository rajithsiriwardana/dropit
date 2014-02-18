package com.anghiari.dropit.execute;

import com.anghiari.dropit.commons.RequestServerConfigLoader;
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
//        if (args.length != 3) {
//            System.err.println("Usage: " +
//                    RequestServerRunner.class.getSimpleName() + " <port> <nb of connections>");
//            return;
//        }
        String ip;
        int port, nbconn;
        if (args.length == 3) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
            nbconn = Integer.parseInt(args[2]);
        } else {
            ip = RequestServerConfigLoader.getRSServerIP();
            port = RequestServerConfigLoader.getRSServerSocket();
            nbconn = 1;
        }
        // Parse options.


        ArrayList<InetSocketAddress> rsList1 = new ArrayList<InetSocketAddress>();
        ArrayList<InetSocketAddress> rsList2 = new ArrayList<InetSocketAddress>();
        ArrayList<InetSocketAddress> rsList3 = new ArrayList<InetSocketAddress>();
        ArrayList<InetSocketAddress> rsList4 = new ArrayList<InetSocketAddress>();


//        rsList1.add(new InetSocketAddress("192.248.8.241", 8006));
//        rsList1.add(new InetSocketAddress("127.0.0.1", port + 21));
//        rsList1.add(new InetSocketAddress("127.0.0.1", port + 11));
//        rsList2.add(new InetSocketAddress("127.0.0.1", port + 1));
//        rsList2.add(new InetSocketAddress("127.0.0.1", port + 21));
//        rsList3.add(new InetSocketAddress("127.0.0.1", port + 1));
//        rsList3.add(new InetSocketAddress("127.0.0.1", port + 11));

        RequestNode node = new RequestNodeImpl();
        node.setActiveRSList(RequestServerConfigLoader.getRSList());
        node.start(ip, port, nbconn);
/*
        RequestNode node2 = new RequestNodeImpl();
        node2.setActiveRSList(rsList2);
        node2.start(ip, port + 10, nbconn);

        RequestNode node3 = new RequestNodeImpl();
        node3.setActiveRSList(rsList3);
        node3.start(ip, port + 20, nbconn);*/
    }
}
