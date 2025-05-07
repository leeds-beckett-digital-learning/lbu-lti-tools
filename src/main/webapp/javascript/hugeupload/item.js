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

let loading;

let resource = null;
        

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
  
  finder.blobuploadtestbutton.addEventListener( 'click', () => blobUploadTest() );
  
  let handler =
  {
    open()
    {
      toolsocket.sendMessage( new hugeupload.GetResourceMessage() );
    },
    
    handleBinaryTest( message )
    {
      console.log( message.payload );
    },
    
    handleBinaryChunkUploadAck( message )
    {
      processChunkAck( message.payload );
    },
    
    handleAlert( message )
    {
      alert( message.payload );
    },
    
    handleResource( message )
    {
      resource = message.payload;
      updateResource();
    },
            
    handleConfiguration( message )
    {
      console.log( message );
    }
  };
  
  toolsocket = new hugeupload.ToolSocket( dynamicData.webSocketUri, handler  );  
}

function updateAlerts()
{
  arialib.updateAlerts();  
}
  
function addAlert( text )
{
  arialib.addAlert( text );
}

function updateResource()
{
  console.log( "Updating UI to reflect changes to resource." );
}

function blobUploadTestX()
{
  var btm = new hugeupload.BinaryTestMessage();
  btm.payload.a = "hello";
  btm.payload.b = 23;
  btm.payload.c = new Uint8Array([11,22,33,44,55,66,77,88,99]);
  toolsocket.sendMessage( btm );
}
  
async function blobUploadTest()
{
    console.log( "Uploading starting" );
    const fileInput = document.querySelector("input[type=file]");
    if ( fileInput.files.length < 1 )
    {
        alert( "No files selected." );
        return;
    }

    fileProgress.file = null;
    fileProgress.maxChunkSize = 16 * 1024; // 10 MB
    fileProgress.chunkSize=0;
    fileProgress.chunkNo=0;
    fileProgress.chunkCount=0;
    fileProgress.chunk = null;
    fileProgress.chunkAckWait = false;
    fileProgress.reader = new FileReader();
    fileProgress.reader.addEventListener( 'load', (e) => sendIncomingChunk( e ) );

    
    for ( var f=0; f<fileInput.files.length; f++ )
    {
        processOneFile( fileInput.files[f] );
    }

    console.log( "Uploading done" );
}


function processOneFile( file )
{    
    console.log( " Name of file: " + file.name );
    console.log( "Last modified: " + file.lastModified );
    console.log( "         Size: " + file.size );
    console.log( "         Type: " + file.type );

    fileProgress.file = file;
    fileProgress.chunkNo=0;
    fileProgress.chunkCount=Math.floor( file.size / fileProgress.maxChunkSize );
    if ( (file.size % fileProgress.maxChunkSize) > 0 )
        fileProgress.chunkCount++;
    console.log( fileProgress );
    startChunk();
}

function startChunk()
{
    const start = fileProgress.chunkNo * fileProgress.maxChunkSize;
    const end   = ( (start + fileProgress.maxChunkSize) > fileProgress.file.size ) ? fileProgress.file.size : start + fileProgress.maxChunkSize;
    fileProgress.chunkSize=end-start;
    
    console.log( "Processing chunk " + fileProgress.chunkNo + " from " + start + " to " + end );
    
    fileProgress.chunk = fileProgress.file.slice( start, end );
    fileProgress.reader.readAsArrayBuffer( fileProgress.chunk );
    fileProgress.chunkAckWait = true;
}

async function sendIncomingChunk( e )
{
    console.log( e );
    console.log( fileProgress );
    const data = new Uint8Array( fileProgress.reader.result );  // result is an ArrayBuffer because of readAsArrayBuffer()
    const hash = await crypto.subtle.digest( "SHA-256", data );
    const hashArray = Array.from(new Uint8Array(hash)); // convert buffer to byte array
    const hashHex = hashArray.map((b) => b.toString(16).padStart(2, "0")).join("");
    console.log( "Hash = " + hashHex );

    var bcm = new hugeupload.BinaryChunkMessage();
    bcm.payload.id = "made up id";
    bcm.payload.chunkNo = fileProgress.chunkNo;
    bcm.payload.chunk = data;
    toolsocket.sendMessage( bcm );
  
}

function processChunkAck( message )
{
  if ( message.chunkNo !== fileProgress.chunkNo )
  {
    alert( "Received mis-matched chunk no acknowledgement." );
    return;
  }
  
  fileProgress.chunkAckWait = false;
  if ( ++fileProgress.chunkNo < fileProgress.chunkCount )
    startChunk();
}

window.addEventListener( "load", function(){ init(); } );
document.addEventListener( "DOMContentLoaded", function()
  { 
    console.log( "DOMContentLoaded event arrived." ); 
    console.log( finder.toplevelalert );
  } );
