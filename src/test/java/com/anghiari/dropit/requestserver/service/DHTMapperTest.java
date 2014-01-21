package com.anghiari.dropit.requestserver.service;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author gayashan
 */
public class DHTMapperTest {
    private DHTMapper dhtMapper;

    @Before
    public void setUp() throws Exception {
        this.dhtMapper = new DHTMapper();
    }

    @Test
    public void testGenerateKeyId() throws Exception {
        KeyId keyId = this.dhtMapper.generateKeyId("dropit.png");
        assertEquals(1870626650L, keyId.getHashId());
    }
}
