package es.deusto.ingenieria.sd.strava.FacebookExternalService;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

import es.deusto.ingenieria.sd.strava.server.gateways.IProviderGateway;

public class FacebookService extends Thread implements IProviderGateway{
	private DataInputStream in;
	private DataOutputStream out;
	private Socket tcpSocket;
    private static final String DELIMITER = "#";
    private static HashMap<String, String> userCredentials = new HashMap<>();


    
    public FacebookService(Socket socket) {
    	userCredentials.put("jane.smith@example.com", "jane.smith");
        userCredentials.put("bob.anderson@example.com", "bob.anderson");
		try {
			this.tcpSocket = socket;
		    this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
			this.start();
		} catch (IOException e) {
			System.err.println("# FacebookService - TCPConnection IO error:" + e.getMessage());
		}
	}

	public void run() {
		try {
			String data = this.in.readUTF();			
			System.out.println("   - FacebookService - Received data from '" + tcpSocket.getInetAddress().getHostAddress() + ":" + tcpSocket.getPort() + "' -> '" + data + "'");					
			data = processRequest(data);
			this.out.writeUTF(data);					
			System.out.println("   - FacebookService - Sent data to '" + tcpSocket.getInetAddress().getHostAddress() + ":" + tcpSocket.getPort() + "' -> '" + data.toUpperCase() + "'");
		} catch (EOFException e) {
			System.err.println("   # FacebookService - TCPConnection EOF error" + e.getMessage());
		} catch (IOException e) {
			System.err.println("   # FacebookService - TCPConnection IO error:" + e.getMessage());
		} finally {
			try {
				tcpSocket.close();
			} catch (IOException e) {
				System.err.println("   # FacebookService - TCPConnection IO error:" + e.getMessage());
			}
		}
	}
    
    
    public String processRequest(String request) {
    	Boolean r;
        StringTokenizer tokenizer = new StringTokenizer(request, DELIMITER);
        String action = tokenizer.nextToken();
        
        //RequestHandler rh = new RequestHandler();
        switch (action) {
            case "register_mandatory":
                r= register(tokenizer.nextToken(), tokenizer.nextToken());
            case "validate_password":
                r=validatePassword(tokenizer.nextToken(), tokenizer.nextToken());
            case "validate_email":
                r=validateEmail(tokenizer.nextToken());
            default:
                r=false;
        }
        handleRequest();
        return r.toString();
    }
    
    public boolean register(String email, String password) {
        if (!userCredentials.containsKey(email)) {
            userCredentials.put(email, password);
            return true;
        }
        return false;
    }

    public boolean validatePassword(String email, String password) {
        if (userCredentials.containsKey(email)) {
            String storedPassword = userCredentials.get(email);
            if (password.equals(storedPassword)) {
                return true;
            }
        }
        return false;
    }

    public boolean validateEmail(String email) {
        if (userCredentials.containsKey(email)) {
            return false;
        }
        return true;
    }
    
    
    public void handleRequest() {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

            String request = in.readLine(); // Read input from client

            System.out.println("Received request from client: " + request);

            in.close(); // Close the input stream
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        }
                
    }

    
}