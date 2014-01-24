package com.anghiari.dropit.commons;

/**
 * User: amila
 */
public enum Constants {

	/* Communication protocol methods */
	GET, RES_GET, PUT, RES_PUT, STORE, ACK_STORE, RETRIEVE, TRANSFER, PING, PONG, FND_SUSC, RES_SUSC, GOSSIP, GET_FILENODE, RES_GET_FILENODE, REQ_JOIN_NODE, RES_JOIN_NODE,

	/* Attribute keys */
	FILE_NAME, KEY_ID, GOS_LIST, INET_ADDRESS, NODE_IP, NODE_PORT;

	public static final int KEY_SPACE = 4;
	public static final int SUCCESSOR_LIST_SIZE = 3;

	public static final FileNode[] REQUEST_SERVER_LIST = new FileNode[2];

	static {
		REQUEST_SERVER_LIST[0] = new FileNode("", 0);
		REQUEST_SERVER_LIST[1] = new FileNode("", 0);
	}

}
