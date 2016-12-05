package com.lotte.main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
	
	public Calendar cal = Calendar.getInstance();
	public SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd-hh:mm:ss");
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
	String datetime;
	
	BufferedWriter out;
	//public HashMap<Integer, Vector<MainServerThreadDTO>> req_room = new HashMap<Integer, Vector<MainServerThreadDTO>>();
	
	
	
	public ServerMain() {
		
		bt_connection = new JButton("Open Server");
		
		try {
			out = new BufferedWriter(new FileWriter(sdf2.format(cal.getTime())+".txt"));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
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
		setSize(1000, 600);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}
	
	public void dbConnection(){
		DatabaseConnection dbConnection = new DatabaseConnection(this);
		con = dbConnection.getConnection();
	}
	
	public void connection(){
		String log;
		try {
			server = new ServerSocket(port);
			log = "[ Debug ] : Server is opend on "+sdf.format(cal.getTime())+"\n";
			log_area.append(log);
			addLog(log);
			dbConnection();
			
			while(true){
				Socket client = server.accept();
				String ip = client.getInetAddress().getHostAddress();
				log = "[ Debug ] : Client "+ip+" is connected on "+sdf.format(cal.getTime())+"\n";
				log_area.append(log);
				addLog(log);
				ServerThread ct = new ServerThread(client, con, ip, this);
				ct.start();
				
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
	
	public void addLog(String msg){
		try {
			out.append(msg);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("[Debug] : Error occured on FileOutput");
		}
	}
}
