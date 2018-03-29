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
 * @serverEndpoint ע����һ�����ε�ע�⣬���Ĺ�����Ҫ�ǽ�Ŀǰ���ඨ���һ��websocket��������
 * ע���ֵ�������ڼ����û����ӵ��ж˷���url��ַ���ͻ��˿���ͨ�����url�����ӵ�WebSocket��������
 */
@ServerEndpoint("/websocket/{name}")
public class WebSocket {
	
	//�̰߳�ȫ�ĵ���
	private static WebSocket single = null;
	
	public static synchronized WebSocket getInstance(){  
		if(null == single ) single = new WebSocket();
			return single;
	} 
	
	//��̬������������¼��ǰ���ߵ���������Ӧ�ð�����Ƴ��̰߳�ȫ�ġ�
	private static int onlineCount = 0;
	
	public static int getOnlineCount() {
		return onlineCount;
	}

	//������������
	private static Map<String,Integer> onlineCountMap = new HashMap<String,Integer>();
	
	//���伯��
	private static Map<String,CopyOnWriteArraySet<WebSocket>> roomMap = new HashMap<String,CopyOnWriteArraySet<WebSocket>>();
	
	//��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
	private Session session;
	
	/**
	 * @date 20180111
	 * @param session  ��ѡ�Ĳ�����sessionΪ��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
	 * ���ӽ����ɹ����õķ���
	 */
	@OnOpen
	public void onOpen(@PathParam(value = "name")String roomName,Session session) {
		this.session = session;
		//�ж��Ƿ���ڸ÷��䣺������������������򴴽�һ��
		roomMaker(roomName);
		//ȡ�ø÷����session����
		CopyOnWriteArraySet<WebSocket> webSocketSet = roomMap.get(roomName);
		//����set��
		webSocketSet.add(this);
		//��������1
		addOnlineCount(roomName);
		System.out.println("�������Ӽ��룡��ǰ��������Ϊ"+getOnlineCount(roomName));
	}
	
	/**
	 * @date 20180111
	 * @return
	 * ���ӹرյ��õķ���
	 */
	@OnClose
	public void onClose(@PathParam(value = "name")String roomName) {
		//ȡ�ø÷����session����
		CopyOnWriteArraySet<WebSocket> webSocketSet = roomMap.get(roomName);
		//�Ӽ�����ɾ������Ͽ����ӵĶ���
		webSocketSet.remove(this);
		subOnlineCount(roomName);
		if(onlineCountMap.get(roomName)<=0){
			roomMap.remove(roomName);
			onlineCountMap.remove(roomName);
		}
	}
	
	/**
	 * @date 20180111
	 * @param message �ͻ��˷�������Ϣ
	 * @param session ��ѡ�Ĳ���
	 * �յ��ͻ�����Ϣ����õķ���
	 */
	@javax.websocket.OnMessage
	public void OnMessage(@PathParam(value = "name")String roomName,String jsonData, Session session) {
		//ȡ�ø÷����session����
		CopyOnWriteArraySet<WebSocket> webSocketSet = roomMap.get(roomName);
		//����Json
		JSONObject obj;
		try {
			obj = new JSONObject(jsonData);
			System.out.println("�ڷ���"+roomName +"���û���Ϊ:"+obj.get("name")+"˵��"+ obj.get("msg"));
		} catch (JSONException e1) {
			System.out.println("Json����ʧ��");
			e1.printStackTrace();
		}
		//Ⱥ����Ϣ
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
	 * ��������ʱ����
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("WebSocketͨ�ŷ�������");
		error.printStackTrace();
	}
	
	/**
	 * @date 20180111
	 * @param message
	 * @throws IOException
	 * ������������漸��������һ����û��ע�⣬�Ǹ����Լ���Ҫ��ӵķ�����
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
		//concurrent�����̰߳�ȫSet���������ÿ���ͻ��˶�Ӧ��WebSocket������Ҫʵ�ַ������ͻ���ͨ�ŵĻ�������ʹ��Map����ţ�����Key����Ϊ�û���ʶ
		CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<WebSocket>();
		roomMap.put(roomName, webSocketSet);
		onlineCountMap.put(roomName, 0);
	}
	
	public Map<String,Integer> getOnlineCountMap(){
		return onlineCountMap;
	}

}
