package com.win7.example.stuapp.toolBox;

import com.win7.example.stuapp.Data_type.FriendsInformation;

//This class can store friendInfo and check userkey and username combination according to its stored data
public class FriendController 
{
	
	private static FriendsInformation[] friendsInfo = null;
	private static FriendsInformation[] unapprovedFriendsInfo = null;
	private static String activeFriend;
	
	public static void setFriendsInfo(FriendsInformation[] friendInfo)
	{
		FriendController.friendsInfo = friendInfo;
	}
	


	public static FriendsInformation checkFriend(String username, String userKey)
	{
		FriendsInformation result = null;
		if (friendsInfo != null)
		{
			for (int i = 0; i < friendsInfo.length; i++)
			{
				if ( friendsInfo[i].userName.equals(username) && friendsInfo[i].userKey.equals(userKey))
				{
					result = friendsInfo[i];
					break;
				}
			}
		}
		return result;
	}
	
	public static void setActiveFriend(String friendName){
		activeFriend = friendName;
	}
	
	public static String getActiveFriend()
	{
		return activeFriend;
	}



	public static FriendsInformation getFriendInfo(String username)
	{
		FriendsInformation result = null;
		if (friendsInfo != null)
		{
			for (int i = 0; i < friendsInfo.length; i++)
			{
				if ( friendsInfo[i].userName.equals(username) )
				{
					result = friendsInfo[i];
					break;
				}
			}
		}
		return result;
	}



	public static void setUnapprovedFriendsInfo(FriendsInformation[] unapprovedFriends) {
		unapprovedFriendsInfo = unapprovedFriends;		
	}



	public static FriendsInformation[] getFriendsInfo() {
		return friendsInfo;
	}



	public static FriendsInformation[] getUnapprovedFriendsInfo() {
		return unapprovedFriendsInfo;
	}
	
	
	

}
