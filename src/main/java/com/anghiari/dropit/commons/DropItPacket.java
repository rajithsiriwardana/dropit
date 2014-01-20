package com.anghiari.dropit.commons;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import com.anghiari.dropit.commons.Constants;
/**
 * Created with IntelliJ IDEA.
 * User: amila
 * Date: 1/20/14
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class DropItPacket implements Serializable{

    /*Specifies whether the connection is for a get, put or delete*/
    public String METHOD;

    /*Filename of the file object being handled*/
    public HashMap<String, Object> attrib;

    public DropItPacket(String method, String filename) {
        this.METHOD = method;
        if(Constants.GET.toString().equals(method)){

        }
    }

    public HashMap<String, Object> initPut(String filename, File file){
        HashMap<String, Object> attrib = new HashMap<String, Object>();
        attrib.put(new String("name"), filename);
        attrib.put(new String("file"), file);
        return attrib;
    }

    public HashMap<String, Object> initGet(String filename){
        HashMap<String, Object> attrib = new HashMap<String, Object>();
        attrib.put(new String("name"), filename);
        return attrib;
    }
}
