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
import sharepointsub from "../generated/sharepointsub.js";

let dynamicData = dynamicPageData;

let toolsocket;

        

let platformconfig = null;
let coursesettings = null;

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

  finder.adddropboxButton.addEventListener(   'click', () => addDropbox() );
      
  console.log( dynamicData.webSocketUri );
    
  let handler =
  {
    open()
    {
      toolsocket.sendMessage( new sharepointsub.ConfigurationRequestMessage() );
    },
    
    handleAlert( message )
    {
      alert( message.payload );
    },
    
    handleConfiguration( message )
    {
      console.log( message );
      platformconfig = message.payload.configuration;
      toolsocket.sendMessage( new sharepointsub.CourseSettingsRequestMessage() );
    },
    
    handleCourseSettings( message )
    {
      updateCourseSettings( message );
    }
  
  };
  
  toolsocket = new sharepointsub.ToolSocket( dynamicData.webSocketUri, handler  );  
}


function updateCourseSettings( message )
{
  console.log( message );
  coursesettings = message.payload;
}

function addDropbox()
{
  if ( coursesettings === null )
    return;
  toolsocket.sendMessage( new sharepointsub.CourseSettingsMessage( coursesettings ) );
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
