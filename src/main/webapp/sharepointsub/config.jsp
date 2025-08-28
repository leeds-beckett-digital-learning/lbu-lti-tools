<%-- 
    Document   : index
    Created on : 18 Nov 2021, 08:56:17
    Author     : jon

    The config page for the huge upload tool.
    For use in platform sysadmin pages to set global configuration parameters.

--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="support" class="uk.ac.leedsbeckett.ltitools.sharepointsub.SpSubPageSupport" scope="request"/>
<% support.setRequest( request ); %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Sharepoint Submission</title>
    <link rel="stylesheet" href="../style/fonts.css">    
    <link rel="stylesheet" href="../style/dialog.css"/>
    <link rel="stylesheet" href="../style/buttons.css"/>
    <style>
      body { font-family: 'Nobile', sans-serif; font-size: 12px; padding: 0.2em 1em 0.2em 1em; background-color: white; }
      h2 { margin: 0em 0em 0.75em 0em; }
      h3 { margin: 0.5em 0em 0.25em 0em; }
      p { margin: 0em 0em 0em 0em; }
      .section { margin: 0em 0em 4em 0em; }
      .block { max-width: 50em; }
      .stage {
        padding-bottom: 1em;
      }
      .stage-active {
        display: block;
        margin: 0px;
        padding: 0px;
      }
      .stage-inactive {
        display: none;
        margin: 0px;
        padding: 0px;
      }
      .stage-text {
        font-size: 14px; 
        border: black 3px solid;
        border-radius: 0.5em;
        margin: 0px;
        padding: 0.25em;
      }
      .stage-img {
        vertical-align: bottom;
      }
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

      th { 
        vertical-align: top;
        padding: 0.25em 0.5em 0.25em 0.5em;
      }
      
      th.thcollapsed {
        padding: 0em;
        margin: 0em;
        border: none;
      }
      
      .nonvisual {
        overflow: hidden;
        height: 0px;
        padding: 0em;
        margin: 0em;
        border: none;        
      }
      
      th.vertical {
        writing-mode: vertical-lr;
        transform: rotate( 180deg );
      }      
      
      th.plain {
        font-weight: normal;
        text-align: left;
      }
      
      td {
        vertical-align: top;
        padding: 0.25em 0.5em 0.25em 0.5em;
      }
      
      td.vertical {
        writing-mode: vertical-lr;
        transform: rotate( 180deg );
      }      
      
      #groups>div {
        max-width: 40em;
      }
      
      #groups>div:nth-child(odd) {
        background-color: mintcream;
      }
      
      #groups>div:nth-child(even) {
        background-color: rgb(240,245,255);        
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
      .grouptitle {
        font-size: 110%;
        font-weight: bold;
      }
      .groupstatus {
        margin-top: 1em;
      }
      .grouptable {
        width: 100%;
      }
.alertList {
  padding: 10px 10px 10px 40px;
  border: 2px solid hsl(206deg 74% 54%);
  border-radius: 4px;
  background: hsl(206deg 74% 90%);
}

.alertList:empty {
  padding: 0px;
  border: none;
  background: none;
}

.alertList li::marker {
  content: "Alert: ";
  font-weight: bold;
}
      
    </style>
    <script lang="JavaScript">
      
const dynamicPageData = ${support.dynamicPageDataAsJSON};
        
    </script>
    <script type="module" src="../javascript/@BUILDTIMESTAMP@/sharepointsub/config.js"></script>
  </head>
  <body>
    <div id="dialogdiv" class="dialogs">
      <!-- no dialog boxes at present -->
    </div>


    <div id="basePage">      
    <div class="section">
    <div class="block">
      <p class="important">${support.importantMessage}</p>
      <h3>LBU Sharepoint Submission Tool</h3>
      <h4>Platform-wide Configuration</h4>
    </div>
    </div>

    <div style="margin-top: 1em;">
    <c:choose>
      <c:when test="${support.allowedToConfigure}">
          <div>
            <table>
              <tr><th></th><th>Settings</th></tr>
              <tr><th>Enabled</th>  <td><input type="checkbox" id="config_enabled"/></td></tr>
            </table>          
            <button id="configdialogSaveButton" value="Close">Save</button>
          </div>
      </c:when>
      <c:otherwise>
          <p>Your role does not allow you to configure this tool.</p>        
      </c:otherwise>
    </c:choose>
        <div style="margin-top: 1em;">
          <p><button id="exitButton" value="Exit">Exit</button></p>
        </div>
    </div>
      
    
    <div class="block">
      <h2 class="nonvisual">Notifications Nonvisual</h2>
      <ul id="toplevelalert" class="alertList"></ul>
      <p class="nonvisual">Notifications will appear above this paragraph. 
        They will tell you about live changes to the content
        of this page in response to the actions of other participants or
        yourself. These notifications are intended for users of screen readers.
      </p>
    </div>
    
    
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
