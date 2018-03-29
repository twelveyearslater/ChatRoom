<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page language="java" import="java.util.Map" %>

<% Map<String, Integer> roomMsgMap= (Map<String, Integer>)request.getAttribute("roomMsg"); 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>苟呆聊天室</title>

<script type="text/javascript">
	function validate(){
		var obj = document.getElementById("name");
		if(obj.value==""){
			alert("苟呆温馨提示：房间名不能为空,请输入房间名。");
			obj.focus();
			return false;
		}
		var re =/[`~!@#$%^&*_+<>{}\/'[\]]/im;
		if (re.test(obj.value)){
			alert("苟呆温馨提示：房间名中不允许有特殊符号哦");
			obj.value="";
			obj.focus();
			return false;
		}
		var url = "ajax?method=validRoomName&name="+obj.value;
		xmlhttp = null;
		if(window.XMLHttpRequest){
			xmlhttp = new XMLHttpRequest();
		}else if(window.ActiveXObject){
			xmlhttp = new AcitveXObject("Microsoft.XMLHTTP");
		}
		if(xmlhttp!=null){
			xmlhttp.onreadystatechange=state_change;
			xmlhttp.open("GET", encodeURI(encodeURI(url)) , false);
			xmlhttp.send();
		}
	}
	
	function state_change(){
	
		if(xmlhttp.readyState==4){
			if(xmlhttp.status==200){
				var obj = document.getElementById("name");
				if(xmlhttp.responseText=="1"){
					alert("房间名已存在,请输入其他房间名。");
					obj.focus();
					return false;
				}
				var url = "index.jsp?name="+obj.value;
				window.location.href=encodeURI(encodeURI(url));
			}else{
				alert("Problem retrieving data:" + xmlhttp.statusText);
			}
		}
	}
</script>

</head>
<body>

<div>
    <div>
		<h1><b>苟呆聊天室</b></h1> 
	</div>
	<div >
		<div><input type="text" id="name" name="name" value="" /><button onclick="javascript:validate();">新建聊天室</button></div>
	</div>
	<br/>
	<div>
		<div>加入聊天室</div>
		<%if(roomMsgMap.isEmpty()){%>
			<b>未发现创建的聊天室</b>
		<%
		}else{
		%>
			<table>
				<tr>
					<td>房间名称</td>
					<td>在线人数</td>
				</tr>
				
		<%
			for(String name:roomMsgMap.keySet()) {
		%>
				<tr>
					<td><a href="index.jsp?name=<%=name%>"><%=name%></a></td>
					<td><%=roomMsgMap.get(name)%></td>
				</tr>
		<%		
			}
		%>
			</table>
		<%
		}
		%>
	</div>
</div>

</body>
</html>