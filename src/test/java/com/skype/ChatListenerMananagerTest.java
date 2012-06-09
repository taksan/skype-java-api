package com.skype;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ChatListenerMananagerTest {
	ChatListenerMock listenerMock = new ChatListenerMock();
	ChatListenerMananager subject = new ChatListenerMananager();
	
	@Test
	public void onMessageOnNewChat_ShouldFireChatCreatedEvent()
	{
		subject.addGlobalChatListener(listenerMock);
		
		User[] usersTime0 = new User[]{User.getInstance("foo")};
		assertEquals(0, listenerMock.chats.size());
		
		subject.handleChatUsers(Chat.getInstance("chatty"), usersTime0);
		assertEquals(1, listenerMock.chats.size());
		
		subject.handleChatUsers(Chat.getInstance("chatty"), usersTime0);
		assertEquals(1, listenerMock.chats.size());
	}
	
	@Test
	public void onMessageOnExistingChat_ShouldFireUserAddedIfNewUserShowsUp()
	{
		subject.addGlobalChatListener(listenerMock);
		User[] usersTime0 = new User[]{User.getInstance("foo")};
		subject.handleChatUsers(Chat.getInstance("chatty"), usersTime0);
		
		User[] usersTime1 = new User[]{
				User.getInstance("foo"), 
				User.getInstance("baz")};
		subject.handleChatUsers(Chat.getInstance("chatty"), usersTime1);
		
		assertEquals("baz", listenerMock.consumeLastAddeUser());
		
		subject.handleChatUsers(Chat.getInstance("chatty"), usersTime1);
		assertEquals(null+"", listenerMock.consumeLastAddeUser()+"");
	}
	
	@Test
	public void onMessageOnExistingChat_ShouldFireUserRemovedIfUserDisappears()
	{
		subject.addGlobalChatListener(listenerMock);
		User[] usersTime0 = new User[]{User.getInstance("foo")};
		subject.handleChatUsers(Chat.getInstance("chatty"), usersTime0);
		
		User[] usersTime1 = new User[]{
				User.getInstance("foo"), 
				User.getInstance("baz")};
		subject.handleChatUsers(Chat.getInstance("chatty"), usersTime1);
		
		User[] usersTime2 = new User[]{User.getInstance("baz")};
		subject.handleChatUsers(Chat.getInstance("chatty"), usersTime2);
		
		assertEquals("foo", listenerMock.consumeLastLeftUser());
	}
	
	final class ChatListenerMock implements GlobalChatListener {
		private String lastAddedUser;
		private String lastLeftUser;
		public List<Chat> chats = new ArrayList<Chat>();
		
		@Override
		public void newChatStarted(Chat chat, User [] users) {
			chats.add(chat);
		}

		@Override
		public void userAdded(Chat chat, User user) {
			lastAddedUser = user.getId();
		}

		@Override
		public void userLeft(Chat chat, User user) {
			lastLeftUser = user.getId();
		}
		
		public String consumeLastAddeUser() {
			String lastAddedUser2 = lastAddedUser;
			lastAddedUser = null;
			return lastAddedUser2;
		}
		
		public String consumeLastLeftUser() {
			String lastLeftUser2 = lastLeftUser;
			lastLeftUser = null;
			return lastLeftUser2;
		}
	}	
}