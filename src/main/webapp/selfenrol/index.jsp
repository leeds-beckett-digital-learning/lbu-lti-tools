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
    <link rel="stylesheet" href="../style/fonts.css">    
    <link rel="stylesheet" href="../style/dialog.css"/>
    <link rel="stylesheet" href="../style/buttons.css"/>
    <style>
      body { font-family: sans-serif; padding: 1em 1em 1em 1em; max-width: 30em; }
      h2 { padding-top: 3em; }
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
    </style>
    <script lang="JavaScript">
      
const dynamicPageData = ${support.dynamicPageDataAsJSON};

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

      <div role="dialog" id="searchdialog" aria-labelledby="searchdialogLabel" aria-modal="true" class="hidden">
        <h3 id="searchdialogLabel">Search Results</h3>
        <div>
          <select size="10" id="searchresults"></select>  
        </div>
        <div class="dialog_form_actions">
          <button id="searchdialogEnrolButton" value="Close">Enrol</button>
          <button id="searchdialogCloseButton" value="Close">Close</button>
        </div>
      </div>
      
    </div>


    <div id="basePage">
      
    <h1>Self Enrol on Modules and Course Communities</h1>
        
    <c:choose>
      <c:when test="${support.allowedToEnrol}">
        
        <h2>Enrol on a module</h2>
        <p>Enter a course reference number (CRN). This will usually be a
        CRN that was created by the student information system for a real
        module that carries credit. Non-credit MyBeckett modules are created
        on request and use a made-up CRN that always starts with a zero.</p>
        <p><input id="courseid"/><button id="searchCourseButton">Search</button></p>

        <h2>Enrol on a course community</h2>
        <p>Enter an award code. These are comprised of capitals letters
        and may include some numerals.</p>
        <p><input id="orgid"/><button id="searchOrgButton">Search</button></p>

        <h2>Enrol on a training module</h2>
        <p>Use the button to view a full list of all available modules.</p>
        <p><button id="searchTrainingButton">Search</button></p>
          
      </c:when>
      <c:otherwise>
        <p>Your role set by system that launched this tool means that you
        cannot use it.</p>
      </c:otherwise>
    </c:choose>
    
    
    <c:if test="${support.debugging}">
      <div style="margin-top: 10em;">
        <hr>
        <p><button id="debugdialogButton">Debug Dialog</button></p>          
        <div><tt><pre>${support.dump}</pre></tt></div>
      </div>
    </c:if>
        
    </div>

    <ul id="toplevelalert" class="alertList"></ul>
    
  </body>
</html>
