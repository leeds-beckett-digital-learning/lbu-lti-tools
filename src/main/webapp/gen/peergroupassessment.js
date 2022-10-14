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

import lbultitoolapi from "./endpoint.js";

const peergroupassessment = (function () {

  let servermessagenames =
  [
{
  name:"Alert",
  class:"class java.lang.String"
},
{
  name:"Resource",
  class:"class uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource"
},
{
  name:"ResourceProperties",
  class:"class uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PgaProperties"
},
{
  name:"Group",
  class:"class uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaChangeGroup"
},
{
  name:"FormAndData",
  class:"class uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaFormAndData"
},
{
  name:"Data",
  class:"class uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PeerGroupData"
}

  ];
  
  
  let lib = new Object();

  lib.ToolSocket = class extends lbultitoolapi.ToolSocket
  {
    validateHandler()
    {
      console.log( "Validating handler" );
      if ( this.handler.open instanceof Function )
        this.handler.open.validated = true;
      else
        console.warn( "Handler lacks open function." );
      
      for ( let i=0; i<servermessagenames.length; i++ )
      {
        let funcname = 'handle'+servermessagenames[i].name;
        if ( this.handler[funcname] instanceof Function )
          this.handler[funcname].validated = true;
        else
          console.warn( "Handler lacks handler function, " + funcname + "." );          
      }
      
      for ( let o in this.handler )
      {
        if ( this.handler[o] instanceof Function )
        {
          if ( !this.handler[o].validated )
            console.warn( "Handler has additional inessential function, " + o + "." );          
        }
      }
    }
  };
  
  
lib.AddGroupMessage = class extends lbultitoolapi.ClientMessage 
{ 
  constructor()
  {
    super( "AddGroup", null );
    this.payload = null;
  }
};
    

lib.ClearEndorsementsMessage = class extends lbultitoolapi.ClientMessage 
{ 
  constructor( id )
  {
    super( "ClearEndorsements", "uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.Id" );
    this.payload = { "id": id };
  }
};
    

lib.SetResourcePropertiesMessage = class extends lbultitoolapi.ClientMessage 
{ 
  constructor( title, description, stage )
  {
    super( "SetResourceProperties", "uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PgaProperties" );
    this.payload = { "title": title, "description": description, "stage": stage };
  }
};
    

lib.SetGroupPropertiesMessage = class extends lbultitoolapi.ClientMessage 
{ 
  constructor( id, title )
  {
    super( "SetGroupProperties", "uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaChangeGroup" );
    this.payload = { "id": id, "title": title };
  }
};
    

lib.ChangeDatumMessage = class extends lbultitoolapi.ClientMessage 
{ 
  constructor( groupId, fieldId, memberId, value )
  {
    super( "ChangeDatum", "uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaChangeDatum" );
    this.payload = { "groupId": groupId, "fieldId": fieldId, "memberId": memberId, "value": value };
  }
};
    

lib.MembershipMessage = class extends lbultitoolapi.ClientMessage 
{ 
  constructor( id, pids )
  {
    super( "Membership", "uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaAddMembership" );
    this.payload = { "id": id, "pids": pids };
  }
};
    

lib.EndorseDataMessage = class extends lbultitoolapi.ClientMessage 
{ 
  constructor( groupId, manager )
  {
    super( "EndorseData", "uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaEndorseData" );
    this.payload = { "groupId": groupId, "manager": manager };
  }
};
    

lib.GetResourceMessage = class extends lbultitoolapi.ClientMessage 
{ 
  constructor()
  {
    super( "GetResource", null );
    this.payload = null;
  }
};
    

lib.GetFormAndDataMessage = class extends lbultitoolapi.ClientMessage 
{ 
  constructor( id )
  {
    super( "GetFormAndData", "uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.Id" );
    this.payload = { "id": id };
  }
};
    


  return lib;
})();


export default peergroupassessment;
