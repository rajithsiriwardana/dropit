package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.fileserver.impl.AbstractRunAtInterval;

/**
 * @author gayashan
 */
public class GossipClientRunAtInterval extends AbstractRunAtInterval {
    private RequestNodeImpl requestServer;

    public GossipClientRunAtInterval(int interval, RequestNodeImpl requestServer) {
        super(interval);
        this.requestServer = requestServer;
    }

    @Override
    public void runClosure() {
        this.requestServer.startGossiping();
    }
}
