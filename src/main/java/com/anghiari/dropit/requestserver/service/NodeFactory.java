package com.anghiari.dropit.requestserver.service;

import java.io.IOException;


/**
 * @author madhawa
 */
public class NodeFactory {

    public static String[] getNode() throws IOException {
        final String[] args = new String[2];
        args[0] = "192.168.43.253";
        args[1] = "14501";
        return args;
    }

}
