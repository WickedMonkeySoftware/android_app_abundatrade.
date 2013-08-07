package com.abundatrade.android_app_abundatrade;

public class User {
	String synckey;
	String login;
	String pw;
	boolean loggedIn;
	
	
	public User (String log, String pass)
	{
		synckey = null;
		login = log;
		pw = pass;
		loggedIn = false;
	}
	
	public void setSync (String newSync) {
		synckey = newSync;
	}
	
	public String getSync () {
		return synckey;
	}
	
	public void setLogin (String newLogin) {
		login = newLogin;
	}
	
	public String getLogin () {
		return login;
	}
	
	public void setPass (String newPass) {
		pw = newPass;
	}
	
	public String getPass () {
		return pw;
	}
	
	public void connSet () {
		loggedIn = !loggedIn;
	}
	
	public boolean connStatus () {
		return loggedIn;
	}

}
