package Client;

import Server.TCPServerInstance;

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
        //initialise global variables and inputs/outputs to/from server
        running = true;
        String modifiedSentence;
        clientSocket = new Socket("localhost", 6789);
        new File(ftp.toString()).mkdirs();
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        binToServer = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
        binFromServer = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
        
        
        while (running) {
            
            //read input from user and send to correct method using mode function
            dontRead = false;
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            sentence = inFromUser.readLine();
            String[] command = sentence.split(" ");
            mode(command[0]);
            //if dontRead value is not set and socket is still open, attempt to read from server
            if (!dontRead && !clientSocket.isClosed()){
                modifiedSentence = readFromServer(inFromServer);
                System.out.println("FROM SERVER: " + modifiedSentence);
            }

        }

    }
    
    /*send command to correct function via switch case
    * some commands dont need their own function since they are small enough
    * to be performed within switch case*/
    public static void mode(String modeArg) throws Exception {
        switch (modeArg.toUpperCase()) {
            case "USER" -> user();
            case "ACCT" -> acct();
            case "PASS" -> pass();
            case "TYPE" -> type(sentence.split(" ")[1]);
            case "LIST" -> list();
            case "CDIR" -> cdir();
            case "KILL" -> kill();
            case "NAME" -> name();
            case "TOBE" -> tobe();
            case "SEND" -> send();
            case "STOP" -> {
                fileToRetr = null;
                size = 0;
            }
            case "RETR" -> retr();
            case "STOR" -> stor();
            case "DONE" -> {
                sendToServer(sentence);
                System.out.println("FROM SERVER: " + readFromServer(inFromServer));
                running = false;
                clientSocket.close();
            }
            default -> sendToServer("-Invalid Command");
        }
    }
    
    /*The following methods are all similar and are checking that the input
    * has the right number of arguments for the desired method call*/
    public static void user() throws IOException {
        if(sentence.split(" ").length == 2){
            sendToServer(sentence);
        } else {
            System.out.println("-Invalid input");
            dontRead = true;
        }
    }

    public static void acct() throws IOException {
        if(sentence.split(" ").length == 2){
            sendToServer(sentence);
        } else {
            System.out.println("-Invalid input");
            dontRead = true;
        }
    }
    public static void pass() throws IOException {
        if(sentence.split(" ").length == 2){
            sendToServer(sentence);
        } else {
            System.out.println("-Invalid input");
            dontRead = true;
        }
    }
    public static void list() throws IOException {
        if(sentence.split(" ").length == 2){
            sendToServer(sentence.toUpperCase());
        } else {
            System.out.println("-Invalid input, expected F or V");
            dontRead = true;
        }
    }
    public static void cdir() throws IOException {
        if(sentence.split(" ").length == 2){
            sendToServer(sentence);
        } else {
            System.out.println("-Invalid input");
            dontRead = true;
        }
    }
    public static void kill() throws IOException {
        if(sentence.split(" ").length == 2){
            sendToServer(sentence);
        } else {
            System.out.println("-Invalid input");
            dontRead = true;
        }
    }

    public static void name() throws IOException {
        if(sentence.split(" ").length == 2){
            sendToServer(sentence);
        } else {
            System.out.println("-Invalid input");
            dontRead = true;
        }
    }
    public static void tobe() throws IOException {
        if(sentence.split(" ").length == 2){
            sendToServer(sentence);
        } else {
            System.out.println("-Invalid input");
            dontRead = true;
        }
    }
    
    //Changed client side sendType to Ascii or binary
    public static void type(String userInput) throws Exception {
        if(sentence.split(" ").length == 2){
            sendToServer(sentence);
            switch (userInput.toUpperCase()){
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
    }
    
    //sends RETR command to server
    public static void retr() throws Exception {
        String[] userInput = sentence.split(" ");
        sendToServer("RETR " + userInput[1]);
        //Since reading from server here, sets dontRead flag so main function wont read and block
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

    public static void send() throws IOException {
        //if the file exists, tell server to send file
        if(fileToRetr != null){
            sendToServer("SEND");
            File file = new File(ftp.getPath() + "/" + fileToRetr);
            //if in Ascii send mode, use bufferedstream to save file from server
            if ("A".equals(sendType)) {
                BufferedOutputStream bufferedStream = new BufferedOutputStream(new FileOutputStream(file, false));
                for (int i = 0; i < size; i++) {
                    bufferedStream.write(inFromServer.read());
                }
                bufferedStream.flush();
                bufferedStream.close();
            } else {
                //if in binary or continuous send mode, use fileoutputstream to save from server
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
            }
            System.out.println("File " + fileToRetr + " was saved.");
        } else {
            System.out.println("-No file selected");
        }
        dontRead = true;
    }

        
    
    public static void stor() throws Exception {
        String[] userInput = sentence.split(" ");
        File file = null;
        //if input arguments are correct, check if file exists
        if(userInput.length == 3) {
            file = new File(ftp.getPath() + "/" + userInput[2]);
            if (!file.isFile()) {
                System.out.println("-File does not exist");
                return;
            }
        }
        //tell server that a file will be coming and how the client wants it stored ie. NEW, OLD, APP
        sendToServer(sentence);
        String serverResponse = readFromServer(inFromServer);
        if("-".equals(serverResponse.substring(0,1))){
            System.out.println(serverResponse);
            dontRead = true;
        }
        //if server responds with + send size of file
        if ("+".equals(serverResponse.substring(0,1))){
            System.out.println("+Sending");
            assert file != null;
            System.out.println(serverResponse + ". Sending SIZE " + file.length());
            sendToServer("SIZE " + file.length());
            serverResponse = readFromServer(inFromServer);
            System.out.println("FROM SERVER: " + serverResponse);
            
            //if server responds with + send file with correct amount of bytes
            if ("+".equals(serverResponse.substring(0,1))){
                outToServer.flush();
                byte[] bytes = new byte[(int) file.length()];
                TCPServerInstance.bufStream(file, bytes, outToServer);
            }
        }
    }


    /*method of reading from server by reading char by char and checking if \0 has come as the end of line
     * If end of line reached but not end of input keep reading*/
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

    //method of outputting to server by appending \0 on end of line
    private static void sendToServer(String message) throws IOException {
        outToServer.writeBytes(message + "\0");
    }

} 
