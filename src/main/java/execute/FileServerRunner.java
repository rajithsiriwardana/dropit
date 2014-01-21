package execute;

import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;

public class FileServerRunner {

	
	public static void main(String[] args) {
		
		
		FileServerNodeImpl server = new FileServerNodeImpl();
		
		server.bootServer("127.0.0.1", 12500);
	}
}
