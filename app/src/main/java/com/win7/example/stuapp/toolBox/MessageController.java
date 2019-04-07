package com.win7.example.stuapp.toolBox;

import com.win7.example.stuapp.Data_type.MessagesInformation;

// This class can store friendInfo and check userkey and username combination according to its stored data
public class MessageController 
{
	
	private static MessagesInformation[] messagesInfo = null;
	
	public static void setMessagesInfo(MessagesInformation[] messageInfo)
	{
		MessageController.messagesInfo = messageInfo;
	}
	
	
	
	public static MessagesInformation checkMessage(String username)
	{
		MessagesInformation result = null;
		if (messagesInfo != null) 
		{
			for (int i = 0; i < messagesInfo.length;) 
			{
				
					result = messagesInfo[i];
					break;
								
			}			
		}		
		return result;
	}
	
	



	public static MessagesInformation getMessageInfo(String username)
	{
		MessagesInformation result = null;
		if (messagesInfo != null)
		{
			for (int i = 0; i < messagesInfo.length;)
			{
					result = messagesInfo[i];
					break;

			}
		}
		return result;
	}






	public static MessagesInformation[] getMessagesInfo() {
		return messagesInfo;
	}



	
	
	

}
