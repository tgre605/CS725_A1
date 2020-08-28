package Client; /**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*;
import java.nio.file.FileSystems;

class TCPClient {

    
    static File ftp = FileSystems.getDefault().getPath("src/client/sftp/").toFile().getAbsoluteFile();
    public static void main(String argv[]) throws Exception
    {
        Boolean running = true;
        String sentence; 
        String modifiedSentence;
        Socket clientSocket = new Socket("localhost", 6789);
        new File(ftp.toString()).mkdirs();
        while (running){

            BufferedReader inFromUser =
                    new BufferedReader(new InputStreamReader(System.in));

            DataOutputStream outToServer =
                    new DataOutputStream(clientSocket.getOutputStream());

            BufferedReader inFromServer =
                    new BufferedReader(new
                            InputStreamReader(clientSocket.getInputStream()));

            sentence = inFromUser.readLine();

            outToServer.writeBytes(sentence + '\n');

            modifiedSentence = inFromServer.readLine();

            System.out.println("FROM SERVER: " + modifiedSentence);

        }
	
    }

    public void connected(Socket socket){

    }
} 
