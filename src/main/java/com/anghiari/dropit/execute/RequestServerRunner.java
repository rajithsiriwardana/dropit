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
        if (args.length != 3) {
            System.err.println("Usage: " +
                    RequestServerRunner.class.getSimpleName() + " <port> <nb of connections>");
            return;
        }
        // Parse options.
        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        int nbconn = Integer.parseInt(args[2]);

        ArrayList<InetSocketAddress> rsList1 = new ArrayList<InetSocketAddress>();
        ArrayList<InetSocketAddress> rsList2 = new ArrayList<InetSocketAddress>();
        ArrayList<InetSocketAddress> rsList3 = new ArrayList<InetSocketAddress>();
        ArrayList<InetSocketAddress> rsList4 = new ArrayList<InetSocketAddress>();


        rsList1.add(new InetSocketAddress("127.0.0.1", port + 20));
        rsList1.add(new InetSocketAddress("127.0.0.1", port + 10));
        rsList2.add(new InetSocketAddress("127.0.0.1", port));
        rsList2.add(new InetSocketAddress("127.0.0.1", port + 20));
        rsList3.add(new InetSocketAddress("127.0.0.1", port));
        rsList3.add(new InetSocketAddress("127.0.0.1", port + 10));

        RequestNode node = new RequestNodeImpl();
        node.setActiveRSList(rsList1);
        node.start(ip, port, nbconn);

        RequestNode node2 = new RequestNodeImpl();
        node2.setActiveRSList(rsList2);
        node2.start(ip, port + 10, nbconn);

        RequestNode node3 = new RequestNodeImpl();
        node3.setActiveRSList(rsList3);
        node3.start(ip, port + 20, nbconn);
    }
}
