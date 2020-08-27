package Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;

public class TCPServerInstance{
    private Socket socket;
    static String authInfo;
    private Boolean running = true;
    private Boolean userV = false;
    private Boolean accountV = false;
    private Boolean passwordV = false;
    private Boolean fileExists = false;
    private String sendType;
    String[] accounts;
    String user;
    String password;
    String account;
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    private String directory = "";
    private Path pathToOldfile;

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
                outToClient.writeBytes(cdir(modeArgs[1]));
                break;
            case "KILL":
                outToClient.writeBytes(kill(modeArgs[1]));
                break;
            case "NAME":
                outToClient.writeBytes(name(modeArgs[1]));
                break;
            case "TOBE":
                outToClient.writeBytes(tobe(modeArgs[1]));
                break;
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

        File authFile = new File("src/Server/authFile.txt");

        BufferedReader reader = new BufferedReader(new FileReader(authFile));
        String line = reader.readLine();
        while (line != null)
        {
            String[] userInfo = line.split(" ");
            String lineUsername = userInfo[0];
            if(lineUsername.equals(userInput)){
                userExists = true;
                if(userInfo.length == 2){
                    if(!(userInfo[1] == null)){
                        lineAccounts = userInfo[1].split(",");
                    }
                    passwordV = true;
                } else if(userInfo.length == 3){
                    if(!(userInfo[1] == null)){
                        lineAccounts = userInfo[1].split(",");
                    }
                    linePassword = userInfo[2];
                }
                if(userInfo.length == 1){
                    accountV = true;
                    passwordV = true;
                } else if(userInfo.length == 2){
                    accounts = lineAccounts;
                    password = null;
                    passwordV = true;
                } else if(userInfo.length == 3){
                    if(userInfo[1].isEmpty()){
                        account = null;
                        accountV = true;
                    }
                    accounts = lineAccounts;
                    password = linePassword;
                }
                break;
            }
            line = reader.readLine();
        }
        if(userExists == false){
            return "Invalid Username" + "\n";
        }
        userV = true;
        if(passwordV && accountV){
            return "!" + userInput + " logged in" + "\n";
        } else {
            if(accountV == true){
                return  "+User-id valid, send password" + "\n";
            } else if(passwordV == true){
                return  "+User-id valid, send account" + "\n";
            } else {
                return  "+User-id valid, send account and password" + "\n";
            }
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

    public void list(String userInput) throws Exception {
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

    public String cdir(String userInput) throws Exception{
        if(userV){
            String cdDirectory = "src/Server/"+userInput;
            // Test if it is a directory
            File file = new File(cdDirectory);
            if (!file.isDirectory()){
                return "-Can't connect to directory because: " + cdDirectory + " is not a directory"+ "\n";
            }
            directory = cdDirectory;
            return "!Changed working dir to " + cdDirectory+ "\n";
        }
        else return "-Can't connect to directory because: unauthorised, please sign in"+ "\n";
    }

    public String kill(String userInput) throws Exception{
        if(userV){
            Path pathToFile = new File(directory+ "/" + userInput).toPath();
            try {
                Files.delete(pathToFile);
                return "+"+userInput+" deleted"+ "\n";
            } catch (NoSuchFileException e){
                return "-Not deleted because file does not exist"+ "\n";
            }
        }
        else return "-Not deleted because: unauthorised, please sign in"+ "\n";
    }

    public String name(String userInput) throws Exception{
        if(userV){
            Path pathToOldFileTemp = new File(directory+ "/" + userInput).toPath();
            if(Files.exists(pathToOldFileTemp)){
                fileExists = true;
                this.pathToOldfile = pathToOldFileTemp;
                return "+File exists"+ "\n";
            }
            return "-Can't find "+ userInput + "\n";
        }
        else return "-Not found because: unauthorised, please sign in"+ "\n";
    }

    public String tobe(String userInput) throws Exception{
        if(userV && fileExists){
            Path newFileName = new File(directory+ "/" + userInput).toPath();
            if(Files.exists(newFileName)){
                return "-File wasn't renamed because file already exists"+ "\n";
            }
            File oldFile = new File(String.valueOf(pathToOldfile));
            oldFile.renameTo(new File(directory+ "/" + userInput));
            return "-Can't find <old-file-spec>"+ "\n";
        }
        else return "-Not deleted because: unauthorised, please sign in"+ "\n";
    }
    

}
