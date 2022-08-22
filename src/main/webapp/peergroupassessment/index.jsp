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

<jsp:useBean id="support" class="uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupPageSupport" scope="request"/>
<% support.setRequest( request ); %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Peer Group Assessment</title>
    <style>
      body { font-family: sans-serif; }
      .important { background-color: yellow };
    </style>
  </head>
  <body>
    <h1>Peer Group Assessment on ${request.getServerName()}</h1>
    <p class="important">${support.importantMessage}</p>
    <p>This tool has not been implemented yet.</p>
    <h2>${support.pgaResource.title}</h2>
    <p>${support.pgaResource.description}</p>
  </body>
</html>
