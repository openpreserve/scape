<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.io.*"%>
<head>
	<style type="text/css">
		form{
			float:left;
			width:350px;
		}
		label{
			float:left;
			clear:right;
		}
		input{
			float:left;
			clear:right;
		}
		input[type=text]{
			width:300px;
		}
	</style>
</head>
<html>
<body>
	<h1>SCAPE PC-AS: web-service listings</h1>
	<%
		File tomcatWebappsFolder = new File("/var/lib/tomcat6/webapps/");
		File[] listFiles = tomcatWebappsFolder.listFiles(new FileFilter() {
			public boolean accept(File file) {
				boolean res = false;
				if (file.isDirectory() && file.getName().startsWith("scapeservices#")) {
					res = true;
				}
				return res;
			}
		});
		
	%>
	<h2>Test data folder:&nbsp;<a href="/scape/testdata/" target="_blank">Link</a></h2>
	<h2>Web-service listings</h2>
	<ul>
		<% 	String name=null;
			String requestURL = request.getRequestURL().toString();
			for(File f : listFiles){
				name = f.getName();
				name = name.replaceFirst("scapeservices#","");
		%>
				<li><a href="/scapeservices/<%=name%>" target="_blank"><%=name %></a></li>
		<% 	} %>
	</ul>
	<h2>Try it!</h2>
	<h3>Just provide the url from the chosen web-service and from the file to be converted...</h3>
	<form action="/executeConversion">
		<label>Service URL</label><input type="text" name="serviceURL">
		<label>File URL</label><input type="text" name="fileURL">
		<input type="submit" value="Convert">
	</form>
</body>
</html>
