import java.io.*;
import java.net.*;



public class FtpServ{
	private DataInputStream din;
	private DataOutputStream dout;
	//DataInputStream din;
	//DataOutputStream dout;
	
	String directory = System.getProperty("user.dir");
	
	public FtpServ(){
	}
	
	public FtpServ(Socket clientSocket) throws IOException{
		din = new DataInputStream(clientSocket.getInputStream());
		dout = new DataOutputStream(clientSocket.getOutputStream());
	}
	
	public void ls() throws IOException{
		File dir = new File(System.getProperty("user.dir"));
		String files[] = dir.list();
		for(String file: files){
			System.out.print(file + " ");
		}
		System.out.println("");
	}
	
        public void mkdir(String dirName) throws IOException{
		System.out.println("Creating directory '" + dirName + "'");
		File dir = new File(dirName);
		dir.mkdir();
        }

        public void cd(String dirName) throws IOException{
		System.out.println("Changing directory to '" + dirName + "'");
		if(dirName.equals("..")){
			File dir = new File(System.getProperty("user.dir"));
			System.setProperty("user.dir", dir.getAbsoluteFile().getParent());
		}
		else{
			File dir = new File(dirName);
			System.setProperty("user.dir", dir.getAbsolutePath());
		}
	
        }

        public void pwd() throws IOException{
		String pwd = System.getProperty("user.dir");
		System.out.println("Remote working directory: " + pwd);
        }

        public void get(String fileName) throws IOException{
		File file = new File(fileName);
		FileInputStream fileIn = new FileInputStream(file);
		int ch = 0;
		System.out.println("Sending file " + fileName);
		String text = "";
		String cipherText = "";
		String keyword = "security";
		while(ch != -1){
			ch = fileIn.read();
			char c = (char)ch;
			text += c;
			//dout.writeUTF(String.valueOf(ch));
		}
		fileIn.close();
		cipherText = encrypt(text, keyword);
		System.out.println("cipherText = " + cipherText);
		File f = createEncryptedFile(fileName, cipherText);		
		sendEncryptedFile(f);
        }	
	
	public File createEncryptedFile(String fileName, String s) throws IOException{
		File f = new File(fileName + "_se");
		Writer writer = new FileWriter(f);
		BufferedWriter bufferedWriter = new BufferedWriter(writer);
		bufferedWriter.write(s);
		bufferedWriter.close();
		return f;
	}
	
	public void sendEncryptedFile(File f) throws IOException, FileNotFoundException{
		FileInputStream fileIn = new FileInputStream(f);
                int ch = 0;
		while(ch != -1){
                        ch = fileIn.read();
			dout.writeUTF(String.valueOf(ch));
                }	
	}
	
	public String encrypt(String text, String key){
		String res = "";
		int i = 0;
		int j = 0;
		for(i = 0; i < text.length(); i++){
			char c = text.charAt(i);
			if(c < 'a' || c > 'z') continue;
			res += (char)((c + key.charAt(j) - 2 * 'a') % 26 + 'a');
			j = ++j % key.length();
		}
		return res;
	}
	
	public int checkArgs(String[] args){
                if(args.length != 1){
                        System.err.println("Usage: java FtpServ 9844");
                        System.exit(1);
                }

                int portNumber = Integer.parseInt(args[0]); //9844

                if(portNumber != 9844){
                    System.err.println("Usage: java FtpServ 9844");
                    System.exit(1);
                }
		return portNumber;
	
	}
	
	public void readCommand(String command) throws IOException{
        	if(command.equals("ls")){
                	System.out.println("FROM CLIENT: " + command);
			ls();
                }
                else if(command.equals("lls")){
                	System.out.println("FROM CLIENT: " + command);
                	//Do nothing
                }
                else if(command.contains("mkdir ")){
                	System.out.println("FROM CLIENT: " + command);
			String dirName = din.readUTF();
			mkdir(dirName);
                }
                else if(command.contains("cd")){
                	System.out.println("FROM CLIENT: " + command);
                        String dirName = din.readUTF();
                        cd(dirName);
                }
                else if(command.equals("pwd")){
                	System.out.println("FROM CLIENT: " + command);
                        pwd();
                }
                else if(command.contains("get ")){
                	System.out.println("FROM CLIENT: " + command);
                        String fileName = din.readUTF();
                        get(fileName);
                }
                else{
                	System.out.println("FROM CLIENT: " + command);
                	System.out.println("Invalid command");
                }
	
	}
	
	public static void main(String[] args) throws Exception{
		FtpServ argChecker = new FtpServ();
		int portNumber = argChecker.checkArgs(args);
		String command;
		String capitalized;
		while(true){	
			System.out.println("Attempting to connect with client");
			ServerSocket serverSocket = new ServerSocket(portNumber);
			Socket clientSocket = serverSocket.accept();
			FtpServ server = new FtpServ(clientSocket);
			System.out.println("Connected to server 9844");
			while((command = server.din.readUTF()) != null){
				if(command.equals("quit")){
					System.out.println("FROM CLIENT: " + command);
					System.out.println("The client has closed");
					break;
				}
				else{
					server.readCommand(command);
				}
				
			}
			serverSocket.close();

		}
		
	}
}
