package com.anghiari.dropit.requestserver.service;

import java.io.IOException;


/**
 * @author madhawa
 * 
 */
public class NodeFactory {

	public String[] getNode() throws IOException {
		final String[] args = new String[2];
		args[0]="127.0.0.1";
		args[1]="8001";
		return args;

	}

}
