package com.lotte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.lotte.domain.MemberDTO;
import com.lotte.main.ServerMain;

public class MemberDAO {

	Connection con;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	ServerMain serverMain;

	int room_number;

	public MemberDAO(Connection con, ServerMain serverMain) {
		this.con = con;
		this.serverMain = serverMain;
	}

	public boolean haveLogedIn(String id) {
		if (!id.equals("admin")) {
			String sql = "select isconnecting from member where id=?";
			try {
				pstmt = con.prepareStatement(sql);
				pstmt.setString(1, id);
				rs = pstmt.executeQuery();
				String log = "[ " + serverMain.sdf.format(serverMain.cal.getTime())
						+ " ] : Have Loged in method requested by \"" + id + "\"\n";
				serverMain.log_area.append(log);
				serverMain.addLog(log);

				if (rs.next()) {
					int isConnect = rs.getInt("isconnecting");
					if (isConnect == 1)
						return false;
					else
						return true;
				} else {
					return true;
				}
			} catch (SQLException e) {
				serverMain.log_area.append("[ Debug ] : Error occured on Have Loged in method.\n");
				return false;

			} finally {
				release(pstmt, rs);
			}
		} else {
			return true;
		}
	}

	public MemberDTO loginCheck(String id, String pwd) {

		String sql = "select * from member where id='" + id + "' and pwd='" + pwd + "'";
		MemberDTO dto = new MemberDTO();

		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			serverMain.log_area.append("[ Debug ] : loginCheck method requested.\n");

			if (rs.next()) {
				dto.setId(rs.getString("id"));
				dto.setName(rs.getString("name"));
				return dto;
			} else {
				dto = null;
				return dto;
			}

		} catch (SQLException e) {
			serverMain.log_area.append("[ Debug ] : Error occured loginCheck method.\n");
			return null;

		} finally {
			release(pstmt, rs);
		}
	}

	public boolean updateUserIp(String ip, String id) {
		String sql = "update member set ip=?, isconnecting=1 where id=?";

		try {
			serverMain.log_area.append("[ Debug ] : updateUserIp method requested.\n");
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, ip);
			pstmt.setString(2, id);
			int result = pstmt.executeUpdate();

			if (result == 1) {

				return true;
			}
		} catch (SQLException e) {
			serverMain.log_area.append("[ Debug ] : updateUserIp method occured error.\n");
		} finally {
			release(pstmt);
		}
		return false;
	}

	public void disConnection(String ip, String id) {
		String sql = "update member set isconnecting=0, ip='' where id=?";
		try {
			String log = "[ " + serverMain.sdf.format(serverMain.cal.getTime()) + " ] " + id
					+ " is try to log off on address : " + ip + "\n";
			serverMain.log_area.append(log);
			serverMain.addLog(log);
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			serverMain.log_area.append("[ Debug ] : Disconnection method occured error.\n");
		} finally {
			release(pstmt);
		}
	}

	
	public ArrayList<MemberDTO> showConnector(String ip, String id){
		String sql="select * from member where isconnecting=1";
		ArrayList<MemberDTO> list=new ArrayList<MemberDTO>();
		try {
			String log = "[ " + serverMain.sdf.format(serverMain.cal.getTime()) + " ] " + id
					+ " requests ShowConnector method on address : " + ip + "\n";
			serverMain.log_area.append(log);
			serverMain.addLog(log);
			serverMain.log_area.append("[ Debug ] : ShowConnector method requested.\n");
			pstmt=con.prepareStatement(sql);
			rs=pstmt.executeQuery();
			while(rs.next()){
				MemberDTO dto=new MemberDTO();
				dto.setId(rs.getString("id"));
				dto.setName(rs.getString("name"));
				list.add(dto);				
			}
		} catch (SQLException e) {
			serverMain.log_area.append("[ Debug ] : ShowConnector method occured error.\n");
		}finally{
			release(pstmt, rs);
		}
		return list;
	}
	
	
	public void release(PreparedStatement pstmt) {
		try {

			if (pstmt != null) {
				pstmt.close();
			}
		} catch (SQLException e) {
		}
	}

	public void release(PreparedStatement pstmt, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		} catch (SQLException e) {
		}
	}

}
