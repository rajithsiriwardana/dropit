package com.anghiari.dropit.requestserver.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author rajith
 * @version ${Revision}
 */
public class DHTMapper implements Mapper {
    private final int KEYSPACE = 32;

    @Override
    public KeyId generateKeyId(String key) {
        try {
            KeyId keyId = new KeyId(0);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            byte[] hash = messageDigest.digest(key.getBytes());
            int keySpaceBytes = KEYSPACE / 4;
            for (int i = 0; i < Math.min(hash.length, keySpaceBytes); ++i) {
                keyId.setHashId(keyId.getHashId() | (((long) hash[i] & 0xff) << (i * 4)));
            }
            return keyId;
        } catch (NoSuchAlgorithmException e) {
            //TODO: handle this exception
            e.printStackTrace();
        }
        return null;
    }
}
