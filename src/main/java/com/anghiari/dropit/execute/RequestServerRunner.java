package com.anghiari.dropit.execute;

import com.anghiari.dropit.requestserver.RequestNode;
import com.anghiari.dropit.requestserver.RequestNodeImpl;

/**
 * @author rajith
 * @version ${Revision}
 */
public class RequestServerRunner {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: " +
                    RequestServerRunner.class.getSimpleName() +  " <port> <nb of connections>");
            return;
        }
        // Parse options.
        int port = Integer.parseInt(args[0]);
        int nbconn = Integer.parseInt(args[1]);

        RequestNode node = new RequestNodeImpl();
        node.start(port, nbconn);
    }
}
