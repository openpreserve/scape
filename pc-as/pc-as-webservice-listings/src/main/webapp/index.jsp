<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<html>
<head>
	<style type="text/css">
		body{
			margin: 0;
    		padding: 0;
		}
		#main{
			margin: 0px auto;
			width: 960px;
		}
		#header{
			width: 100%;
			position: relative;
			float:left;
		}
		#header h1{
			display: inline;
    		left: 310px;
    		position: absolute;
    		top: 40px;
		}
		#content{
			width: 100%;
			position: relative;
			float:left;
			padding-left: 20px;
		}
		#header img{
			border:none;
		}
		form{
			float:left;
		}
		label{
			float:left;
			width: 100px;
		}
		input{
			float:left;
		}
		input[type=text],select{
			width:400px;
		}
		.floatLeft{
			float:left;
		}
		.clearRight{
			clear:right;
		}
		form div{
			float: left;
			width:100%;
		}
		a, a:hover, a:visited, a:active{
			/*text-decoration: none;*/
			color:#000000;
		} 
	</style>
</head>
<body>
	<div id="main">
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
			List<String> services = new ArrayList<String>();
			String serviceName=null;
			for(File f : listFiles){
				serviceName = f.getName();
				serviceName = serviceName.replaceFirst("scapeservices#","");
				services.add(serviceName);
			}
			Collections.sort(services);
			String requestURL = request.getRequestURL().toString();
			if(requestURL.endsWith("/")){
				requestURL = requestURL.substring(0,requestURL.length()-1);
			}
			
		%>
		<div id="header">
			<img alt="scape_log" src="img/scape-logo.png">
			<h1>SCAPE PC-AS: web-service listings</h1>
		</div>
		<div id="content">
			<h3>Just chose a web-service and provide the URL for the input file...</h3>
			<form action="/executeConversion">
				<div>
					<label>Service URL</label>
					<select name="serviceURL">
						<%
							for(String name : services){
						%>
								<option value="<%=requestURL %>/scapeservices/<%=name%>"><%=name.replaceFirst("^scape-","").replaceFirst("[0-9]+-service$","") %></option>
						<% 	} %>
					</select>
				</div>
				<div>
					<label>File URL</label><input type="text" name="fileURL"><a href="/scape/testdata/" target="_blank" class="clearRight floatLeft">(test data folder)</a>
				</div>
				<input type="submit" value="Convert">
			</form>
		</div>
	</div>
</body>
</html>
