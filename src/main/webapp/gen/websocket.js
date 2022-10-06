/*===================================
     Generated script, do not edit.  
  ===================================*/


class Message
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
}


class getformanddataMessage extends Message
{
  constructor( id )
  {
    super( "getformanddata", "uk.ac.leedsbeckett.ltitools.peergroupassessment.data.Id" );
    this.payload = { "id": id };
  }
}

class addgroupMessage extends Message
{
  constructor()
  {
    super( "addgroup", null );
    this.payload = null;
  }
}

class setresourcepropertiesMessage extends Message
{
  constructor( title, description, stage )
  {
    super( "setresourceproperties", "uk.ac.leedsbeckett.ltitools.peergroupassessment.data.PeerGroupResourceProperties" );
    this.payload = { "title": title, "description": description, "stage": stage };
  }
}

class GetResourceMessage extends Message
{
  constructor()
  {
    super( "GetResource", null );
    this.payload = null;
  }
}

class membershipMessage extends Message
{
  constructor( id, pids )
  {
    super( "membership", "uk.ac.leedsbeckett.ltitools.peergroupassessment.data.PeerGroupAddMembership" );
    this.payload = { "id": id, "pids": pids };
  }
}

class setgrouppropertiesMessage extends Message
{
  constructor( id, title )
  {
    super( "setgroupproperties", "uk.ac.leedsbeckett.ltitools.peergroupassessment.data.PeerGroupChangeGroup" );
    this.payload = { "id": id, "title": title };
  }
}

class changedatumMessage extends Message
{
  constructor( groupId, fieldId, memberId, value )
  {
    super( "changedatum", "uk.ac.leedsbeckett.ltitools.peergroupassessment.data.PeerGroupChangeDatum" );
    this.payload = { "groupId": groupId, "fieldId": fieldId, "memberId": memberId, "value": value };
  }
}

