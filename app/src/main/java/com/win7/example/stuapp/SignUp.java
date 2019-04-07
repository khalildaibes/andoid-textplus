package com.win7.example.stuapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.win7.example.stuapp.service.MService;


public class SignUp extends Activity {
	

	private static final String SERVER_RES_RES_SIGN_UP_SUCCESFULL = "1";
	private static final String SERVER_RES_SIGN_UP_USERNAME_CRASHED = "2";
	
	
	
	private EditText usernameText;
	private EditText passwordText;
	private EditText eMailText;
	private EditText passwordAgainText;
	private MService imService = new MService() ;
	private Handler handler = new Handler();
	
	private ServiceConnection mConnection = new ServiceConnection() {


		public void onServiceConnected(ComponentName className, IBinder service) {
            imService = (MService) ((MService.IMBinder)service).getService();


        }

        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(SignUp.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };

	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);        
	        setContentView(R.layout.activity_signup);
	        setTitle("Sign up to textplus");
	        Button signUpButton = (Button) findViewById(R.id.signUp);
	        Button cancelButton = (Button) findViewById(R.id.cancel_signUp);
	        usernameText = (EditText) findViewById(R.id.userName);
	        passwordText = (EditText) findViewById(R.id.password);  
	        passwordAgainText = (EditText) findViewById(R.id.passwordAgain);  
	        eMailText = (EditText) findViewById(R.id.email);
	        
	        signUpButton.setOnClickListener(new OnClickListener(){
				public void onClick(View arg0) 
				{						
					if (usernameText.length() > 0 &&		
						passwordText.length() > 0 && 
						passwordAgainText.length() > 0 &&
						eMailText.length() > 0
						)
					{
						
						
						if (passwordText.getText().toString().equals(passwordAgainText.getText().toString())){
						
							if (usernameText.length() >= 5 && passwordText.length() >= 5) {
							
									Thread thread = new Thread(){
										String result = new String();
										@Override
										public void run() {
											result = imService.signUpUser(usernameText.getText().toString(), passwordText.getText().toString(), eMailText.getText().toString());
		
											handler.post(new Runnable(){
		
												public void run() {
													if (result.equals(SERVER_RES_RES_SIGN_UP_SUCCESFULL)) {
														Toast.makeText(getApplicationContext(),R.string.signup_successfull, Toast.LENGTH_LONG).show();
														
													}
													else if (result.equals(SERVER_RES_SIGN_UP_USERNAME_CRASHED)){
														Toast.makeText(getApplicationContext(),R.string.signup_username_crashed, Toast.LENGTH_LONG).show();
														
													}
													else   
													{
														Toast.makeText(getApplicationContext(),result, Toast.LENGTH_LONG).show();
														
													}			
												}
		
											});
										}
		
									};
									thread.start();
							}
							else{
								Toast.makeText(getApplicationContext(),R.string.username_and_password_length_short, Toast.LENGTH_LONG).show();
								
							}							
						}
						else {
							Toast.makeText(getApplicationContext(),R.string.signup_type_same_password_in_password_fields, Toast.LENGTH_LONG).show();
							
						}
						
					}
					else {
						Toast.makeText(getApplicationContext(),R.string.signup_fill_all_fields, Toast.LENGTH_LONG).show();
						
						
					}				
				}       	
	        });
	        
	        cancelButton.setOnClickListener(new OnClickListener(){
				public void onClick(View arg0) 
				{						
					finish();					
				}	        	
	        });
	        
	        
	    }
	
	
	
	@Override
	protected void onResume() {
		bindService(new Intent(SignUp.this, MService.class), mConnection , Context.BIND_AUTO_CREATE);
		   
		super.onResume();
	}
	
	@Override
	protected void onPause() 
	{
		unbindService(mConnection);
		super.onPause();
	}
	
	

}
