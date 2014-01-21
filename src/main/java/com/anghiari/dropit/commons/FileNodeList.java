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
		int port=12500;
		
		for (int i = 0; i < 5; i++) {
			FileNode fn = new FileNode("localhost", port+i);
		
			nodeList.add(fn);
		}
		
		return nodeList;
	}
	
}
