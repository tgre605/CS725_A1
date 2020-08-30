package Client; /**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;

class TCPClient {
    static DataOutputStream outToServer;
    static BufferedReader inFromServer;
    static DataOutputStream binToServer;
    static DataInputStream binFromServer;
    static String sentence;

    static File ftp = FileSystems.getDefault().getPath("src/client/sftp/").toFile().getAbsoluteFile();
    private static String sendType;
    private static boolean dontRead;
    private static boolean running;
    private static Socket clientSocket;
    private static String fileToRetr;
    private static int size;

    public static void main(String[] argv) throws Exception
    {
        running = true;
        String modifiedSentence;
        clientSocket = new Socket("localhost", 6789);
        new File(ftp.toString()).mkdirs();
        outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        inFromServer =
                new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));
        binToServer = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
        binFromServer = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
        while (running) {
            dontRead = false;
            BufferedReader inFromUser =
                    new BufferedReader(new InputStreamReader(System.in));
            sentence = inFromUser.readLine();
            String[] command = sentence.split(" ");
            mode(command[0]);

            if (!dontRead && !clientSocket.isClosed()){
                modifiedSentence = readFromServer(inFromServer);
                System.out.println("FROM SERVER: " + modifiedSentence);
            }

        }

    }
    public static void mode(String modeArg) throws Exception {
        switch (modeArg.toUpperCase()){
            case "USER":
                sendToServer(sentence);
                break;
            case "ACCT":
                sendToServer(sentence);
                break;
            case "PASS":
                sendToServer(sentence);
                break;
            case "TYPE":
                type(sentence.split(" ")[1]);
                break;
            case "LIST":
                sendToServer(sentence);
                break;
            case "CDIR":
                sendToServer(sentence);
                break;
            case "KILL":
                sendToServer(sentence);
                break;
            case "NAME":
                sendToServer(sentence);
                break;
            case "TOBE":
                sendToServer(sentence);
                break;
            case "SEND":
                send(sentence);
                break;
            case "RETR":
                retr(sentence);
                break;
/*            case "STOP":
                fileToRetr = null;
                retrV = false;
                sendToServer("+ok, RETR aborted");
                break;*/
            case "STOR":
                stor(sentence);
                break;
            case "DONE":
                sendToServer(sentence);
                System.out.println("FROM SERVER: " + readFromServer(inFromServer));
                running = false;
                clientSocket.close();
                break;
            default:
                sendToServer("-invalid Command");
                break;
        }
    }
    public static void type(String userInput) throws Exception {
        sendToServer(sentence);
        switch (userInput){
            case "A":
                sendType = "A";
                break;
            case "B":
                sendType = "B";
                break;
            case "C":
                sendType = "C";
                break;
            default:
                break;
        }
    }

    public static void retr(String userInputs) throws Exception {
        String[] userInput = userInputs.split(" ");
        sendToServer("RETR " + userInput[1]);
        String serverResponse = readFromServer(inFromServer);
        dontRead = true;
        if ("-".equals(serverResponse.substring(0,1))){
            System.out.println(serverResponse);
        } else {
            fileToRetr = userInput[1];
            System.out.println(serverResponse);
            size = Integer.parseInt(serverResponse);
        }
    }

    public static void send(String userInput) throws IOException {
        sendToServer("SEND");
        File file = new File(ftp.getPath() + "/" + fileToRetr);
        if ("A".equals(sendType)) {
            BufferedOutputStream bufferedStream = new BufferedOutputStream(new FileOutputStream(file, false));
            for (int i = 0; i < size; i++) {
                bufferedStream.write(inFromServer.read());
            }
            bufferedStream.flush();
            bufferedStream.close();
            System.out.println("File " + fileToRetr + " was saved.");
        } else {
            FileOutputStream fileStream = new FileOutputStream(file, false);
            int e;
            int i = 0;
            byte[] bytes = new byte[(int) size];
            while (i < size) {
                e = binFromServer.read(bytes);
                fileStream.write(bytes, 0, e);
                i+=e;
            }
            fileStream.flush();
            fileStream.close();
            System.out.println("File " + fileToRetr + " was saved.");

        }
    }


    public static void stor(String userInputs) throws Exception {
        String[] userInput = userInputs.split(" ");
        File file = null;
        if(userInput.length == 3) {
            file = new File(ftp.getPath() + "/" + userInput[2]);
            if (!file.isFile()) {
                System.out.println("-File does not exist");
                return;
            }
        }

        sendToServer(userInputs);
        String serverResponse = readFromServer(inFromServer);
        if("-".equals(serverResponse.substring(0,1))){
            System.out.println(serverResponse);
            dontRead = true;
        }
        if ("+".equals(serverResponse.substring(0,1))){
            System.out.println("+Sending");
            System.out.println(serverResponse + ". Sending SIZE " + file.length());
            sendToServer("SIZE " + file.length());  //send size
            serverResponse = readFromServer(inFromServer);
            System.out.println(serverResponse);

            if ("+".equals(serverResponse.substring(0,1))){
                byte[] bytes = new byte[(int) file.length()];
                BufferedInputStream bufferedStream = new BufferedInputStream(new FileInputStream(file));
                outToServer.flush();
                int b;
                while ((b = bufferedStream.read(bytes)) >= 0) {
                    outToServer.write(bytes, 0, b);
                }
                bufferedStream.close();
                outToServer.flush();
            }
        }
    }



    private static String readFromServer(BufferedReader inFromServer) throws IOException {
        StringBuilder text = new StringBuilder();
        var character = 0;
        while (true){
            character = inFromServer.read();
            if ((char) character == '\0' && text.length() > 0) {
                break;
            }
            if ((char) character != '\0') {
                text.append((char) character);
            }
        }
        return text.toString();
    }

    private static void sendToServer(String message) throws IOException {
        outToServer.writeBytes(message + "\0");
    }

} 
