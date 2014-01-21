package com.anghiari.dropit.requestserver;

import com.anghiari.dropit.execute.RequestServerRunner;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rajith
 * @version ${Revision}
 */
public class RequestServerTest {

    @Before
    public void setupEnvironment(){
        RequestServerRunner.main(new String[]{"8005", "100"});
    }

    @Test
    public void testServer(){

    }
}
