import java.io.*;
import java.net.*;

public class FtpCli{
	private String hostName;
	private int portNumber;
	Socket socketClient;
	
	DataInputStream din;
	DataOutputStream dout;
	BufferedReader br; 
	
	public FtpCli(String hostName, int portNumber){
		this.hostName = hostName;
		this.portNumber = portNumber;
	}

	public void connect() throws UnknownHostException, IOException{
		System.out.println("Attempting to connect to " + hostName +":" + portNumber);
		socketClient = new Socket(hostName, portNumber);
		System.out.println("Successfully connected to " + hostName +":" + portNumber);
		din = new DataInputStream(socketClient.getInputStream());
		dout = new DataOutputStream(socketClient.getOutputStream());
		br = new BufferedReader(new InputStreamReader(System.in));

	}
	
	public void readResponse() throws IOException{
		String command;
		System.out.print("ftp> ");
		while((command = br.readLine()) != null){
			System.out.print("ftp> ");
			if(command.equals("ls")){
				dout.writeUTF(command);
                        }
			else if(command.equals("lls")){
				lls();
				System.out.print("ftp> ");
				dout.writeUTF(command);
			}
			else if(command.contains("mkdir ")){
				String name;
				name = command.substring(6,command.length());
						
				dout.writeUTF(command);
				dout.writeUTF(name);
			}
                        else if(command.contains("cd ")){
				String name;
				name = command.substring(3,command.length());
				
				dout.writeUTF(command);
				dout.writeUTF(name);
                        }
			else if(command.equals("pwd")){
				dout.writeUTF(command);
			}
			else if(command.contains("get ")){
				String fileName;
				fileName = command.substring(4,command.length());
				
				dout.writeUTF(command);
				get(fileName);
			}			
                        else if(command.equals("quit")){
        			System.out.println("Closing client");
	                        dout.writeUTF(command);
				break;
                        }
		}
		socketClient.close();		
	}	
	
	public void lls() throws IOException{
                File dir = new File(System.getProperty("user.dir"));
                String files[] = dir.list();
                for(String file: files){
                        System.out.print(file + " ");
                }
                System.out.println("");		
	}
	
	public void get(String fileName) throws IOException{
		dout.writeUTF(fileName);
		File file = new File(fileName + "_ce");
		FileOutputStream fileOutput = new FileOutputStream(file);
		int ch = 0;
		String keyword = "security";
		String temp;
		while(ch != -1){
			temp = din.readUTF();
			ch = Integer.parseInt(temp);
			if(ch != -1){
				fileOutput.write(ch);
			}
		}
		fileOutput.close();
		System.out.println("File " + fileName + " received.");
		File f = createDecryptedFile(fileName, file);
		System.out.print("ftp> ");
	}
		
	public File createDecryptedFile(String fileName, File f) throws IOException{
		String encryptedText = readEncryptedFile(f);
                
		String keyword = "security";
		String decryptedText = decryptFile(encryptedText, keyword);
		System.out.println("decryptedText: " + decryptedText);

		File file = new File(fileName + "_cd");
		Writer writer = new FileWriter(file);
		BufferedWriter bufferWriter = new BufferedWriter(writer);
		bufferWriter.write(decryptedText);
		bufferWriter.close();
		return file;
	}
	
	public String readEncryptedFile(File f) throws IOException{
		FileReader inputFile = new FileReader(f);
                BufferedReader bufferReader = new BufferedReader(inputFile);
                String line;
                String cipherText = "";
                while((line = bufferReader.readLine()) != null){
                        cipherText += line;
                }
                System.out.println("cipherText = " + cipherText);
                bufferReader.close();
		return cipherText;
	}	
	
	public String decryptFile(String text, String key){
		String res = "";
		int i = 0;
		int j = 0;
		for(i = 0; i < text.length(); i++){
			char c = text.charAt(i);
			if(c < 'a' || c > 'z') continue;
			res += (char)((c - key.charAt(j) + 26) % 26 + 
'a');
			j = ++j % key.length();
		}
		return res;
	}
	
	public void checkArgs(String[] args){
		if(args.length != 2){
			System.err.println("Wrong # of command line args. Usage: java FtpCli bingsuns.binghamton.edu 9844");
			System.exit(1);
		}
		
		if(!(this.hostName.equals("bingsuns.binghamton.edu"))){
			System.err.println("Hostname is wrong. Usage: java FtpCli bingsuns.binghamton.edu 9844");
			System.exit(1);
		}

		if(this.portNumber != 9844){
			System.err.println("PortNumber is wrong. Usage: java FtpCli bingsuns.binghamton.edu 9844");
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception{
		FtpCli client = new FtpCli("bingsuns.binghamton.edu", 9844);
		client.checkArgs(args);
		client.connect();
		client.readResponse();
	}
}
