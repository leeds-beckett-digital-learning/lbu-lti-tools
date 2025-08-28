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

  
  if ( dynamicData.allowedToConfigure )
  {
    // These HTML elements won't exist if the user is not allowed to configure
    finder.configdialogSaveButton.addEventListener(   'click', () => saveConfig() );
  }

  if ( dynamicData.returnURL === null )
  {
    finder.exitButton.style.display = 'none';
  }
  else
  {
    finder.exitButton.addEventListener(   'click', () => exitPage() );    
  }
  
  let handler =
  {
    open()
    {
      if ( dynamicData.allowedToConfigure )
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
      finder.config_enabled.checked = platformconfig.enabled;
    },
    
    handleConfigurationSuccess( message )
    {
      alert( "Configuration success: " + message.payload );
    }
  
  };
  
  toolsocket = new sharepointsub.ToolSocket( dynamicData.webSocketUri, handler  );  
}

function exitPage()
{
  console.log( "Exiting to " + dynamicData.returnURL );
  if ( dynamicData.returnURL !== null )
    window.location.replace( dynamicData.returnURL );
}

function updateAlerts()
{
  arialib.updateAlerts();  
}
  
function addAlert( text )
{
  arialib.addAlert( text );
}

function saveConfig()
{
  if ( !dynamicData.allowedToConfigure )
  {
    alert( "No permission to configure this tool." );
    return;
  }
  
  if ( platformconfig === null )
  {
    alert( "Unable to save configuration because none was received." );
    return;
  }

  let updatedconfig = new Object();
  updatedconfig.enabled = finder.config_enabled.checked;
  console.log( updatedconfig );  
  toolsocket.sendMessage( new sharepointsub.ConfigureMessage( updatedconfig ) );
}

window.addEventListener( "load", function(){ init(); } );
document.addEventListener( "DOMContentLoaded", function()
  { 
    console.log( "DOMContentLoaded event arrived." ); 
    console.log( finder.toplevelalert );
  } );
