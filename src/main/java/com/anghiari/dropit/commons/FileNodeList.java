package com.anghiari.dropit.commons;

import java.util.ArrayList;

/**
 * 
 * @author chinthaka316
 *
 */
public class FileNodeList {


	public static ArrayList<FileNode> getFileNodeList(){
		ArrayList<FileNode> nodeList= new ArrayList<FileNode>();
		int port=14500;
        int port_ring=15500;
		int keyid = 100;
		for (int i = 0; i < 5; i++) {
            keyid += 10;
			FileNode fn = new FileNode("localhost", port+i, port_ring++, new KeyId(keyid));
		
			nodeList.add(fn);
		}
		
		return nodeList;
	}
	
}
