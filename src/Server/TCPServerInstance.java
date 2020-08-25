package Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TCPServerInstance{
    private Socket socket;
    static String authInfo;
    private Boolean running = true;
    private Boolean userV = false;
    private Boolean accountV = false;
    private Boolean passwordV = false;
    private String sendType;
    String[] accounts;
    String user;
    String password;
    String account;
    BufferedReader inFromClient;
    DataOutputStream outToClient;

    TCPServerInstance(Socket socket){
        this.socket = socket;
    }
    public void runInstance() throws Exception {
        inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outToClient = new DataOutputStream(socket.getOutputStream());
        while (running){
            try {
                String[] clientInput = inFromClient.readLine().split(" ");
                if (clientInput[0].equals("DONE")) {
                    outToClient.writeBytes("+(the message may be charge/accounting info)" + "\n");
                    running = false;
                    socket.close();
                    break;
                } else {
                    mode(clientInput);
                }
            } catch (Exception e){}
        }
    }

    public void mode(String[] modeArgs) throws Exception {
        switch (modeArgs[0]){
            case "USER":
                outToClient.writeBytes(user(modeArgs[1]));
                break;
            case "ACCT":
                outToClient.writeBytes(acct(modeArgs[1]));
                break;
            case "PASS":
                outToClient.writeBytes(pass(modeArgs[1]));
                break;
            case "TYPE":
                type(modeArgs[1]);
                break;
            case "LIST":
            case "CDIR":
            case "KILL":
            case "NAME":
            case "DONE":
            case "RETR":
            case "STOR":
            default:
                break;
        }
    }

    public static void setAuthInfo(String authInfo) {
        TCPServerInstance.authInfo = authInfo;
    }

    public String user(String userInput) throws Exception {
        Boolean userExists = false;
        String[] lineAccounts = null;
        String linePassword = null;
        String passSearch = null;
        String[] accSearch = null;

        File authFile = new File("src/Server/authFile.txt");


        BufferedReader reader = new BufferedReader(new FileReader(authFile));
        String line = reader.readLine();
        while (line != null)
        {
            String[] userInfo = line.split(" ");
            String lineUsername = userInfo[0];
            if(userInfo.length == 2){
                lineAccounts = userInfo[1].split(",");
            } else if(userInfo.length == 3){
                lineAccounts = userInfo[1].split(",");
                linePassword = userInfo[2];
            }
            if(lineUsername.equals(userInput)){
                userExists = true;
                if(userInfo.length == 2){
                    accSearch = lineAccounts;
                } else if(userInfo.length == 3){
                    accSearch = lineAccounts;
                    passSearch = linePassword;
                }
                break;
            }
            line = reader.readLine();
        }
        if(userExists == false){
            return "Invalid Username" + "\n";
        }
        if(passSearch==null){
            password = null;
            passwordV = true;
        }
        if (accSearch.length > 0){
            accounts = accSearch;
        } else {
            accountV = true;
        }
        if(passwordV && accountV){
            userV = true;
            return "!" + userInput + " logged in" + "\n";
        } else {
            userV = true;
            password = passSearch;
            return  "+User-id valid, send account and password" + "\n";
        }
    }

    public String acct(String userInput) throws Exception {
        if(userV == true){
            for (String accountTest : accounts) {
                if(userInput.equals(accountTest)){
                    account = userInput;
                    accountV = true;
                    break;
                }
            }

        }
        if(passwordV && accountV){
            accountV = true;
            return "! Account valid, logged-in"+ "\n";
        } else if (accountV){
            accountV = true;
            return "+Account valid, send password"+ "\n";
        } else {
            return "-Invalid account, try again"+ "\n";
        }
    }

    public String pass(String userInput) throws Exception {
        if(password.equals(null)){
            if(accountV){
                passwordV = true;
                return "! Logged in" + "\n";
            }
        }
        if(userV == true){
            if(userInput.equals(password)){
                if(accountV){
                    passwordV = true;
                    return "! logged in"+ "\n";
                } else {
                    passwordV = true;
                    return "+ Send account" + "\n";
                }
            }
            return "-Wrong password, try again"+ "\n";
        }
        return "-Wrong password, try again"+ "\n";
    }

    public void type(String userInput) throws Exception {
        switch (userInput){
            case "A":
                sendType = "A";
                outToClient.writeBytes("+Using Ascii"+ "\n");
                break;
            case "B":
                sendType = "B";
                outToClient.writeBytes("+Using Binary"+ "\n");
                break;
            case "C":
                sendType = "C";
                outToClient.writeBytes("+Using Continuous" + "\n");
                break;
            default:
                outToClient.writeBytes("-Type not valid" + "\n");
        }
    }



}
