<%-- 
    Document   : index
    Created on : 18 Nov 2021, 08:56:17
    Author     : jon

    The Java web app is configured to only allow access to this page for
    administrators of the host web server. It provides a form for 
    configuring the tool.

--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="outcomes" class="uk.ac.leedsbeckett.ltitools.admin.AdminOutcomes" scope="request"/>
<% outcomes.setRequest( request ); %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Admin Page</title>
  </head>
  <body>
    <h1>Admin Page</h1>
    <p>Action = ${outcomes.action}</p>
    <p>${outcomes.importantMessage}</p>
    
    <form method="POST" action=".">
      <input type="hidden" name="action" value="saveconfig" />
      <h2>LTI 1.3 Configuration File</h2>
      <p><textarea name="config">${outcomes.rawConfiguration}</textarea></p>
      <h2>Logging Configuration File</h2>
      <p>Note that changes to logging configuration only come into effect when
        the java web application restarts.</p>
      <p><textarea name="logconfig">${outcomes.logConfiguration}</textarea></p>
      <p><input type="submit" value="Save Config"></p>
    </form>
          
  </body>
</html>
