/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltidemo.tool;

import java.util.HashMap;

/**
 *
 * @author jon
 */
public class ResourceStore
{
  HashMap<String,HashMap<String,Resource>> map = new HashMap<>();
  
  public Resource get( String platform, String resource, boolean create )
  {
    HashMap<String,Resource> platformmap = map.get( platform );
    if ( platformmap == null )
    {
      if ( create )
      {
        platformmap = new HashMap<>();
        map.put( platform, platformmap );
      }
      else
        return null;
    }
    
    Resource r = platformmap.get( resource );
    if ( r == null && create )
    {
      r = new Resource();
      platformmap.put( resource, r );
    }
    return r;
  }
  
  public synchronized String dump()
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append( "Resource Store Contents\n" );
    for ( String p : map.keySet() )
    {
      sb.append( "  Platform " + p + "\n" );
      HashMap<String,Resource> platformmap = map.get( p );
      for ( String rid : platformmap.keySet() )
      {
        sb.append( "    Resource " + rid + "\n" );
        Resource r = platformmap.get( rid );
        for ( ResourceEntry entry : r.getEntries() )
          sb.append( "      Entry " + entry.timestamp + " " + entry.person + " {" + entry.message + "}\n" );
      }
    }
    return sb.toString();
  }
}
