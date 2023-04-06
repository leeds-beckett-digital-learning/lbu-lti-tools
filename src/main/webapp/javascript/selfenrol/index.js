/* 
 * Copyright 2022 maber01.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import finder from "../common/domutil.js";
import arialib from "../common/aria.js";
import selfenrol from "../generated/selfenrol.js";

let dynamicData = dynamicPageData;

let toolsocket;

let loading;

let dataentryopening=true;

let coursespecvalidator;
let orgspecvalidator;

function init()
{
  console.log( "init" );
  console.log( finder.toplevelalert );
  console.log( finder.toplevelalert.ariaLive );
  finder.toplevelalert.ariaLive = 'polite';
  console.log( "Set ariamixin property" );
  console.log( finder.toplevelalert.ariaLive );
  
  arialib.setDialogAlertClass( 'alertList' );
  arialib.setBaseAlertElement( finder.toplevelalert );
  setInterval( updateAlerts, 1000 );
    
  console.log( dynamicData.webSocketUri );

  coursespecvalidator = new RegExp( dynamicData.courseSearchValidation );
  orgspecvalidator    = new RegExp( dynamicData.orgSearchValidation    );

  finder.searchdialogEnrolButton.addEventListener( 'click', () => enrolOnCourse() );
  finder.searchdialogCloseButton.addEventListener( 'click', () => arialib.closeDialog(finder.searchdialog)    );
  finder.searchCourseButton.addEventListener( 'click', () => searchForCourses() );
  finder.searchOrgButton.addEventListener( 'click', () => searchForOrgs() );
  
  let handler =
  {
    open()
    {
    },
    
    handleAlert( message )
    {
      console.log( "Rxed alert " + message.payload );
      alert( message.payload );
    },
    
    handleCourseInfoList( message )
    {
      console.log( "Rxed course info list " + message.payload );
      let content = "";
      if ( message.payload.length === 0 )
      {
        finder.searchresults.innerHTML = "<option value=\"\">Nothing found.</option>\n";
        return;
      }
      for ( const d of message.payload )
        content += "<option value=\"" + d.externalId + "\">" + d.externalId + " ~ " + d.name + "</option>\n";
      finder.searchresults.innerHTML = content;
    },
    
    handleEnrolSuccess( message )
    {
      alert( "Enrolment succeeded." );
      arialib.closeDialog(finder.searchdialog);
    }
  };
  
  toolsocket = new selfenrol.ToolSocket( dynamicData.webSocketUri, handler  );  
}

function updateAlerts()
{
  arialib.updateAlerts();  
}
  
function addAlert( text )
{  arialib.addAlert( text );
}

function clearSearch()
{
  finder.searchresults.innerHTML = "<option value=\"\">Waiting for results...</option>";
}

function searchForCourses()
{
  var spec = finder.courseid.value;
  if ( !coursespecvalidator.test( spec ) )
  {
    alert( "The input was not valid. Please read the notes on the page about how to search." );
    return;
  }
  toolsocket.sendMessage( new selfenrol.SearchMessage( "course", spec ) );
  clearSearch();
  arialib.openDialog( 'searchdialog', finder.searchCourseButton );
}

function searchForOrgs()
{
  var spec = finder.orgid.value;
  if ( !orgspecvalidator.test( spec ) )
  {
    alert( "The input was not valid. Please read the notes on the page about how to search." );
    return;
  }
  toolsocket.sendMessage( new selfenrol.SearchMessage( "organization", spec ) );  
  clearSearch();
  arialib.openDialog( 'searchdialog', finder.searchCourseButton );
}

function enrolOnCourse()
{
  var id = finder.searchresults.value;
  if ( id === "" )
  {
    alert( "No course is selected." );
    return;
  }
  
  toolsocket.sendMessage( new selfenrol.EnrolRequestMessage( id ) );  
}

window.addEventListener( "load", function(){ init(); } );
document.addEventListener( "DOMContentLoaded", function()
  { 
    console.log( "DOMContentLoaded event arrived." ); 
    console.log( finder.toplevelalert );
  } );
