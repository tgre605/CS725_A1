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
	
	ServerSocket welcomeSocket = new ServerSocket(6789); 
	
	while(true) {
            Socket connectionSocket = welcomeSocket.accept(); 
	    	new TCPServerInstance(connectionSocket).runInstance();
        } 
    } 
} 

