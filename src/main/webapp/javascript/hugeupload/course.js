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
import hugeupload from "../generated/hugeupload.js";

let dynamicData = dynamicPageData;

let toolsocket;

        

let platformconfig = null;

const fileProgress = new Object();

function init()
{
  console.log( "init" );
  console.log( finder.toplevelalert );

  console.log( finder.toplevelalert.ariaLive );
  finder.toplevelalert.ariaLive = 'polite';
  console.log( "Set ariamixin property" );
  console.log( finder.toplevelalert.ariaLive );
  
  //finder.toplevelalert.setAttribute( 'aria-live', 'polite' );  
  //console.log( finder.toplevelalert.getAttribute( 'aria-live' ) );

  arialib.setDialogAlertClass( 'alertList' );
  arialib.setBaseAlertElement( finder.toplevelalert );
  setInterval( updateAlerts, 1000 );
      
  console.log( dynamicData.webSocketUri );

  finder.forminstructorallow.addEventListener(    'click', () => changePermission( "instructor", true  ) );
  finder.forminstructordisallow.addEventListener( 'click', () => changePermission( "instructor", false ) );
  finder.formstudentallow.addEventListener(       'click', () => changePermission( "student",    true  ) );
  finder.formstudentdisallow.addEventListener(    'click', () => changePermission( "student",    false ) );
  
  finder.studentAddButton.addEventListener(    'click', () => addMimeDialog( "student" ) );    
  finder.instructorAddButton.addEventListener( 'click', () => addMimeDialog( "instructor" ) );    
    
  let handler =
  {
    open()
    {
      toolsocket.sendMessage( new hugeupload.GetCourseMessage() );
    },
    
    handleAlert( message )
    {
      alert( message.payload );
    },
    
    handleResource( message )
    {
    },
            
    handleCourse( message )
    {
      updateCourseConfiguration( message );
    },
    
    handleConfiguration( message )
    {
    },
    
    handleConfigurationSuccess( message )
    {
    }
  
  };
  
  toolsocket = new hugeupload.ToolSocket( dynamicData.webSocketUri, handler  );  
}


function updateCourseConfiguration( message )
{
  console.log( message );
  const course = message.payload;
  
  if ( !dynamicData.allowedToConfigure )
  {
    if ( finder.instructorAddAllowanceRow )
      finder.instructorAddAllowanceRow.remove();
    if ( finder.studentAddAllowanceRow )
      finder.studentAddAllowanceRow.remove();
    const radiobuttons = new Array();
    radiobuttons.push( finder.forminstructorallow );
    radiobuttons.push( finder.forminstructordisallow );
    radiobuttons.push( finder.formstudentallow );
    radiobuttons.push( finder.formstudentdisallow );
    for ( var i=0; i<radiobuttons.length; i++ )
      if ( radiobuttons[i] )
        radiobuttons[i].disabled = true;
  }
  
  finder.instructorAllowanceTableBody.innerHtml = "";
  finder.studentAllowanceTableBody.innerHtml = "";
  
  finder.forminstructorallow.checked = course.instructorUploadAllowed;
  finder.forminstructordisallow.checked = !course.instructorUploadAllowed;
  finder.formstudentallow.checked = course.studentUploadAllowed;
  finder.formstudentdisallow.checked = !course.studentUploadAllowed;
  
  var rows;
          
  rows = "";
  for ( var i=0; i<course.instructorUploadAllowances.length; i++ )
  {
    rows += 
            "<tr><td>" + 
            course.instructorUploadAllowances[i].mimeType + 
            "</td><td>" + 
            course.instructorUploadAllowances[i].maxSize + 
            "</td><td>";
    if ( dynamicData.allowedToConfigure )
      rows += "<button>Remove</button>";
    rows += "</td></th>\n";
  }
  finder.instructorAllowanceTableBody.innerHTML = rows;
  
  rows = "";
  for ( var i=0; i<course.studentUploadAllowances.length; i++ )
  {
    rows += 
            "<tr><td>" + 
            course.studentUploadAllowances[i].mimeType + 
            "</td><td>" + 
            course.studentUploadAllowances[i].maxSize + 
            "</td><td>";
    if ( dynamicData.allowedToConfigure )
      rows += "<button>Remove</button>";
    rows += "</td></th>\n";
  }
  finder.studentAllowanceTableBody.innerHTML = rows;
  
}

function addMimeDialog( target )
{
  console.log( target );
}

function changePermission( target, allow )
{
  console.log( target );
  console.log( allow );
}


function updateAlerts()
{
  arialib.updateAlerts();  
}
  
function addAlert( text )
{
  arialib.addAlert( text );
}


window.addEventListener( "load", function(){ init(); } );
document.addEventListener( "DOMContentLoaded", function()
  { 
    console.log( "DOMContentLoaded event arrived." ); 
    console.log( finder.toplevelalert );
  } );
