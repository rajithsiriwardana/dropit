package com.anghiari.dropit.commons;

import java.io.Serializable;
import java.util.HashMap;
/**
 * User: amila
 */
public class DropItPacket implements Serializable{

    /*Specifies whether the connection is for a get, put or delete*/
    private String method;
    private byte[] data;

    /*Filename of the file object being handled*/
    private HashMap<String, Object> attrib;

    public DropItPacket(String method) {
        this.method = method;
        this.attrib = new HashMap<String, Object>();
    }

    public String getMethod(){
        return method;
    }

    public Object getAttribute(String key){
        return attrib.get(key);
    }

    public boolean setAttribute(String key, Object value){
        if(key!=null && value !=null){
            attrib.put(key, value);
            return true;
        }
        return false;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
