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
package uk.ac.leedsbeckett.ltitools.peergroupassessment;

import uk.ac.leedsbeckett.ltitools.peergroupassessment.blackboard.BlackboardGroupSets;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PeerGroupData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaConfigurationMessage;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaDataList;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PgaProperties;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageName;

/**
 * This enum represents all the types of message that this tool sends from 
 * the server end of its web socket to the client. This class will be used
 * to generate javascript.
 * 
 * @author maber01
 */
public enum PgaServerMessageName implements ToolMessageName
{
  /**
   * Message contains a simple alert message for the user.
   */
  Alert(              "Alert",              String.class ),
  
  /**
   * Message contains the PGA as created by the manager(s).
   */
  Resource(           "Resource",           PeerGroupResource.class ),
  
  /**
   * Message contains only the basic properties of the PGA.
   */
  ResourceProperties( "ResourceProperties", PgaProperties.class     ),
  
  /**
   * Message contains the PGA resource and the resource's selected form.
   */
  Form(               "Form",               PeerGroupForm.class    ),
    
  /**
   * Message contains data from participants relating to a specific
   * group.
   */
  Data(               "Data",               PeerGroupData.class     ),
  
  /**
   * A list of data objects for multiple groups.
   */
  DataList(           "DataList",           PgaDataList.class       ),
  
  /**
   * A list of data objects for multiple groups.
   */
  Export(             "Export",             String.class       ),
  
  /**
   * A list of data objects for multiple groups.
   */
  BlackboardGroupSets( "BlackboardGroupSets", BlackboardGroupSets.class       ),

  /**
   * Contains platform wide configuration.
   */
  Configuration(        "Configuration",        PgaConfigurationMessage.class ),
  
  /**
   * Indicates that configuration was saved successfully.
   */
  ConfigurationSuccess( "ConfigurationSuccess", String.class);
  
  /**
   * Each constant has a name which can be used in encoded messages passing
   * through the web socket.
   */
  private final String name;
  
  /**
   * The class of the payload for this message. Note that multiple
   * message names can use the same payload class.
   */
  private final Class payloadClass;

  /**
   * Simple constructor. Not public - only used to create the defined
   * constants.
   * 
   * @param name The name.
   * @param payloadClass The payload class.
   */
  PgaServerMessageName( String name, Class payloadClass )
  {
    this.name = name;
    this.payloadClass = payloadClass;
  }

  /**
   * Get the name of the message.
   * 
   * @return The name.
   */
  @Override
  public String getName()
  {
    return name;
  }

  /**
   * Get the payload class for this message.
   * 
   * @return The payload class.
   */
  @Override
  public Class getPayloadClass()
  {
    return payloadClass;
  }  
}
