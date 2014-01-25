package com.anghiari.dropit.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author gayashan
 */
public class RequestServerConfigLoader {
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/rs.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Properties file not found");
        }
    }

    public static ArrayList<InetSocketAddress> getRSList() {
        ArrayList<InetSocketAddress> rsList = new ArrayList<InetSocketAddress>();
        for (String server : properties.getProperty("servers").split(",")) {
            rsList.add(new InetSocketAddress(server.split(":")[0], Integer.parseInt(server.split(":")[1])));
        }
        rsList.addAll(getBackUpServers());
        return rsList;
    }

    public static ArrayList<InetSocketAddress> getBackUpServers() {
        ArrayList<InetSocketAddress> backupList = new ArrayList<InetSocketAddress>();
        for (String server : properties.getProperty("backup").split(",")) {
            backupList.add(new InetSocketAddress(server.split(":")[0], Integer.parseInt(server.split(":")[1])));
        }
        return backupList;
    }
}
