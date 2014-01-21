package com.anghiari.dropit.requestserver.service;

/**
 * @author gayashan
 */
public class KeyId {
    private long hashId;

    public KeyId(long hashId) {
        this.setHashId(hashId);
    }

    public long getHashId() {
        return hashId;
    }

    public void setHashId(long hashId) {
        this.hashId = hashId;
    }
}
