package com.anghiari.dropit.commons;

import java.io.Serializable;

/**
 * User: amila
 */
public class KeyId implements Serializable{
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
