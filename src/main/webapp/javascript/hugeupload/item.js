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

let pendingFileMap = new Object();

function init()
{
  console.log( "init" );

  console.debug( finder.toplevelalert.ariaLive );
  finder.toplevelalert.ariaLive = 'polite';
  console.debug( "Set ariamixin property" );
  console.debug( finder.toplevelalert.ariaLive );
  
  //finder.toplevelalert.setAttribute( 'aria-live', 'polite' );  
  //console.debug( finder.toplevelalert.getAttribute( 'aria-live' ) );

  arialib.setDialogAlertClass( 'alertList' );
  arialib.setBaseAlertElement( finder.toplevelalert );
  setInterval( updateAlerts, 1000 );
      
  console.debug( dynamicData.webSocketUri );
  
  finder.fileselection.addEventListener( 'change', () => blobUploadTest() );
  finder.startuploadbutton.addEventListener( 'click', () => alert( 'Not implemented yet' ) );
  finder.stopuploadbutton.addEventListener( 'click', () => alert( 'Not implemented yet' ) );
  
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
    
    handleBinaryChunkUploadReq( message )
    {
      processChunkReq( message.payload );
    },
    
    handleAlert( message )
    {
      alert( message.payload );
    },
    
    handleResource( message )
    {
      resource = message.payload;
      console.log( "receiving resource data" );
      console.log( resource );
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

function blobUploadTest()
{
    console.log( "File chunk mapping starting" );
    const fileInput = document.querySelector("input[type=file]");
    if ( fileInput.files.length < 1 )
    {
        alert( "No files selected." );
        return;
    }
    if ( fileInput.files.length > 1 )
    {
        alert( "Only one file may be selected." );
        return;
    }

    fileProgress.action = "digest";    
    fileProgress.file = fileInput.files[0];
    fileProgress.chunkNo = 0;
    fileProgress.maxChunkSize = 10 * 1024 * 1024;

    fileProgress.chunkCount=Math.floor( fileProgress.file.size / fileProgress.maxChunkSize );
    // Remainder? One smaller chunk
    if ( (fileProgress.file.size % fileProgress.maxChunkSize) > 0 )
        fileProgress.chunkCount++;
  
    pendingFileMap = {};
    pendingFileMap.name         = fileProgress.file.name;
    pendingFileMap.lastModified = fileProgress.file.lastModified;
    pendingFileMap.size         = fileProgress.file.size;
    pendingFileMap.type         = fileProgress.file.type;
    pendingFileMap.map          = [];

    console.debug( fileProgress );
    console.debug( pendingFileMap );
    startChunk();
}

function startChunk()
{
  const start = fileProgress.chunkNo * fileProgress.maxChunkSize;
  const end   = ( (start + fileProgress.maxChunkSize) > fileProgress.file.size ) ? fileProgress.file.size : start + fileProgress.maxChunkSize;
  fileProgress.chunkSize=end-start;

  pendingFileMap.map[fileProgress.chunkNo] = {};
  pendingFileMap.map[fileProgress.chunkNo].start = start;
  pendingFileMap.map[fileProgress.chunkNo].end   = end;

  console.debug( "Processing chunk " + fileProgress.chunkNo + " from " + start + " to " + end );

  function handleHash( hash )
  {
    const hashArray = Array.from(new Uint8Array(hash)); // convert buffer to byte array
    const hashHex = hashArray.map((b) => b.toString(16).padStart(2, "0")).join("");
    console.log( "Hash = " + hashHex );        
    pendingFileMap.map[fileProgress.chunkNo].hash   = hashHex;
    if ( ++fileProgress.chunkNo < fileProgress.chunkCount )
      startChunk();
    else
    {
      console.debug( "File mapping complete" );
      console.log( pendingFileMap );
      const message = new hugeupload.FileMapMessage();
      message.payload = pendingFileMap;
      toolsocket.sendMessage( message );
    }
  }

  function chunkLoadProcess( abuffer )
  {
    console.debug( "Buffer slice read into abuffer", abuffer );

    if ( fileProgress.action === "digest" )
    {
      const data = new Uint8Array( abuffer );
      crypto.subtle.digest( "SHA-256", data ).then( (hash) => handleHash( hash ) );
    }
    if ( fileProgress.action === "upload" )
    {
      var bcm = new hugeupload.BinaryChunkMessage();
      bcm.payload.id = "made up id";
      bcm.payload.chunkNo = fileProgress.chunkNo;
      bcm.payload.chunk = data;
      console.debug( "Sending chunk message." );
      toolsocket.sendMessage( bcm );
    }
  }

  fileProgress.chunk = fileProgress.file.slice( start, end );
  fileProgress.chunk.arrayBuffer().then( 
            (abuffer) => chunkLoadProcess( abuffer ),
            (error)   => console.log( "Buffer slice error", error )
          );
}

function processChunkAck( message )
{
  if ( message.chunkNo !== fileProgress.chunkNo )
  {
    alert( "Received mis-matched chunk no acknowledgement." );
    return;
  }
  
  if ( ++fileProgress.chunkNo < fileProgress.chunkCount )
    startChunk();
}

function processChunkReq( message )
{  
  console.debug( message );
  
  // server asked for a chunk to be uploaded
  if ( !fileProgress.file )
  {
    alert( "No file selected for upload. Resuming upload not yet implemented." );
    return;
  }
  
  function chunkUpload( abuffer )
  {
    const data = new Uint8Array( abuffer );
    console.debug( "Buffer slice read into abuffer", abuffer );
    var bcm = new hugeupload.BinaryChunkMessage();
    bcm.payload.id = "made up id";
    bcm.payload.chunkNo = message.chunkNo;
    bcm.payload.chunk = data;
    console.debug( "Sending chunk message." );
    toolsocket.sendMessage( bcm );
  }
  
  console.log( "Slicing file start = " + message.start + " end = " + message.end );
  const chunk = fileProgress.file.slice( message.start, message.end );
  chunk.arrayBuffer().then( 
            (abuffer) => chunkUpload( abuffer ),
            (error)   => console.log( "Buffer slice error", error )
          );
}

window.addEventListener( "load", function(){ init(); } );
document.addEventListener( "DOMContentLoaded", function()
  { 
    console.debug( "DOMContentLoaded event arrived." ); 
    console.debug( finder.toplevelalert );
  } );
