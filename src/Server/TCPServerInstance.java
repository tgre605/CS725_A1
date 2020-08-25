package Server;

import java.io.*;
import java.net.Socket;

public class TCPServerInstance{
    private Socket socket;
    static String authInfo;
    private Boolean running = true;
    private Boolean userV = false;
    BufferedReader inFromClient;
    DataOutputStream outToClient;

    TCPServerInstance(Socket socket){
        this.socket = socket;
    }
    public void runInstance() throws Exception {
        inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outToClient = new DataOutputStream(socket.getOutputStream());
        while (running){
            String[] clientInput = inFromClient.readLine().split(" ");
            if(clientInput[0] == "DONE"){
                outToClient.writeBytes("+(the message may be charge/accounting info)");
                running = false;
                socket.close();
                break;
            } else {
                mode(clientInput);
            }
        }
    }

    public void mode(String[] modeArgs) throws Exception {
        switch (modeArgs[0]){
            case "USER":
                outToClient.writeBytes(user(modeArgs[1]));
                break;
            case "ACCT":
                outToClient.writeBytes(user(modeArgs[1]));
                break;
            case "PASS":
                outToClient.writeBytes(user(modeArgs[1]));
                break;
            case "TYPE":
            case "LIST":
            case "CDIR":
            case "KILL":
            case "NAME":
            case "DONE":
            case "RETR":
            case "STOR":
        }
    }

    public static void setAuthInfo(String authInfo) {
        TCPServerInstance.authInfo = authInfo;
    }

    public String user(String userInput) throws Exception {
        Boolean userExists = false;
        Boolean passwordV = false;
        Boolean accountV = false;
        String passSearch = null;
        String[] accSearch = null;
        File authFile = new File("authFile.txt");

        BufferedReader reader = new BufferedReader(new FileReader(authFile));
        String line = reader.readLine();
        while (line != null)
        {
            String[] userInfo = line.split(" ");
            String lineUsername = userInfo[0];
            String[] lineAccounts = userInfo[1].split(",");
            String linePassword = userInfo[2];
            if(lineUsername.equals(userInput)){
                userExists = true;
                passSearch = linePassword;
                accSearch = lineAccounts;
                break;
            }
            line = reader.readLine();
        }
        if(userExists == false){
            return "Invalid Username";
        }
        if(passSearch.equals("")){
            passwordV = true;
        }
        if (accSearch.length <= 1){
            accountV = true;
        }
        if(passwordV && accountV){
            userV = true;
            return "!" + userInput + "logged in";
        } else if (passwordV){
            return  "+User-id valid, send account";
        } else if (accountV){
            return  "+User-id valid, send password";
        } else {
            return  "+User-id valid, send account and password";
        }
    }

    public void acct(String userInput) throws Exception {

    }



}
