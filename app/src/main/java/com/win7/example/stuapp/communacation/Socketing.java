package com.win7.example.stuapp.communacation;

import android.util.Log;

import com.win7.example.stuapp.interfaces.Manager;
import com.win7.example.stuapp.interfaces.SocketInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;


public class Socketing implements SocketInterface
{	//we must check the ip via cmd command line ipcinfig and take our new ip and try to connect to it
	private static final String AUTHENTICATION_SERVER_ADDRESS = "http://10.0.0.9/test/index.php";
	private int listeningPort = 0;
	private static final String HTTP_REQUEST_FAILED = null;
	//hash map have is like array list in our program we put the socket and the inetadrees in  it  and he socket (the socket and his inet adress )
	private HashMap<InetAddress, Socket> sockets = new HashMap<InetAddress, Socket>();
	private ServerSocket serverSocket = null;
	private boolean listening;
	//creating a  thread class  to do the thread in it when we recsive the connetion from the socket
	//the replay of the socket goes to here so we can rread the replay
	private class ReceiveConnection extends Thread {
		Socket clientSocket = null;
		public ReceiveConnection(Socket socket) 
		{		///saving the succesful socekt replay in the hashmap
			//making the client socket is the socket that was succefuly connected
			this.clientSocket = socket;
			Socketing.this.sockets.put(socket.getInetAddress(), socket);
		}
		
		@Override
		public void run() {
			 try {
				 //reading from the socket input the socket it means the replay of the socket
				BufferedReader in = new BufferedReader(
						    new InputStreamReader(clientSocket.getInputStream()));
				String inputLine;
				
				 while ((inputLine = in.readLine()) != null) 
				 {///while we are getting a replay
					 if (inputLine.equals("exit") == false)
					 {

					 }
					 else
					 {		///when the socket returns exit
						 clientSocket.shutdownInput();
						 clientSocket.shutdownOutput();
						 clientSocket.close();
						 //removing the socket and socket inet adress from the hash map
						 Socketing.this.sockets.remove(clientSocket.getInetAddress());
					 }						 
				 }		
				
			} catch (IOException e) {
				Log.println(Log.ERROR,"error in socket reading replay ","ReceiveConnection.run: when receiving connection ");
			}			
		}	
	}
	///empty constactoor
	public Socketing(Manager appManager) {	
	}
	
	///send hhtp request so the php page can read it we connect to the php and send our data to it via this method
	//sending the data the we get as a string from the MService class to the php
	public String sendHttpRequest(String params)
	{		
		URL url;
		String result = new String();
		try 
		{		///the url to the php page that we want to send the data to
			url = new URL(AUTHENTICATION_SERVER_ADDRESS);
			///opining the http connection as opining the connection to the php page
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			///write the data to the print write so we can send them to the php
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			///sending the data to the php
			out.println(params);
			//cloing the printwriter
			out.close();
			///reading the php replay via getting it with the bufferreader
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							connection.getInputStream()));
			String inputLine;
			//while the is a data to get from the php
			//as long as the php sends data
			while ((inputLine = in.readLine()) != null) {
				//putting the replayed data in resualt varibale
				result = result.concat(inputLine);				
			}
			in.close();			
		} 
		catch (MalformedURLException e) {
				Log.println(Log.ERROR," error ",e.getMessage());
			
		} 
		catch (IOException e) {
				Log.println(Log.ERROR," error ",e.getMessage());
		}			
		
		if (result.length() == 0) {
			result = HTTP_REQUEST_FAILED;
		}
		
		return result;
		
	
	}


			///starting the connection to the ip adrres
	public int startListening(int portNo) 
	{
		listening = true;
		
		try {
			///try to connecto the the port wwe get
			serverSocket = new ServerSocket(portNo);
			this.listeningPort = portNo;
		} catch (IOException e) {			
			
			Log.println(Log.ERROR," error ",e.getMessage());
			this.listeningPort = 0;
			return 0;
		}

		while (listening) {
			try {
				new ReceiveConnection(serverSocket.accept()).start();
				
			} catch (IOException e) {
					Log.println(Log.ERROR," error ",e.getMessage());
				return 2;
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {			
				Log.println(Log.ERROR," error ",e.getMessage());
				return 3;
		}
		
		
		return 1;
	}
	
	
	public void stopListening() 
	{
		this.listening = false;
	}
	
	public void exit() 
	{			
		for (Iterator<Socket> iterator = sockets.values().iterator(); iterator.hasNext();) 
		{
			Socket socket = (Socket) iterator.next();
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) 
			{				
			}		
		}
		
		sockets.clear();
		this.stopListening();
	}


	public int getListeningPort() {
		
		return this.listeningPort;
	}	

}
