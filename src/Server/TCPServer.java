package Server;

import java.net.ServerSocket;
import java.net.Socket;

class TCPServer { 
    
    public static void main(String[] argv) throws Exception 
    {
	/*connects to welcome socket and receives connection socket from server
	*This socket is then used to instantiate a new TCPServerInstance where all
	* methods are stored for operations between client and server*/
	 
	ServerSocket welcomeSocket = new ServerSocket(6789); 
	boolean running = true;
	while(running) {
            Socket connectionSocket = welcomeSocket.accept(); 
	    	running = new TCPServerInstance(connectionSocket).runInstance();
        } 
    } 
} 

