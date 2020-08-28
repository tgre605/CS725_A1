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
    static File ftp = FileSystems.getDefault().getPath("src/Server/sftp/").toFile().getAbsoluteFile();
    private String sendType;
    String[] accounts;
    String user;
    String password;
    String account;
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    private String directory = "";
    private Path pathToOldfile;
    private String existingFile;
    private boolean retrV;
    private String fileToRetr;
    private boolean cDirP = false;
    private boolean cDirA = false;
    private String directoryTemp;

    TCPServerInstance(Socket socket){
        this.socket = socket;
    }
    public void runInstance() throws Exception {
        inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outToClient = new DataOutputStream(socket.getOutputStream());
        new File(ftp.toString()).mkdirs();
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
        switch (modeArgs[0].toUpperCase()){
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
                outToClient.writeBytes(list(modeArgs[1]));
                break;
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
            case "SEND":
                outToClient.writeBytes(send());
                break;
            case "RETR":
                outToClient.writeBytes(retr(modeArgs[1]));
                break;
            case "STOR":
                break;
            default:
                outToClient.writeBytes("-invalid Command" + "\n");
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
            if(cDirA == true){
                String dir = changeDir();
                cDirA = false;
                return dir + "\n";
            }
            return "! Account valid, logged-in"+ "\n";
        } else if (accountV){
            return "+Account valid, send password"+ "\n";
        } else {
            return "-Invalid account, try again"+ "\n";
        }
    }

    public String pass(String userInput) throws Exception {
        if(userV == true){
            if(password.equals(null)){
                passwordV = true;
            }
            if(userInput.equals(password)){
                passwordV = true;
            }
            if(accountV && passwordV){
                if(cDirP == true){
                    String dir = changeDir();
                    cDirA = false;
                    return dir + "\n";
                }
                return "! logged in"+ "\n";
            } else if(passwordV == true){
                return "+ Password ok, send account" + "\n";
            }
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

    public String list(String userInput) throws Exception {
        if(userV) {
            if ("F".equals(userInput)) {
                Path path = new File(directory).toPath();
                String response = null;
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory))) {
                    response += directory + "\n";
                    for (Path filePath : stream) {
                        response += filePath.getFileName() + "\n";
                    }
                    return response;
                } catch(Exception e) {
                    return "failed";
                }
            }
        }
        return "-Not found because: unauthorised, please sign in"+ "\n";
    }

    public String cdir(String userInput) throws Exception{
        String cdDirectory = "src/Server/"+userInput;
        if(userV && passwordV && accountV){
            directoryTemp = cdDirectory;
            String response = changeDir();
            return response;
        }
        
        if(userV){
            cdDirectory = "src/Server/"+userInput;
            File file = new File(cdDirectory);
            if (!file.isDirectory()){
                return "-Can't connect to directory because: " + cdDirectory + " is not a directory"+ "\n";
            }
            directoryTemp = cdDirectory;
            if(accountV == false && passwordV == false){
                cDirA = true;
                cDirP = true;
                return "+directory ok, send account/password" +"\n";
            }
            if(accountV == false){
                cDirA = true;
                return "+directory ok, send account/password" +"\n";
            }
            if(passwordV == false){
                cDirP = true;
                return "+directory ok, send account/password" +"\n";
            }
            return"+directory ok, send account/password" + "\n";
        }
        return "-Can't connect to directory because: unauthorised, please sign in"+ "\n";
    }

    private String changeDir() {
        File file = new File(directoryTemp);
        if (!file.isDirectory()){
            return "-Can't connect to directory because: " + directoryTemp + " is not a directory"+ "\n";
        }
        directory = directoryTemp;
        directoryTemp = null;
        return "!Changed working dir to " + directory+ "\n";
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
                existingFile = userInput;
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
            fileExists = false;
            String response = "+"+existingFile+" renamed to " + userInput;
            this.existingFile = null;
            this.pathToOldfile = null;
            return response+ "\n";
        }
        if(!fileExists){
            return "-File wasn't renamed because file does not exist"+ "\n";
        }
        else return "-Not found because: unauthorised, please sign in"+ "\n";
        
    }

    public String retr(String userInput) throws Exception{
        if(userV){
            Path pathToFileTemp = new File(directory+ "/" + userInput).toPath();
            if(Files.exists(pathToFileTemp)){
                Long fileSize = new File(String.valueOf(pathToFileTemp)).length();
                fileToRetr = userInput;
                retrV = true;
                return "Size of file to send:"+ fileSize+ "\n";
            }
            return "-Can't find "+ userInput + "\n";
        }
        else return "-Not found because: unauthorised, please sign in"+ "\n";
    }

    public String send() throws Exception{
        if(userV && retrV){
            Path pathToFile = new File(directory+ "/" + fileToRetr).toPath();
            File file = new File(String.valueOf(pathToFile));
            byte[] bytes = new byte[(int) file.length()];
            fileToRetr = null;
            try (BufferedInputStream bufferedStream = new BufferedInputStream(new FileInputStream(file))) {
                outToClient.flush();
                // Read and send by byte
                int p = 0;
                while ((p = bufferedStream.read(bytes)) >= 0) {
                    outToClient.write(bytes, 0, p);
                }
                bufferedStream.close();
                outToClient.flush();
            } catch (IOException e){
                socket.close();
                running = false;
            }
        }
        if(userV){
            return "No file selected" + "\n";
        }
        else return "-Not found because: unauthorised, please sign in"+ "\n";
    }
}
