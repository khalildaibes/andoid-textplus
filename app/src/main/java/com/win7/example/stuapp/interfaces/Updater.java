package com.win7.example.stuapp.interfaces;
import com.win7.example.stuapp.Data_type.FriendsInformation;
import com.win7.example.stuapp.Data_type.MessagesInformation;


public interface Updater {
	public void updateData(MessagesInformation[] messages, FriendsInformation[] friends, FriendsInformation[] unApprovedFriends, String userKey);

}
