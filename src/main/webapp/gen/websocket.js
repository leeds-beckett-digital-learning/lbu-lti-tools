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


/*

===================================
  Generated script, do not edit. 
===================================

*/

const lbultitoolapi = (function () {

  let lib = new Object();
  let nextid = Math.floor( Math.random()*10000 );
  
  lib.ClientMessage = class 
  {
    constructor( messageType, payloadType )
    {
      this.id = nextid++;
      this.messageType = messageType?messageType:null;
      this.payloadType = payloadType?payloadType:null;
      this.replyToId   = null;
      this.payload     = null;
    }
      
    toString()
    {
      var str = "toolmessageversion1.0\n";
      str += "id:" + this.id + "\n";
      if ( this.replyToId )
        str += "replytoid:" + this.replyToId + "\n";
      if ( this.messageType )
        str += "messagetype:" + this.messageType + "\n";
      if ( this.payloadType && this.payload )
      {
        str += "payloadtype:" + this.payloadType + "\npayload:\n" ;
        str += JSON.stringify( this.payload );
      }
      return str;
    }
  };
  
  lib.ServerMessage = class
  {
    constructor( id, replyToId, messageType, payloadType, payload )
    {
      this.id = id;
      this.replyToId   = replyToId;
      this.messageType = messageType;
      this.payloadType = payloadType;
      this.payload     = payload;
    }
  };

  
lib.AddGroupMessage = class extends lib.ClientMessage 
{ 
  constructor()
  {
    super( "AddGroup", null );
    this.payload = null;
  }
};
    

lib.SetResourcePropertiesMessage = class extends lib.ClientMessage 
{ 
  constructor( title, description, stage )
  {
    super( "SetResourceProperties", "uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PgaProperties" );
    this.payload = { "title": title, "description": description, "stage": stage };
  }
};
    

lib.SetGroupPropertiesMessage = class extends lib.ClientMessage 
{ 
  constructor( id, title )
  {
    super( "SetGroupProperties", "uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaChangeGroup" );
    this.payload = { "id": id, "title": title };
  }
};
    

lib.ChangeDatumMessage = class extends lib.ClientMessage 
{ 
  constructor( groupId, fieldId, memberId, value )
  {
    super( "ChangeDatum", "uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaChangeDatum" );
    this.payload = { "groupId": groupId, "fieldId": fieldId, "memberId": memberId, "value": value };
  }
};
    

lib.MembershipMessage = class extends lib.ClientMessage 
{ 
  constructor( id, pids )
  {
    super( "Membership", "uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaAddMembership" );
    this.payload = { "id": id, "pids": pids };
  }
};
    

lib.GetResourceMessage = class extends lib.ClientMessage 
{ 
  constructor()
  {
    super( "GetResource", null );
    this.payload = null;
  }
};
    

lib.GetFormAndDataMessage = class extends lib.ClientMessage 
{ 
  constructor( id )
  {
    super( "GetFormAndData", "uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.Id" );
    this.payload = { "id": id };
  }
};
    


  return lib;
})();


export default lbultitoolapi;
