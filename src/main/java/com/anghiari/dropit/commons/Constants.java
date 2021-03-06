package com.anghiari.dropit.commons;

/**
 * User: amila
 */
public enum Constants {

    /* Communication protocol methods */
    REQ, RES, GET, RES_GET, PUT, RES_PUT, STORE, ACK_STORE, RETRIEVE, TRANSFER, PING, PONG, FND_SUSC, RES_SUSC, FND_SUSC_INT, RES_SUSC_INT, GOSSIP,
    GET_FILENODE, RES_GET_FILENODE, FILE_PATH, REQ_JOIN_NODE, RES_JOIN_NODE, REQ_JOIN_FINAL, RES_JOIN_FINAL, SEARCH,
    RES_SEARCH, SET_SUCCESSOR, SET_PREDECESSOR, ACK_SUCCESSOR, ACK_PREDECESSOR, ACK_FILE_SAVED, DELETE, ACK_DELETE, LIST_NULL,
    GET_PREDECESSOR, RES_GET_PREDECESSOR,RECVD_PACKET, RECVD_PATH, RECVD_FNAME, CLIENT, REPLICATE, SUCC_ALIVE, LIVE_SUCC, PRED_ALIVE, LIVE_PRED,

    /* Attribute keys */
    FILE_NAME, KEY_ID, GOS_LIST, INET_ADDRESS, NODE_IP, NODE_PORT, SEARCH_RESULTS, SUCCESSOR, PREDECESSOR, FILE_EXISTS, FILE_NODE, FINGER, IP_LIST, REQ_NODE;

    public static final int KEY_SPACE = 4;
    public static final int SUCCESSOR_LIST_SIZE = 3;

    public static final FileNode[] REQUEST_SERVER_LIST = new FileNode[2];

    static {
        REQUEST_SERVER_LIST[0] = new FileNode("192.248.8.241", 8010);
        REQUEST_SERVER_LIST[1] = new FileNode("", 0);
    }

    public static final int FS_INTERVAL = 15000;
    public static final int GOSSIP_INTERVAL = 5000;
    public static final float LOG_BASE = 16.0f;
}
