package Server; /**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*; 

class TCPServer { 
    
    public static void main(String argv[]) throws Exception 
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

