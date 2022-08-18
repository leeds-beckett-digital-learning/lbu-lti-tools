/*
 * Copyright 2022 Leeds Beckett University.
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

package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment;

import java.util.HashMap;

/**
 * A store of resources which can be retrieved using keys. At present all
 * resources stay in the store until the store is garbage collected after the
 * web application shuts down. All resources are lost entirely at shut down
 * in this demo. A proper implementation would store data on file or in a 
 * database and would purge memory of resources that haven't been used for a
 * while.
 * 
 * @author jon
 */
public class PeerGroupResourceStore
{
  HashMap<String,HashMap<String,PeerGroupResource>> map = new HashMap<>();
  
  /**
   * Find a resource keyed by platform ID and resource ID with option to
   * create the resource if it doesn't exist yet.
   * 
   * @param platform ID of the platform.
   * @param resource ID of the resource.
   * @param create Set true if the resource should be created if it doesn't already exist.
   * @return The resource or null if it wasn't found and creation wasn't requested.
   */
  public PeerGroupResource get( String platform, String resource, boolean create )
  {
    HashMap<String,PeerGroupResource> platformmap = map.get( platform );
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
    
    PeerGroupResource r = platformmap.get( resource );
    if ( r == null && create )
    {
      r = new PeerGroupResource();
      platformmap.put( resource, r );
    }
    return r;
  }
  
  /**
   * Fetch a dump of the entire store for debugging.
   * 
   * @return Multi-line text containing description of all resources.
   */
  public synchronized String dump()
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append( "Resource Store Contents\n" );
    for ( String p : map.keySet() )
    {
      sb.append( "  Platform " + p + "\n" );
      HashMap<String,PeerGroupResource> platformmap = map.get( p );
      for ( String rid : platformmap.keySet() )
      {
        sb.append( "    Resource " + rid + "\n" );
        PeerGroupResource r = platformmap.get( rid );
        sb.append( "    Resource exists " + (r!=null) + "\n" );
      }
    }
    return sb.toString();
  }
}
