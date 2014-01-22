package com.anghiari.dropit.requestserver.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author madhawa
 *
 */
public class NodeFactory {

	public String[] getNode() throws IOException {
		Properties prop = new Properties();
		InputStream in = this
				.getClass()
				.getClassLoader()
				.getResourceAsStream(
						"com/anghiari/dropit/requestserver/service/resources/nodelist.properties");

		prop.load(in);
		final String[] args=new String[2];
		args[0] = prop.getProperty("node");
		args[1] = prop.getProperty("port");
		return args;

	}

}
