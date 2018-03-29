<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page language="java" import="java.net.URLDecoder" %>
    <% 
    	request.setCharacterEncoding("utf-8");
    	String name = request.getParameter("name"); 
    	name = URLDecoder.decode(name, "UTF-8");
    %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>苟呆聊天室</title>
</head>
<body>
	<div>
		<h1><b id="name"><%=name%></b>_房间</h1> 
	</div>
	<div>
		昵称：<input type="text" id="user_name" name="user_name" value=""/>&nbsp;&nbsp;<button id="button1" onclick="javascript:setName()">确定后将不允许修改</button>
	</div>
	<div id="msgBLK">
		您好，首次进入聊天室请先输入你在此房间的昵称。
	</div>
	<br/><input type="text" id="text" />
	<button onclick="send()">发送消息</button>
	<hr/>
	<button onclick="closeWebSocket()">关闭聊天</button>
	<hr/>
	<div id="message"></div>
</body>

<script type="text/javascript">
	var name = document.getElementById("name").innerHTML;
	var websocket = null;
	//判断当前浏览器是否支持WebSocket
	if('WebSocket' in window){
		//根据实际的情况配置ip及端口
		websocket = new WebSocket("ws://localhost:8080/JavaWebSocket/websocket/"+name);
	}else{
		alert("当前服务器 Not Support WebSocket")
	};
	
	//链接发生错误的回调方法
	websocket.onerror = function(){
		setMessageInnerHTML("聊天室发生错误，请重新进入");
	};
	
	//连接成功建立的回调方法
	websocket.onopen = function(){
		setMessageInnerHTML("您已进入该房间");
	};
	
	//接收到消息的回调方法
	websocket.onmessage = function(event){
		setMessageForChat(event.data);
	};
	
	//链接关闭的回调方法
	websocket.onclose = function(){
		setMessageInnerHTML("您已关闭聊天");
	};
	
	//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket链接，防止链接还没断开就关闭窗口，server端会抛异常
	window.onbeforeunload = function(){
		closeWebSocket();
	};
	
	//将系统消息显示在网页上
	function setMessageInnerHTML(innerHTML){
		document.getElementById('message').innerHTML += innerHTML + '<br/>';
	};
	//将消息显示在聊天框
	function setMessageForChat(msg){
		var jsonObj =JSON.parse(msg);
		document.getElementById('message').innerHTML += jsonObj.name + ': <br/>';
		document.getElementById('message').innerHTML +=  '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' + jsonObj.msg + '<br/>';
	}
	
	
	//关闭WebSocket连接
	function closeWebSocket(){
		websocket.close();
	};
	
	//发送信息
	function send(){
		var username = document.getElementById('user_name').value;
		var message = document.getElementById('text');
		if(username==null||username==""){
			alert("苟呆温馨提示：第一次发言请先填写昵称");
			document.getElementById('user_name').focus()
			return;
		}
		if(message.value.length==0){
			alert("苟呆温馨提示：您难道不要说点什么吗？");
			document.getElementById('user_name').focus()
			return;
		}
		document.getElementById('user_name').readonly="true";
		document.getElementById('button1').disabled="true";
		var data = '{"name":"'+username+'","msg":"'+message.value+'"}';
		websocket.send(data);
		message.value="";
	};
	
	function setName(){
		var username = document.getElementById('user_name').value;
		if(username==null||username==""){
			alert("苟呆温馨提示：请重新填写长度不为空的昵称");
			return;
		}
		if(username.length>16){
			alert("苟呆温馨提示：昵称长度不要超过在八个中文字符哦");
			return;
		}
		var input = document.getElementById('user_name');
		input.setAttribute("readOnly",true);
		input.style.backgroundColor="#ffffff";
		document.getElementById('button1').disabled=true;
		document.getElementById('msgBLK').style.display="none";
	}
</script>
</html>