/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitools.tool;

import java.io.Serializable;

/**
 *
 * @author jon
 */
  public class ResourceKey implements Serializable
  {
    final String platform;
    final String resource;

    public ResourceKey(String platform, String resource)
    {
      this.platform = platform;
      this.resource = resource;
      assert( platform != null && resource != null );
    }
    
    public String getPlatform()
    {
      return platform;
    }

    public String getResource()
    {
      return resource;
    }

    @Override
    public String toString() {
      return platform + "    " + resource;
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
      return platform.hashCode() | resource.hashCode();
    }
  }
  
