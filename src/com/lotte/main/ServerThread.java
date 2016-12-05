package com.lotte.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.lotte.dao.MemberDAO;
import com.lotte.domain.MemberDTO;

public class ServerThread extends Thread {
	Socket socket;
	ServerMain serverMain;
	BufferedWriter buffw;
	BufferedReader buffr;
	Connection con;
	boolean flag = true;
	StringBuffer sb = new StringBuffer();

	String sql;
	String ip;
	String id;

	MemberDAO dao;
	String user_ip;
	int room_no;
	boolean correct_error;

	public ServerThread(Socket socket, Connection con, String ip, ServerMain serverMain) {
		this.socket = socket;
		this.serverMain = serverMain;
		this.con = con;
		this.ip = ip;
		dao = new MemberDAO(con, serverMain);
		try {
			buffr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			buffw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			user_ip = socket.getInetAddress().getHostAddress();
		} catch (IOException e) {
			this.serverMain.log_area.append("[ Debug ] : Thread IOException\n");
		}
	}

	public void listen() {
		try {
			String msg = buffr.readLine();
			serverMain.request = msg;
			analyzeProtocol(msg);

		} catch (IOException e) {
			if (socket.isClosed()) {
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e1) {
						serverMain.log_area.append("[ Debug ] : MainServer Listen Exception\n");
					}
				}
			}
		}
	}

	public void speak(String msg) {
		try {
			serverMain.log_area.append(msg + "\n");		//추후 삭제
			buffw.write(msg + "\n");
			buffw.flush();
		} catch (IOException e) {
			serverMain.log_area.append("[ Debug ] : MainServer SendMessage Exception\n");
		}
	}

	public void run() {
		while (true) {
			listen();
		}
	}

	public void analyzeProtocol(String msg) {
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);
			serverMain.log_area.append(msg + "\n");		//추후 삭제
			if (jsonObject.get("request").equals("login")) {
				JSONObject obj = (JSONObject) jsonObject.get("data");
				id = (String) obj.get("id");
				
				if (haveLogedIn(id)) {
					String pwd = (String) obj.get("password");
					MemberDTO dto = new MemberDTO();
					dto = getLoginCheck(id, pwd);
					
					if (dto == null) {
						sb.delete(0, sb.length());
						sb.append("{\"response\" : \"login\",");
						sb.append("\"data\" : {");
						sb.append("\"success\" : \"false\"");
						sb.append("}}");
						speak(sb.toString());
					} else {
						updateUserIp(ip, id);
						String log = "[ "+serverMain.sdf.format(serverMain.cal.getTime())+" ] "+id+" is logged in\n";
						serverMain.log_area.append(log);
						serverMain.addLog(log);
						sb.delete(0, sb.length());
						sb.append("{\"response\" : \"login\",");
						sb.append("\"data\" : {");
						sb.append("\"id\" : \"" + dto.getId() + "\",");
						sb.append("\"name\" : \"" + dto.getName() + "\",");
						sb.append("\"success\" : \"true\"");
						sb.append("}}");
						speak(sb.toString());
					}
				}

				else {
					String log = "[ "+serverMain.sdf.format(serverMain.cal.getTime())+" ] "+id+" failed to log in on address : "+ip+"\n";
					serverMain.log_area.append(log);
					serverMain.addLog(log);
					sb.delete(0, sb.length());
					sb.append("{\"response\" : \"logedin\",");
					sb.append("\"data\" : {");
					sb.append("\"success\" : \"false\"");
					sb.append("}}");
					speak(sb.toString());
				}
			}
			
			else if(jsonObject.get("request").equals("disconnect")){
				JSONObject obj=(JSONObject)jsonObject.get("data");
				disConnection((String)obj.get("id"));
				String log = "[ "+serverMain.sdf.format(serverMain.cal.getTime())+" ] "+id+" loged offed on address : "+ip+"\n";
				serverMain.log_area.append(log);
				serverMain.addLog(log);
				sb.delete(0, sb.length());
				sb.append("{\"response\" : \"disconnect\",");
				sb.append("\"data\" : {");
				sb.append("\"success\" : \"true\"");
				sb.append("}}");
				speak(sb.toString());
			}
			
else if(jsonObject.get("request").equals("showconnector")){
				
				ArrayList<MemberDTO> list=showConnector(ip);
				if(list!=null){
					sb.delete(0, sb.length());
					sb.append("{\"response\" : \"showconnector\",");
					sb.append("\"success\" : \"true\",");
					sb.append("\"data\" : [");
					for(int i=0;i<list.size();i++){
						MemberDTO dto=list.get(i);
						sb.append("{");
						sb.append("\"name\" : \"" + list.get(i).getName() + "\",");
						sb.append("\"ID\" : \"" + list.get(i).getId() + "\"");
						if (i < (list.size() - 1)) {
							sb.append("},");
						} else {
							sb.append("}");
						}
					}
					sb.append("]}");
					speak(sb.toString());
				}
				
				else {
					sb.delete(0, sb.length());
					sb.append("{\"response\" : \"showconnector\",");
					sb.append("\"success\" : \"false\"");
					sb.append("}");
					speak(sb.toString());
				}
			}

		} catch (ParseException e) {
			serverMain.log_area.append("[ Debug ] : JSON Parsing Error.\n");
		}
	}

	public boolean haveLogedIn(String id) {
		String log = "[ "+serverMain.sdf.format(serverMain.cal.getTime())+" ] "+id+" is try to log in on address : "+ip+"\n";
		serverMain.log_area.append(log);
		serverMain.addLog(log);
		return dao.haveLogedIn(id);
	}

	public MemberDTO getLoginCheck(String id, String pwd) {
		return dao.loginCheck(id, pwd);
	}

	public void updateUserIp(String ip, String id) {
		boolean result = dao.updateUserIp(ip, id);
		serverMain.log_area.append("[ Debug ] : " + result + "\n");
		if (result) {
			serverMain.log_area.append("[ Debug ] : User Ip is updated.\n");
		} else {
			serverMain.log_area.append("[ Debug ] : User Ip updating is failed.\n");
		}
	}
	
	public void disConnection(String id){
		dao.disConnection(ip, id);
	}
	
	public ArrayList<MemberDTO> showConnector(String ip){
		return dao.showConnector(ip, id);
		
	}
	
}
