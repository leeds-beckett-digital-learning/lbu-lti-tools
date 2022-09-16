/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitools.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author jon
 */
  public class ResourceKey implements Serializable
  {
    final String platformId;
    final String resourceId;

    public ResourceKey( 
            @JsonProperty("platformId") String platformId, 
            @JsonProperty("resourceId") String resourceId )
    {
      this.platformId = platformId;
      this.resourceId = resourceId;
      assert( platformId != null && resourceId != null );
    }
    
    public String getPlatformId()
    {
      return platformId;
    }

    public String getResourceId()
    {
      return resourceId;
    }

    
    
    @Override
    public String toString() {
      return platformId + "    " + resourceId;
    }

    @Override
    public boolean equals(Object obj)
    {
      if ( !(obj instanceof ResourceKey ) )
        return false;
      ResourceKey other = (ResourceKey)obj;
      return this.toString().equals( other.toString() );
    }

    @Override
    public int hashCode()
    {
      return platformId.hashCode() | resourceId.hashCode();
    }
  }
  
