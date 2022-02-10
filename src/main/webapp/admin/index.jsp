<%-- 
    Document   : index
    Created on : 18 Nov 2021, 08:56:17
    Author     : jon
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="outcomes" class="uk.ac.leedsbeckett.ltidemo.AdminOutcomes" scope="request"/>
<% outcomes.setRequest( request ); %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Admin Page</title>
  </head>
  <body>
    <h1>Admin Page</h1>
    <p>Action = ${outcomes.action}</p>
    <h2>Configuration File</h2>
    <p>${outcomes.importantMessage}</p>
    <form method="POST" action=".">
      <input type="hidden" name="action" value="saveconfig" />
      <p><textarea name="config">${outcomes.rawConfiguration}</textarea></p>
      <p><input type="submit" value="Save Config"></input></p>
    </form>
  </body>
</html>
