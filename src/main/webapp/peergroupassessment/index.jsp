<%-- 
    Document   : index
    Created on : 18 Nov 2021, 08:56:17
    Author     : jon

    The 'home' page for the peergroupassessment tool.
    Same starting point for instructors and students.

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
    <h1>Peer Group Assessment Tool</h1>
    <p class="important">${support.importantMessage}</p>
    <p>This tool has not been implemented yet.</p>
    <h2>${support.pgaResource.title}</h2>
    <p>${support.pgaResource.description}</p>
    
    <c:choose>
      <c:when test="${support.allowedToManage || support.allowedToParticipate}">
        
        <c:if test="${support.allowedToManage}">
          <p>You can manage this resource.</p>
          <table>
            <tr><th>ID</th><th>Title</th><th>Members</th></tr>
            <c:forEach var="g" items="${support.pgaResource.groups}">
              <tr><td>${g.id}</td><td>${g.title}</td>
                <td><c:forEach var="m" items="${g.members}">${m.name}<br></c:forEach></td>
              </tr>
            </c:forEach>
            <tr><td colspan="2">Button to add a group will go here.</td></tr>
          </table>

          <h4>Unplaced Students</h4>
          <p><c:forEach var="m" items="${support.pgaResource.groupOfUnattached.members}">${m.name}<br></c:forEach></p>
          
        </c:if>
        
        <c:if test="${support.allowedToParticipate}">
          <p>You can use this resource by joining a group and participating in peer group assessment.</p>
        </c:if>
        
      </c:when>
      <c:otherwise>
        <p>Your role set by system that launched this tool means that you
        can neither participate or manage this resource.</p>
      </c:otherwise>
    </c:choose>
    
    <c:if test="${support.debugging}">
      <hr>
      <div><tt><pre>${support.dump}</pre></tt></div>
    </c:if>
        
  </body>
</html>
