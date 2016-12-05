package com.lotte.main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.lotte.db.DatabaseConnection;

public class ServerMain extends JFrame implements ActionListener, Runnable{
	ServerSocket server;
	BufferedReader buffr;
	String request;
	static int port = 5555;
	
	Connection con = null;
	
	JButton bt_connection;
	public JTextArea log_area; 
	JPanel p_bt;
	JScrollPane scroll;
	
	Thread mainThread;
	
	//public HashMap<Integer, Vector<MainServerThreadDTO>> req_room = new HashMap<Integer, Vector<MainServerThreadDTO>>();
	
	
	
	public ServerMain() {
		
		bt_connection = new JButton("Open Server");
		
		log_area = new JTextArea();
		scroll = new JScrollPane(log_area);
		p_bt = new JPanel();
		
		p_bt.add(bt_connection);
		add(p_bt, BorderLayout.NORTH);
		add(scroll, BorderLayout.CENTER);
		
		bt_connection.addActionListener(this);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if( server != null ){
					try {
						server.close();
					} catch (IOException e1) {
					}
				}
				if( con != null ){
					try {
						con.close();
					} catch (SQLException e1) {
					}
				}
				System.exit(0);
			}
		});
		setTitle("MainServer");
		setSize(400, 600);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		
	
		
	}
	
	public void dbConnection(){
		DatabaseConnection dbConnection = new DatabaseConnection(this);
		con = dbConnection.getConnection();
	}
	
	public void connection(){
		try {
			server = new ServerSocket(port);
			log_area.append("[ Debug ] : Server is opend\n");
			dbConnection();
			while(true){
				Socket client = server.accept();
				String ip = client.getInetAddress().getHostAddress();
				log_area.append(("[ Debug ] : Client "+ip+" is connected\n"));
				//MainServerThread ct = new MainServerThread(client, con, ip, this);
				//ct.start();
				
			}
		} catch (IOException e) {
		}	
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == bt_connection){
			bt_connection.setEnabled(false);
			mainThread = new Thread(this);
			mainThread.start();
		}
	}

	public void run() {
		connection();
	}
	
	public static void main(String[] args) throws Exception {
		new ServerMain();
	}
}
