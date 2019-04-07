package com.win7.example.stuapp.toolBox;

import android.util.Log;

import com.win7.example.stuapp.Data_type.FriendsInformation;
import com.win7.example.stuapp.Data_type.MessagesInformation;
import com.win7.example.stuapp.Data_type.StatusInformation;
import com.win7.example.stuapp.interfaces.Updater;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Vector;

public class XmlHandler extends DefaultHandler
{
		private String userKey = new String();
		private Updater updater;
		
		public XmlHandler(Updater updater) {
			super();
			this.updater = updater;
		}

		private Vector<FriendsInformation> mFriends = new Vector<FriendsInformation>();
		private Vector<FriendsInformation> mOnlineFriends = new Vector<FriendsInformation>();
		private Vector<FriendsInformation> mUnapprovedFriends = new Vector<FriendsInformation>();
		private Vector<MessagesInformation> mUnreadMessages = new Vector<MessagesInformation>();
		
		public void endDocument() throws SAXException 
		{
			FriendsInformation[] friends = new FriendsInformation[mFriends.size() + mOnlineFriends.size()];
			MessagesInformation[] messages = new MessagesInformation[mUnreadMessages.size()];

			
			int onlineFriendCount = mOnlineFriends.size();			
			for (int i = 0; i < onlineFriendCount; i++) 
			{				
				friends[i] = mOnlineFriends.get(i);
			}
			
						
			int offlineFriendCount = mFriends.size();			
			for (int i = 0; i < offlineFriendCount; i++) 
			{
				friends[i + onlineFriendCount] = mFriends.get(i);
			}
			
			int unApprovedFriendCount = mUnapprovedFriends.size();
			FriendsInformation[] unApprovedFriends = new FriendsInformation[unApprovedFriendCount];
			
			for (int i = 0; i < unApprovedFriends.length; i++) {
				unApprovedFriends[i] = mUnapprovedFriends.get(i);
			}
			
			int unreadMessagecount = mUnreadMessages.size();
			Log.println(Log.ERROR,"unreadMessagecount","mUnreadMessages="+unreadMessagecount);
			for (int i = 0; i < unreadMessagecount; i++) 
			{
				messages[i] = mUnreadMessages.get(i);
				Log.println(Log.ERROR,"message log","i="+i);
			}
			
			this.updater.updateData(messages, friends, unApprovedFriends, userKey);
			super.endDocument();
		}		
		
		public void startElement(String uri, String localName, String name,Attributes attributes) throws SAXException 
		{				
			if (localName == "friend")
			{
				FriendsInformation friend = new FriendsInformation();
				friend.userName = attributes.getValue(FriendsInformation.USERNAME);
				String status = attributes.getValue(FriendsInformation.STATUS);
				friend.ip = attributes.getValue(FriendsInformation.IP);
				friend.port = attributes.getValue(FriendsInformation.PORT);
				friend.userKey = attributes.getValue(FriendsInformation.USER_KEY);
				
				if (status != null && status.equals("online"))
				{					
					friend.status = StatusInformation.ONLINE;
					mOnlineFriends.add(friend);
				}
				else if (status.equals("unApproved"))
				{
					friend.status = StatusInformation.UNAPPROVED;
					mUnapprovedFriends.add(friend);
				}	
				else
				{
					friend.status = StatusInformation.OFFLINE;
					mFriends.add(friend);	
				}											
			}
			else if (localName == "user") {
				this.userKey = attributes.getValue(FriendsInformation.USER_KEY);
			}
			else if (localName == "message") {
				MessagesInformation message = new MessagesInformation();
				message.userid = attributes.getValue(MessagesInformation.USERID);
				message.sendt = attributes.getValue(MessagesInformation.SENDT);
				message.messagetext = attributes.getValue(MessagesInformation.MESSAGETEXT);
				Log.println(Log.ERROR,"MessageLOG", message.userid + message.sendt + message.messagetext);
				mUnreadMessages.add(message);
			}
			super.startElement(uri, localName, name, attributes);
		}

		@Override
		public void startDocument() throws SAXException {			
			this.mFriends.clear();
			this.mOnlineFriends.clear();
			this.mUnreadMessages.clear();
			super.startDocument();
		}
		
		
}

