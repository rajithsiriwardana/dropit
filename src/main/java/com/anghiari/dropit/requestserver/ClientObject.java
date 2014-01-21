package com.anghiari.dropit.requestserver;

import java.io.Serializable;

public class ClientObject implements Serializable {

	
	private static final long serialVersionUID = 1L;

	public int id;
	public String filename;
	public byte[] status;

	public String test1 = "Drop It File Sharing";


	public ClientObject(int id,String filename, byte[] status) {
		this.id=id;
		this.filename=filename;
		this.status = status;
	}

	@Override
	public String toString() {
		return "Drop It: " + test1;
	}
}