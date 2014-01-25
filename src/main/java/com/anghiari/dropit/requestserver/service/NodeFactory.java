package com.anghiari.dropit.requestserver.service;

import java.io.IOException;


/**
 * @author madhawa
 */
public class NodeFactory {

    public static String[] getNode() throws IOException {
        final String[] args = new String[2];
        args[0] = "192.248.8.241";
        args[1] = "14505";
        return args;
    }

}
