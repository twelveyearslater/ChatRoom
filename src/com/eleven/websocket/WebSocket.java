package com.eleven.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @date 20180111
 * @author eleven
 * @serverEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端
 * 注解的值将被用于监听用户连接的中端访问url地址，客户端可以通过这个url来连接到WebSocket服务器端
 */
@ServerEndpoint("/websocket/{name}")
public class WebSocket {
	
	//线程安全的单例
	private static WebSocket single = null;
	
	public static synchronized WebSocket getInstance(){  
		if(null == single ) single = new WebSocket();
			return single;
	} 
	
	//静态变量，用来记录当前在线的链接数，应该把它设计成线程安全的。
	private static int onlineCount = 0;
	
	public static int getOnlineCount() {
		return onlineCount;
	}

	//在线人数集合
	private static Map<String,Integer> onlineCountMap = new HashMap<String,Integer>();
	
	//房间集合
	private static Map<String,CopyOnWriteArraySet<WebSocket>> roomMap = new HashMap<String,CopyOnWriteArraySet<WebSocket>>();
	
	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;
	
	/**
	 * @date 20180111
	 * @param session  可选的参数。session为与某个客户端的链接会话，需要通过它来给客户端发送数据
	 * 链接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(@PathParam(value = "name")String roomName,Session session) {
		this.session = session;
		//判断是否存在该房间：存在则继续，不存在则创建一个
		roomMaker(roomName);
		//取得该房间的session集合
		CopyOnWriteArraySet<WebSocket> webSocketSet = roomMap.get(roomName);
		//加入set中
		webSocketSet.add(this);
		//在线数加1
		addOnlineCount(roomName);
		System.out.println("有新连接加入！当前在线人数为"+getOnlineCount(roomName));
	}
	
	/**
	 * @date 20180111
	 * @return
	 * 链接关闭调用的方法
	 */
	@OnClose
	public void onClose(@PathParam(value = "name")String roomName) {
		//取得该房间的session集合
		CopyOnWriteArraySet<WebSocket> webSocketSet = roomMap.get(roomName);
		//从集合中删除这个断开连接的对象
		webSocketSet.remove(this);
		subOnlineCount(roomName);
		if(onlineCountMap.get(roomName)<=0){
			roomMap.remove(roomName);
			onlineCountMap.remove(roomName);
		}
	}
	
	/**
	 * @date 20180111
	 * @param message 客户端发来的消息
	 * @param session 可选的参数
	 * 收到客户端消息后调用的方法
	 */
	@javax.websocket.OnMessage
	public void OnMessage(@PathParam(value = "name")String roomName,String jsonData, Session session) {
		//取得该房间的session集合
		CopyOnWriteArraySet<WebSocket> webSocketSet = roomMap.get(roomName);
		//解析Json
		JSONObject obj;
		try {
			obj = new JSONObject(jsonData);
			System.out.println("在房间"+roomName +"中用户名为:"+obj.get("name")+"说："+ obj.get("msg"));
		} catch (JSONException e1) {
			System.out.println("Json解析失败");
			e1.printStackTrace();
		}
		//群发消息
		for(WebSocket item:webSocketSet) {
			try {
				item.sendMessage(jsonData);
			}catch(IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	/**
	 * @date 20180111
	 * @param session 
	 * @param error
	 * 发生错误时调用
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("WebSocket通信发生错误");
		error.printStackTrace();
	}
	
	/**
	 * @date 20180111
	 * @param message
	 * @throws IOException
	 * 这个方法与上面几个方法不一样，没有注解，是根据自己需要添加的方法。
	 */
	public void sendMessage(String message) throws IOException{
		this.session.getBasicRemote().sendText(message);
	}
	
	public static synchronized int getOnlineCount(String roomName) {
		return onlineCountMap.get(roomName);
	}
	
	public static synchronized void addOnlineCount(String roomName) {
		WebSocket.onlineCount++;
		onlineCountMap.put(roomName,(onlineCountMap.get(roomName)+1));
	}
	
	public static synchronized void subOnlineCount(String roomName) {
		WebSocket.onlineCount--;
		onlineCountMap.put(roomName,(onlineCountMap.get(roomName)-1));
	}
	
	private static void roomMaker(String roomName) {
		for(String name : roomMap.keySet()) {
			if(roomName.equals(name)) {
				return;
			}
		}
		//concurrent包的线程安全Set，用来存放每个客户端对应的WebSocket对象。若要实现服务端与客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
		CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<WebSocket>();
		roomMap.put(roomName, webSocketSet);
		onlineCountMap.put(roomName, 0);
	}
	
	public Map<String,Integer> getOnlineCountMap(){
		return onlineCountMap;
	}

}
