package com.win7.example.stuapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.win7.example.stuapp.Data_type.FriendsInformation;
import com.win7.example.stuapp.Data_type.MessagesInformation;
import com.win7.example.stuapp.interfaces.Manager;
import com.win7.example.stuapp.service.MService;
import com.win7.example.stuapp.toolBox.DataStorage;
import com.win7.example.stuapp.toolBox.FriendController;

import java.io.UnsupportedEncodingException;


public class WritingMessage extends Activity {


	public String username;
	private EditText messageText;
	private EditText messageHistoryText;
	private Button sendMessageButton;
	private Manager imService;
	private FriendsInformation friend = new FriendsInformation();
	private DataStorage localstoragehandler; 
	private Cursor dbCursor;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((MService.IMBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(WritingMessage.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		
		super.onCreate(savedInstanceState);	   
		
		setContentView(R.layout.message);		
		messageHistoryText = (EditText) findViewById(R.id.messageHistory);
		messageText = (EditText) findViewById(R.id.message);
		messageText.requestFocus();

		sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		Bundle extras = this.getIntent().getExtras();
		friend.userName = extras.getString(FriendsInformation.USERNAME);
		friend.ip = extras.getString(FriendsInformation.IP);
		friend.port = extras.getString(FriendsInformation.PORT);
		String msg = extras.getString(MessagesInformation.MESSAGETEXT);
		setTitle("Chatting with -> " + friend.userName);
		localstoragehandler = new DataStorage(this);
		dbCursor = localstoragehandler.get(friend.userName, MService.USERNAME );
		if (dbCursor.getCount() > 0){
		int noOfScorer = 0;
		dbCursor.moveToFirst();
		    while ((!dbCursor.isAfterLast())&&noOfScorer<dbCursor.getCount()) 
		    {
		        noOfScorer++;

				this.appendToMessageHistory(dbCursor.getString(2) , dbCursor.getString(3));
		        dbCursor.moveToNext();
		    }
		}
		localstoragehandler.close();
		
		if (msg != null) 
		{
			this.appendToMessageHistory(friend.userName , msg);
			((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((friend.userName+msg).hashCode());
		}
		
		sendMessageButton.setOnClickListener(new OnClickListener(){
			CharSequence message;
			Handler handler = new Handler();
			public void onClick(View arg0) {
				message = messageText.getText();

				if (message.length()>0) 
				{		
					appendToMessageHistory(imService.getUsername(), message.toString());
					
					localstoragehandler.insert(imService.getUsername(), friend.userName, message.toString());

					messageText.setText("");
					Thread thread = new Thread(){					
						public void run() {
							try {
								if (imService.sendMessage(imService.getUsername(), friend.userName, message.toString()) == null)
								{
									
									handler.post(new Runnable(){	

										public void run() {
											
									        Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();								
										}
										
									});
								}
							} catch (UnsupportedEncodingException e) {
								Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

							Log.println(Log.ERROR,"error",e.getMessage());
							}
						}						
					};
					thread.start();
										
				}
				
			}});
		
		messageText.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) 
			{
				if (keyCode == 66){// ENTER CODE
					sendMessageButton.performClick();
					return true;
				}
				return false;
			}
			
			
		});
				
	}


	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(messageReceiver);
		unbindService(mConnection);
		FriendController.setActiveFriend(null);
		
	}

	@Override
	protected void onResume() 
	{		
		super.onResume();
		bindService(new Intent(WritingMessage.this, MService.class), mConnection , Context.BIND_AUTO_CREATE);
				
		IntentFilter i = new IntentFilter();
		i.addAction(MService.TAKE_MESSAGE);
		
		registerReceiver(messageReceiver, i);
		
		FriendController.setActiveFriend(friend.userName);		
		
		
	}
	
	
	public class  MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{		
			Bundle extra = intent.getExtras();
			String username = extra.getString(MessagesInformation.USERID);			
			String message = extra.getString(MessagesInformation.MESSAGETEXT);
			
			if (username != null && message != null)
			{
				if (friend.userName.equals(username)) {
					appendToMessageHistory(username, message);
					localstoragehandler.insert(username,imService.getUsername(), message);
					
				}
				else {
					if (message.length() > 15) {
						message = message.substring(0, 15);
					}
					Toast.makeText(WritingMessage.this,  username + " said'"+message + "'",Toast.LENGTH_SHORT).show();
				}
			}			
		}
		
	};
	private MessageReceiver messageReceiver = new MessageReceiver();
	
	public  void appendToMessageHistory(String username, String message) {
		if (username != null && message != null) {
			messageHistoryText.append(username + ":\n");								
			messageHistoryText.append(message + "\n");
		}
	}
	
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (localstoragehandler != null) {
	    	localstoragehandler.close();
	    }
	    if (dbCursor != null) {
	    	dbCursor.close();
	    }
	}
	

}
