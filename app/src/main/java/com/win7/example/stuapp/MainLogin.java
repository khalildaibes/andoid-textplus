package  com.win7.example.stuapp;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.win7.example.stuapp.interfaces.Manager;
import com.win7.example.stuapp.service.MService;

import java.io.UnsupportedEncodingException;


public class MainLogin extends Activity {

	public static final String AUTHENTICATION_FAILED = "0";
	public static final String FRIEND_LIST = "FRIEND_LIST";
	private EditText usernameText;
    private EditText passwordText;
    
    private Manager imService;
    public static final int SIGN_UP_ID = Menu.FIRST;
    public static final int EXIT_APP_ID = Menu.FIRST + 1;
   

   
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            
            imService = ((MService.IMBinder)service).getService();  
            
            if (imService.isUserAuthenticated() == true)
            {
            	Intent i = new Intent(MainLogin.this, ListOfFriends.class);																
				startActivity(i);
				MainLogin.this.finish();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(MainLogin.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    

       
    	startService(new Intent(MainLogin.this,  MService.class));			
	
               
        setContentView(R.layout.activity_mainlogin);
        setTitle("Login");
        
        ImageButton loginButton = (ImageButton) findViewById(R.id.button1);
       
        usernameText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);        

        loginButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) 
			{					
				if (imService == null) {
					Toast.makeText(getApplicationContext(),R.string.not_connected_to_service, Toast.LENGTH_LONG).show();
					return;
				}
				else if (imService.isNetworkConnected() == false)
				{
					Toast.makeText(getApplicationContext(),R.string.not_connected_to_network, Toast.LENGTH_LONG).show();
					
					
				}
				else if (usernameText.length() > 0 && 
					passwordText.length() > 0)
				{
					
					Thread loginThread = new Thread(){
						private Handler handler = new Handler();
						@Override
						public void run() {
							String result = null;
							try {
								result = imService.authenticateUser(usernameText.getText().toString(), passwordText.getText().toString());
							} catch (UnsupportedEncodingException e) {
								
								Log.println(Log.ERROR,"error",e.getMessage());
							}
							if (result == null || result.equals(AUTHENTICATION_FAILED)) 
							{
								
								handler.post(new Runnable(){
									public void run() {	
										Toast.makeText(getApplicationContext(),R.string.make_sure_username_and_password_correct, Toast.LENGTH_LONG).show();

									}									
								});
														
							}
							else {
							
								
								handler.post(new Runnable(){
									public void run() {										
										Intent i = new Intent(MainLogin.this, ListOfFriends.class);																	
										startActivity(i);	
										MainLogin.this.finish();
									}									
								});
								
							}
							
						}
					};
					loginThread.start();
					
				}
				else {
					
					Toast.makeText(getApplicationContext(),R.string.fill_both_username_and_password, Toast.LENGTH_LONG).show();
					
				}				
			}       	
        });
        
       
        
    }
    
   
	@Override
	protected void onPause() 
	{
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{		
		bindService(new Intent(MainLogin.this, MService.class), mConnection , Context.BIND_AUTO_CREATE);
	    		
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);
		
		 menu.add(0, SIGN_UP_ID, 0, R.string.sign_up);
		 menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);


		return result;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    
		switch(item.getItemId()) 
	    {
	    	case SIGN_UP_ID:
	    		Intent i = new Intent(MainLogin.this, SignUp.class);
	    		startActivity(i);
	    		return true;
	    	case EXIT_APP_ID:
	    	
	    		return true;
	    }
	       
	    return super.onMenuItemSelected(featureId, item);
	}

	
	
    
    
    
    
    
}