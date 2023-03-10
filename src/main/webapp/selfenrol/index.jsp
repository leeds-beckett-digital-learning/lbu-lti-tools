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

<jsp:useBean id="support" class="uk.ac.leedsbeckett.ltitools.selfenrol.SePageSupport" scope="request"/>
<% support.setRequest( request ); %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Self Enrol</title>
    <style>
      body { font-family: sans-serif; padding: 1em 1em 1em 1em; }
      .stage {}
      .stage-label { font-weight: bold }
      .important { background-color: yellow }
      .dialog { 
                   display: none; 
                   position: fixed;
                   background-color: rgb(0,0,0);
                   background-color: rgba(0,0,0,0.4);                   
                   left: 0;
                   top: 0;
                   width: 100%;
                   height: 100%;
                   overflow: auto;
                   z-index: 1000; }
      .dialogcontent {
                   margin: 2em 2em 1em 4em; 
                   padding: 1em 1em 1em 1em;
                   box-shadow: 0.5em 0.5em 0.5em 0.25em black;
                   min-height: 10em;
                   background: white; }
      td {
                   padding: 0.5em 2em 0.5em 2em;
      }
      
      .emptyinput {
        background-color: white;
      }

      .validinput {
        background-color: greenyellow;
      }

      .invalidinput {
        background-color: hotpink;
      }


.alertList {
  padding: 10px;
  border: 2px solid hsl(206deg 74% 54%);
  border-radius: 4px;
  background: hsl(206deg 74% 90%);
}

.alertList:empty {
  display: none;
  padding: 10px;
  border: 2px white;
  border-radius: 4px;
  background: white;
}

      
    </style>
    <link rel="stylesheet" href="../style/dialog.css"/>
    <script lang="JavaScript">
      
const gendata = 
{
  myid:         '${support.personId}',
  myname:       '${support.personName}',
  manager:      ${support.allowedToManage},
  participant:  ${support.allowedToParticipate},
  wsuri:        '${support.websocketUri}',
  debugging:    ${support.debugging}
};
        
    </script>
    <script type="module" src="../javascript/@BUILDTIMESTAMP@/selfenrol/index.js"></script>
  </head>
  <body>
    <div id="dialogdiv" class="dialogs">
      
      <div role="dialog" id="debugdialog" aria-labelledby="debugdialogLabel" aria-modal="true" class="hidden">
        <h3 id="debugdialogLabel">Debug Information</h3>
        <div class="dialog_form_actions"><button id="debugdialogCloseButtonTop" value="Close">Close</button></div>
        <pre id="debugtext"></pre>
        <div class="dialog_form_actions"><button id="debugdialogCloseButtonBottom" value="Close">Close</button></div>
      </div>
      
    </div>


    <div id="basePage">
      
    <h1>Self Enrol on Modules and Course Communities</h1>
    <p class="important">${support.importantMessage}</p>

    <ul id="toplevelalert" class="alertList"></ul>
    
    <p>This tool is non-functional as it is under development.</p>
    <c:choose>
      <c:when test="${support.allowedToManage || support.allowedToParticipate}">
        
        <c:if test="${support.allowedToManage}">
          <p>You have management rights where this tool is deployed.</p>          
        </c:if>
                  
      </c:when>
      <c:otherwise>
        <p>Your role set by system that launched this tool means that you
        can neither participate or manage this resource.</p>
      </c:otherwise>
    </c:choose>
    
    <p><button id="searchButton">Search</button></p>
    
    <c:if test="${support.debugging}">
      <div style="margin-top: 10em;">
        <hr>
        <p><button id="debugdialogButton">Debug Dialog</button></p>          
        <div><tt><pre>${support.dump}</pre></tt></div>
      </div>
    </c:if>
        
    </div>
  </body>
</html>
