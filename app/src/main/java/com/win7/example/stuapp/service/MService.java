package com.win7.example.stuapp.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.win7.example.stuapp.MainLogin;
import com.win7.example.stuapp.R;
import com.win7.example.stuapp.WritingMessage;
import com.win7.example.stuapp.communacation.Socketing;
import com.win7.example.stuapp.interfaces.Manager;
import com.win7.example.stuapp.interfaces.SocketInterface;
import com.win7.example.stuapp.interfaces.Updater;
import com.win7.example.stuapp.toolBox.FriendController;
import com.win7.example.stuapp.toolBox.DataStorage;
import com.win7.example.stuapp.toolBox.MessageController;
import com.win7.example.stuapp.toolBox.XmlHandler;
import com.win7.example.stuapp.Data_type.FriendsInformation;
import com.win7.example.stuapp.Data_type.MessagesInformation;


public class MService extends Service implements Manager, Updater {

	
	public static String USERNAME;
	public static final String TAKE_MESSAGE = "Take_Message";
	public static final String FRIEND_LIST_UPDATED = "Take Friend List";
	public static final String MESSAGE_LIST_UPDATED = "Take Message List";
	public ConnectivityManager conManager = null; 
	private final int UPDATE_TIME = 12000;

	private String rawFriendList = new String();
	private String rawMessageList = new String();

	SocketInterface socketOperator = new Socketing(this);

	private final IBinder mBinder = new IMBinder();
	private String username;
	private String password;
	private boolean authenticatedUser = false;
	 // timer to take the updated data from server
	private Timer timer;
	

	private DataStorage localstoragehandler; 
	
	private NotificationManager mNM;

	public class IMBinder extends Binder {
		public Manager getService() {
			return MService.this;
		}
		
	}
	   
    @Override
    public void onCreate() 
    {   	
         mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

         localstoragehandler = new DataStorage(this);

    	conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	new DataStorage(this);
    	

		timer = new Timer();   
		
		Thread thread = new Thread()
		{
			@Override
			public void run() {			
				
				
				Random random = new Random();
				int tryCount = 0;
				while (socketOperator.startListening(10000 + random.nextInt(20000))  == 0 )
				{		
					tryCount++; 
					if (tryCount > 10)
					{

						break;
					}
					
				}
			}
		};		
		thread.start();
    
    }



	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}


    private void showNotification(String username, String msg) 
	{       

    	String title = "Textpluse You got a new Message From ~>(" + username + ")";
 				
    	String text = username + ": " + ((msg.length() < 5) ? msg : msg.substring(0, 5)+ "...");
    	
    	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.notification).setContentTitle(title).setContentText(text);

        Intent i = new Intent(this, WritingMessage.class);
        i.putExtra(FriendsInformation.USERNAME, username);
        i.putExtra(MessagesInformation.MESSAGETEXT, msg);	

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

        mBuilder.setContentIntent(contentIntent); 
        mBuilder.setContentText("You Have a New Message Amigo From -> " + username + ": " + msg);    

        mNM.notify((username+msg).hashCode(), mBuilder.build());
    }
	 

	public String getUsername() {
		return this.username;
	}

	
	public String sendMessage(String  username, String  tousername, String message) throws UnsupportedEncodingException 
	{			
		String params = "username="+ URLEncoder.encode(this.username,"UTF-8") +"&password="+ URLEncoder.encode(this.password,"UTF-8") +
						"&to=" + URLEncoder.encode(tousername,"UTF-8") +"&message="+ URLEncoder.encode(message,"UTF-8") +
					"&action="  + URLEncoder.encode("sendMessage","UTF-8")+"&";		
					Log.println(Log.ERROR,"not error its debugging  PARAMS ",params);
	
		return socketOperator.sendHttpRequest(params);		
	}

	
	private String getFriendList() throws UnsupportedEncodingException 	{		

		 rawFriendList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		 if (rawFriendList != null) 
		 {
			this.parseFriendInfo(rawFriendList);
		 }
		 return rawFriendList;
	}
	
	private String getMessageList() throws UnsupportedEncodingException 	{
		 rawMessageList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		 if (rawMessageList != null) {
			 this.parseMessageInfo(rawMessageList);
		 }
		 return rawMessageList;
	}
	
	

	public String authenticateUser(String usernameText, String passwordText) throws UnsupportedEncodingException 
	{
		this.username = usernameText;
		this.password = passwordText;
		this.authenticatedUser = false;
		String result = this.getFriendList();
		if (result != null && !result.equals(MainLogin.AUTHENTICATION_FAILED)) 
		{
			this.authenticatedUser = true;
			rawFriendList = result;
			USERNAME = this.username;
			Intent i = new Intent(FRIEND_LIST_UPDATED);					
			i.putExtra(FriendsInformation.FRIEND_LIST, rawFriendList);
			sendBroadcast(i);
			timer.schedule(new TimerTask()
			{			
				public void run() 
				{
					try {					
						

						Intent i = new Intent(FRIEND_LIST_UPDATED);
						Intent i2 = new Intent(MESSAGE_LIST_UPDATED);
						String tmp = MService.this.getFriendList();
						String tmp2 = MService.this.getMessageList();
						if (tmp != null)
						{
							i.putExtra(FriendsInformation.FRIEND_LIST, tmp);
							sendBroadcast(i);	
							Log.println(Log.ERROR,"not error its debugging ","friend list broadcast sent Amigo   (-_*) ");
						
							if (tmp2 != null)
							{
							i2.putExtra(MessagesInformation.MESSAGE_LIST, tmp2);
							sendBroadcast(i2);	
							Log.println(Log.ERROR,"not error its debugging ","friend list broadcast sent Amigo  (*_*) ");
	
							}
						}
						else {
							Log.println(Log.ERROR,"not error its debugging ","friend list returned null you have no friends  Amigo We Can BE Your Friend ADD me khalil123er and add  sharbelmousa");

						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}					
				}			
			}, UPDATE_TIME, UPDATE_TIME);
		}
		
		return result;		
	}

	public void messageReceived(String username, String message) 
	{				
		
	MessagesInformation msg = MessageController.checkMessage(username);
		if ( msg != null)
		{			
			Intent i = new Intent(TAKE_MESSAGE);
			i.putExtra(MessagesInformation.USERID, msg.userid);			
			i.putExtra(MessagesInformation.MESSAGETEXT, msg.messagetext);			
			sendBroadcast(i);
			String activeFriend = FriendController.getActiveFriend();
			if (activeFriend == null || activeFriend.equals(username) == false) 
			{
				localstoragehandler.insert(username,this.getUsername(), message.toString());
				showNotification(username, message);
			}
			Log.println(Log.ERROR,"not error its debugging ","TAKE_MESSAGE broadcast sent by im service");
	
		}	
		
	}  
	
	private String getAuthenticateUserParams(String usernameText, String passwordText) throws UnsupportedEncodingException 
	{			
		String params = "username=" + URLEncoder.encode(usernameText,"UTF-8") +"&password="+ URLEncoder.encode(passwordText,"UTF-8") +
				"&action="  + URLEncoder.encode("authenticateUser","UTF-8")+"&port="    + 
				
				URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&";		
		
		return params;		
	}

	public void setUserKey(String value) 
	{		
	}

	public boolean isNetworkConnected() {
		return conManager.getActiveNetworkInfo().isConnected();
	}
	
	public boolean isUserAuthenticated(){
		return authenticatedUser;
	}
	
	@Override
	public void onDestroy() {
			Log.println(Log.ERROR,"not error its debugging ","IMService is being destroyed");

		super.onDestroy();
	}
	
	public void exit() 
	{
		timer.cancel();
		socketOperator.exit(); 
		socketOperator = null;
		this.stopSelf();
	}
	
	public String signUpUser(String usernameText, String passwordText,
			String emailText) 
	{
		String params = "username=" + usernameText +	"&password=" + passwordText +
					"&action=" + "signUpUser"+"&email=" + emailText+"&";
		String result = socketOperator.sendHttpRequest(params);		
		return result;
	}

	public String addNewFriendRequest(String friendUsername) 
	{
		String params = "username=" + this.username +"&password=" + this.password +"&action=" + "addNewFriend" +
		"&friendUserName=" + friendUsername +"&";
		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
	}

	public String sendFriendsReqsResponse(String approvedFriendNames,
			String discardedFriendNames) 
	{
		String params = "username=" + this.username +
		"&password=" + this.password +"&action=" + "responseOfFriendReqs"+
		"&approvedFriends=" + approvedFriendNames +"&discardedFriends=" +discardedFriendNames +
		"&";

		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
		
	} 
	
	private void parseFriendInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XmlHandler(MService.this));
		} 
		catch (ParserConfigurationException e) {			
			Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
		}
		catch (SAXException e) {			
					Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
	
		} 
		catch (IOException e) {			
					Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
	
		}	
	}
	private void parseMessageInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XmlHandler(MService.this));
		} 
		catch (ParserConfigurationException e) {			
					Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
	
		}
		catch (SAXException e) {			
					Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
	
		} 
		catch (IOException e) {			
					Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
	
		}	
	}

	public void updateData(MessagesInformation[] messages,FriendsInformation[] friends, FriendsInformation[] unApprovedFriends, String userKey)
	{
		this.setUserKey(userKey);
		MessageController.setMessagesInfo(messages);
			Log.println(Log.ERROR,"not error its debugging SERVICE" ,"messages.length="+messages.length);
		
		int i = 0;
		while (i < messages.length){
			messageReceived(messages[i].userid,messages[i].messagetext);
			i++;
		}
		
		
		FriendController.setFriendsInfo(friends);
		FriendController.setUnapprovedFriendsInfo(unApprovedFriends);
		
	}


	
	
	
	
}