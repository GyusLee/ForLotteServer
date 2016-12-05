package com.lotte.db;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.lotte.main.ServerMain;

public class DatabaseConnection {
	public Connection con;
	
	String url = "jdbc:oracle:thin:@localhost:1521:XE";
	String user = "lotte";
	String password = "lotte";
	ServerMain serverMain;
	
	public DatabaseConnection(ServerMain serverMain) {
		this.serverMain = serverMain;
	}
	
	public Connection getConnection(){
		try {
			con = DriverManager.getConnection(url, user, password);
			serverMain.log_area.append("[ Debug ] : Database connection is successed\n");
			
		} catch (SQLException e) {
			e.printStackTrace();
			serverMain.log_area.append("[ Debug ] : Database connection is failed");
		} 
			return con;
	}
}
