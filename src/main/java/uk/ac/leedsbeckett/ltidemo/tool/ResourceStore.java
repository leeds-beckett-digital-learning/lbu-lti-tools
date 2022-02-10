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
