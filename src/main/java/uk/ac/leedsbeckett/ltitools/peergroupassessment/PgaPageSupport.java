/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitools.peergroupassessment;

import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.store.StoreCluster;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.ltitoolset.page.ToolPageSupport;

/**
 * Support class for PeerGroupPage related JSP pages.
 * @author jon
 */
public class PgaPageSupport extends ToolPageSupport
{
  static final Logger logger = Logger.getLogger(PgaPageSupport.class.getName() );
  
  PeerGroupAssessmentTool tool;
  StoreCluster store;
  PgaToolLaunchState pgaState;
  PeerGroupResource pgaResource;
  String websocketuri;
  
  @Override
  public void setRequest(HttpServletRequest request) throws ServletException
  {
    super.setRequest( request );
    logger.log( Level.FINE, "setRequest() state id = {0}", state.getId() );
    if ( logger.isLoggable( Level.FINE ) )
    {
      ObjectMapper mapper = new ObjectMapper();
      logger.fine( "Launch State" );
      try
      { logger.fine( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( state ) ); }
      catch ( JsonProcessingException ex )
      { logger.log( Level.SEVERE, "Problem dumping state.", ex ); }
    }
    
    pgaState = (PgaToolLaunchState)state.getToolLaunchState();
    if ( pgaState == null )
      throw new ServletException( "Could not find peer group assessment tool session data." );
    logger.log( Level.FINE, "resource key = {0}", pgaState.getResourceKey() );
    tool = (PeerGroupAssessmentTool)toolCoordinator.getTool( state.getToolKey() );
    store = tool.getPeerGroupAssessmentStore();
    pgaResource = store.getResource( pgaState.getResourceKey(), true );

    String base = computeWebSocketUri( PgaEndpoint.class.getAnnotation( ServerEndpoint.class ) );
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
