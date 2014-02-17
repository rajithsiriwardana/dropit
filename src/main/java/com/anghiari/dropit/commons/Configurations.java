package com.anghiari.dropit.commons;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * User: amila
 */
public class Configurations {


    public final static String ip = "192.248.8.240";
    public final static int[] extPorts = {14500,14501,14502,14503,14504,14505,14506,14507,14508,14509};
    public final static int[] intPorts = {15500,15501,15502,15503,15504,15505,15506,15507,15508,15509};

    public final static int[] fileNodeKeys = {0, 4, 7, 9, 15, 20, 22, 24, 28, 30};
    public final static String FOLDER_PATH = "./FILES";

    
//    
//    public ArrayList<FileNode> initializeNodes(){
//    	ArrayList<FileNode> fileNodeList = new ArrayList<FileNode>();
//    	
//    	File fXmlFile = new File("configuration.xml");
//    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//    	try{	  
//    		
//	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//	    	Document doc = dBuilder.parse(fXmlFile);
//     
//    		//optional, but recommended
//	    	doc.getDocumentElement().normalize();
//	     
//	    	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
//	     
//	    	NodeList nList = doc.getElementsByTagName("fileserver");
//	     
//	    	System.out.println("----------------------------");
//	     
//		    	for (int temp = 0; temp < nList.getLength(); temp++) {
//		     
//		    		Node nNode = nList.item(temp);
//		     
//		    		System.out.println("\nCurrent Element :" + nNode.getNodeName());
//		    		
//		    		FileNode node;
//		     
//		    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//		     
//		    			Element eElement = (Element) nNode;
//		     
//		    			int id = Integer.parseInt(eElement.getAttribute("id"));
//		    			int port =  Integer.parseInt(eElement.getAttribute("port"));
//		    			int portRing =  Integer.parseInt(eElement.getAttribute("portring"));
//		    			Long key= Long.parseLong(eElement.getAttribute("key"));
//		    			KeyId keyId= new KeyId(key);
//		    			node = new FileNode(id, 
//		    					eElement.getAttribute("ip"), 
//		    					port, portRing, keyId);
//		    			
//		    		}
//	    	}
//    	}catch (Exception e) {
//        	e.printStackTrace();
//        }
}


