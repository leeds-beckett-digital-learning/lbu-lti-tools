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
      body { font-family: sans-serif; padding: 1em 1em 1em 1em; max-width: 50em; }
      h2 { padding-top: 2em; }
      input { margin: 1em 1em 1em 1em; }
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
          <table>
            <tr>
              <td valign="top">
                <h4>Which do you want to enrol on?</h4>
                <select size="10" id="searchresults"></select>  
              </td>
              <td valign="top">
                <div id="whoBlock">
                <h4>Who authorised you to enrol yourself?</h4>
                <select size="10" id="reason">
                  <option value="none">Select from this list.</option>
                  <option value="coursedirector">Myself - I am the course director.</option>
                  <option value="moduleleader">Myself - I am the module/community leader.</option>
                  <option value="admin">Myself - I am the course administrator.</option>
                  <option value="directorpermit">The course director has asked me to self enrol.</option>
                  <option value="leaderpermit">The module leader has asked me to self enrol.</option>
                  <option value="sysadmin">Myself - I am a Digital Learning Service system administrator.</option>
                </select>
                <div id="authDiv" style="display: none;">
                  <h4>Name of the <span id="authNameTitle">authoriser</span></h4>
                  <p><input id="authName"/></p>
                  <h4>Email address of the <span id="authEmailTitle">authoriser</span></h4>
                  <p><input id="authEmail"/></p>
                </div>
                </div>
              </td>
            </tr>
          </table>
        </div>
        <div class="dialog_form_actions">
          <button id="searchdialogEnrolButton" value="Close">Enrol</button>
          <button id="searchdialogCloseButton" value="Close">Close</button>
        </div>
      </div>

      <c:if test="${support.allowedToConfigure}">
      <div role="dialog" id="configdialog" aria-labelledby="configdialogLabel" aria-modal="true" class="hidden">
        <h3 id="configdialogLabel">Configure Staff Self Enrol</h3>
        <div>
          <table>
            <tr><th></th><th>Permissions</th></tr>
            <tr><th>Module/Community Instructor/Leader Can Deep Link</th>  <td><input type="checkbox" id="config_membershipInstructorDeepLinkPermitted"/></td></tr>
            <tr><th></th><th>Module Enrol</th><th>Community Enrol</th></tr>
            <tr><th>Validation RegEx</th>                <td><input id="config_courseSearchValidation"/></td>     <td><input id="config_organizationSearchValidation"/></td></tr>
            <tr><th>Post Search Filter RegEx</th>        <td><input id="config_courseSearchFilter"/></td>         <td><input id="config_organizationSearchFilter"/></td></tr>
            <tr><th>Advice</th>                          <td><textarea id="config_courseAdvice"></textarea></td>  <td><textarea id="config_organizationAdvice"></textarea></td></tr>
            <tr><th>Email text</th>                      <td><textarea id="config_courseEmail"></textarea></td>   <td><textarea id="config_organizationEmail"></textarea></td></tr>
            <tr><th></th><th>Staff Training Enrol</th></tr>
            <tr><th>Search Term</th>                     <td><input id="config_trainingSearchSpecification"/></td></tr>
            <tr><th>Post Search Filter RegEx</th>        <td><input id="config_trainingSearchFilter"/></td></tr>
            <tr><th>Advice</th>                          <td><textarea id="config_trainingAdvice"></textarea></td></tr>
            <tr><th></th><th>Email notification</th></tr>
            <tr><th>SMTP Host Name</th>                  <td><input id="config_smtpHost"/></td></tr>
            <tr><th>Admin Email Address</th>             <td><input id="config_adminEmailAddress"/></td></tr>
          </table>          
        </div>
        <div class="dialog_form_actions">
          <button id="configdialogSaveButton" value="Close">Save</button>
          <button id="configdialogCancelButton" value="Close">Cancel</button>
        </div>
      </div>
      </c:if>
      
    </div>


    <div id="basePage">
      
    <h1>Self Enrol on Modules and Course Communities</h1>
        
    <c:choose>
      <c:when test="${support.allowedToEnrol}">
        
        <h2 id="coursetitle">Enrol on a module</h2>
        <div id="courseadvice">${support.courseAdvice}</div>
        <p><form action="javascript:void(0);" aria-labelledby="coursetitle">
          <label for="courseid">CRN Input Box</label>
          <input id="courseid"/>
          <button id="searchCourseButton">Search Modules</button>
        </form></p>

        <h2 id="orgtitle">Enrol on a course community</h2>
        <div id="orgadvice">${support.organizationAdvice}</div>
        <p><form action="javascript:void(0);" aria-labelledby="orgtitle">
          <label for="orgid">Award Code Input Box</label>
          <input id="orgid"/>
          <button id="searchOrgButton">Search Course Communities</button>
        </form></p>

        <h2 id="trainingtitle">Enrol on a training module</h2>
        <div id="trainingadvice">${support.trainingAdvice}</div>
        <p><form action="javascript:void(0);" aria-labelledby="trainingtitle">
          <button id="searchTrainingButton">Search Training Modules</button>
        </form></p>
          
      </c:when>
      <c:otherwise>
        <p>Your role set by system that launched this tool means that you
        cannot use it.</p>
      </c:otherwise>
    </c:choose>
    
    <c:if test="${support.allowedToConfigure}">
      <div style="margin-top: 10em;">
        <hr>
        <p>Your role allows you to configure this tool.</p>
        <p><button id="configureButton">Configure</button></p>          
      </div>
    </c:if>
    
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
