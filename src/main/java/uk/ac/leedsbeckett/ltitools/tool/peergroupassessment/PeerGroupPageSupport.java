/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment;

import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupResource;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.store.PeerGroupAssessmentStore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.ltitools.tool.LtiPageSupport;

/**
 * Support class for PeerGroupPage related JSP pages.
 * @author jon
 */
public class PeerGroupPageSupport extends LtiPageSupport
{
  static final Logger logger = Logger.getLogger( PeerGroupPageSupport.class.getName() );
  
  PeerGroupAssessmentTool tool;
  PeerGroupAssessmentStore store;
  PeerGroupAssessmentState pgaState;
  PeerGroupResource pgaResource;
  String websocketuri;
  
  @Override
  public void setRequest(HttpServletRequest request) throws ServletException
  {
    super.setRequest( request );
    logger.log( Level.FINE, "setRequest() state id = {0}", state.getId() );
    pgaState = (PeerGroupAssessmentState)state.getAppSessionState();
    if ( pgaState == null )
      throw new ServletException( "Could not find peer group assessment tool session data." );
    tool = (PeerGroupAssessmentTool)appcontext.getTool( state.getToolKey() );
    store = tool.getPeerGroupAssessmentStore();
    pgaResource = store.getResource( pgaState.getResourceKey(), true );

    String base = computeWebSocketUri( PeerGroupAssessmentEndpoint.class.getAnnotation( ServerEndpoint.class ) );
    websocketuri = base + "?state=" + state.getId();
  }

  public PeerGroupResource getPgaResource()
  {
    return pgaResource;
  }
  
  public String getPersonId()
  {
    return pgaState.getPersonId();
  }
  
  public String getPersonName()
  {
    return pgaState.getPersonName();
  }
  
  public boolean isAllowedToParticipate()
  {
    return pgaState.isAllowedToParticipate();
  }

  public boolean isAllowedToManage()
  {
    return pgaState.isAllowedToManage();
  }
  
  
  public String getWebsocketUri()
  {
    return websocketuri;
  }

  public String getMessagingScript()
  {
    // Create an endpoint temporarily just to fetch the
    // javascript.  Not great - should be possible to get script
    // from a static method....
    PeerGroupAssessmentEndpoint e = new PeerGroupAssessmentEndpoint();
    return e.getJavaScript();
  }

  public boolean isDebugging()
  {
    return logger.isLoggable( Level.FINE );
  }
  
  public String getDump()
  {
    StringBuilder sb = new StringBuilder();
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      mapper.enable( SerializationFeature.INDENT_OUTPUT );
      mapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
      mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
      sb.append( "Launch State\n" );    
      sb.append( "=============\n" );
      sb.append( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( state ) );
      sb.append( "\n\n" );    
      sb.append( "Web Socket\n" );
      sb.append( "=============\n" );
      sb.append( getWebsocketUri() );
      sb.append( "\nResource\n" );    
      sb.append( "=============\n" );
      sb.append( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( pgaResource ) );
    }
    catch ( JsonProcessingException ex )
    {
      logger.log( Level.SEVERE, "Unable to dump data as JSON.", ex );
      return "Unable to dump data as JSON.\n";
    }
    return sb.toString();
  }
}
