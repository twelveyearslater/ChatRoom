package com.eleven.websocket;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AjaxServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String method = req.getParameter("method");
		if("validRoomName".equals(method)){
			validRoomName(req,resp);
		}
		
	}
	
	public void validRoomName(HttpServletRequest req, HttpServletResponse resp){
		try{
			resp.setContentType("text/html;charset=utf-8");
			PrintWriter pw = resp.getWriter();
			String name = req.getParameter("name");
			System.out.println(name);
			Map<String, Integer> onlineCountMap = WebSocket.getInstance().getOnlineCountMap();
			Set<String> roomSet = onlineCountMap.keySet();
			if(roomSet.size()>99){
				pw.print("2");   // 2:代表可用房间已满
				return;
			}
			for(String str:onlineCountMap.keySet()){
				if(name.equals(str)){
					pw.print("1");   // 1:代表有同名的房间
					return;
				}
			}
			pw.println("0");  // 0：代表可以新建房间
			pw.flush();
			pw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
