package com.win7.example.stuapp;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.win7.example.stuapp.interfaces.Manager;
import com.win7.example.stuapp.service.MService;
import com.win7.example.stuapp.toolBox.FriendController;
import com.win7.example.stuapp.Data_type.FriendsInformation;
import com.win7.example.stuapp.Data_type.StatusInformation;


public class ListOfFriends extends ListActivity 
{
	private static final int ADD_NEW_FRIEND_ID = Menu.FIRST;
	private static final int EXIT_APP_ID = Menu.FIRST + 1;
	private Manager  imService = null;
	private FriendListAdapter friendAdapter;
	
	public String ownusername = new String();

	private class FriendListAdapter extends BaseAdapter 
	{		
		class ViewHolder {
			TextView text;
			ImageView icon;
		}
		private LayoutInflater mInflater;
		private Bitmap mOnlineIcon;
		private Bitmap mOfflineIcon;		

		private FriendsInformation[] friends = null;


		public FriendListAdapter(Context context) {
			super();			

			mInflater = LayoutInflater.from(context);
			mOnlineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.online_icon);
			mOfflineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.offline_icon);

		}

		public void setFriendList(FriendsInformation[] friends)
		{
			this.friends = friends;
		}


		public int getCount() {		

			return friends.length;
		}
		

		public FriendsInformation getItem(int position) {			

			return friends[position];
		}

		public long getItemId(int position) {

			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

		
			ViewHolder holder;


			if (convertView == null) 
			{
				convertView = mInflater.inflate(R.layout.friend_list_screen, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);                                       

				convertView.setTag(holder);
			}   
			else {

				holder = (ViewHolder) convertView.getTag();
			}


			holder.text.setText(friends[position].userName);
			holder.icon.setImageBitmap(friends[position].status == StatusInformation.ONLINE ? mOnlineIcon : mOfflineIcon);

			return convertView;
		}

	}

	public class MessageReceiver extends  BroadcastReceiver  {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.println(Log.ERROR,"not error ","Broadcast receiver received a message");
			Bundle extra = intent.getExtras();
			if (extra != null)
			{
				String action = intent.getAction();
				if (action.equals(MService.FRIEND_LIST_UPDATED))
				{

					ListOfFriends.this.updateData(FriendController.getFriendsInfo(), 
												FriendController.getUnapprovedFriendsInfo());
					
				}
			}
		}

	};
	public MessageReceiver messageReceiver = new MessageReceiver();

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((MService.IMBinder)service).getService();      
			
			FriendsInformation[] friends = FriendController.getFriendsInfo();
			if (friends != null) {    			
				ListOfFriends.this.updateData(friends, null); 
			}    
			
			setTitle(imService.getUsername() + "'s friend list");
			ownusername = imService.getUsername();
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(ListOfFriends.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
	


	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_listoffriends);
        
		friendAdapter = new FriendListAdapter(this);
		
		


	}
	public void updateData(FriendsInformation[] friends, FriendsInformation[] unApprovedFriends)
	{
		if (friends != null) {
			friendAdapter.setFriendList(friends);	
			setListAdapter(friendAdapter);				
		}				
		
		if (unApprovedFriends != null) 
		{
			NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			if (unApprovedFriends.length > 0)
			{					
				String tmp = new String();
				for (int j = 0; j < unApprovedFriends.length; j++) {
					tmp = tmp.concat(unApprovedFriends[j].userName).concat(",");			
				}
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
		    	.setSmallIcon(R.drawable.notification)
		    	.setContentTitle(getText(R.string.new_friend_request_exist));
				Intent i = new Intent(this, WritingListFriends.class);
				i.putExtra(FriendsInformation.FRIEND_LIST, tmp);				

				PendingIntent contentIntent = PendingIntent.getActivity(this, 0,i, 0);

				mBuilder.setContentText("You have new friend request(s) Amigooo");
				mBuilder.setContentIntent(contentIntent);

				
				NM.notify(R.string.new_friend_request_exist, mBuilder.build());			
			}
			else
			{
				NM.cancel(R.string.new_friend_request_exist);			
			}
		}

	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);		

		Intent i = new Intent(this, WritingMessage.class);
		FriendsInformation friend = friendAdapter.getItem(position);
		i.putExtra(FriendsInformation.USERNAME, friend.userName);
		i.putExtra(FriendsInformation.PORT, friend.port);
		i.putExtra(FriendsInformation.IP, friend.ip);		
		startActivity(i);
	}




	@Override
	protected void onPause() 
	{
		unregisterReceiver(messageReceiver);		
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
			
		super.onResume();
		bindService(new Intent(ListOfFriends.this, MService.class), mConnection , Context.BIND_AUTO_CREATE);

		IntentFilter i = new IntentFilter();
		i.addAction(MService.FRIEND_LIST_UPDATED);

		registerReceiver(messageReceiver, i);			
		

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);		

		menu.add(0, ADD_NEW_FRIEND_ID, 0, R.string.add_new_friend);
		
		menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);		
		
		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{		

		switch(item.getItemId()) 
		{	  
			case ADD_NEW_FRIEND_ID:
			{
				Intent i = new Intent(ListOfFriends.this, AddFriend.class);
				startActivity(i);
				return true;
			}		
			case EXIT_APP_ID:
			{
				imService.exit();
				finish();
				return true;
			}			
		}

		return super.onMenuItemSelected(featureId, item);		
	}	
	
	@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {

			super.onActivityResult(requestCode, resultCode, data);

		}
}
