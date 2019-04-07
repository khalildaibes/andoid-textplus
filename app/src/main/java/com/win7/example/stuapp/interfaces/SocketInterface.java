package com.win7.example.stuapp.interfaces;

public  interface SocketInterface {

	public String sendHttpRequest(String params);
	public int startListening(int port);
	public void stopListening();
	public void exit();
	public int getListeningPort();

}
