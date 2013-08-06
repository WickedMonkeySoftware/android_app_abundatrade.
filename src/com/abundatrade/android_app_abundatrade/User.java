package com.abundatrade.android_app_abundatrade;

public class User {
	String synchkey;
	String login;
	String pw;
	String device;
	String mobilescan;
	
	public User (String synch, String log, String pass)
	{
		synchkey = synch;
		login = log;
		pw = pass;
		device = "android";
		mobilescan = "t";
	}

}
