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

import java.util.List;
import java.util.Stack;

/**
 * An object that represents the resource which the user is accessing after the
 * LTI launch. A simple stack of log entries.
 * 
 * @author jon
 */
public class Resource
{
  Stack<ResourceEntry> entries = new Stack<>();

  public Resource()
  {
    entries.add( new ResourceEntry( "System", "Resource initiallised by system." ) );
  }
  
  public synchronized void addEntry( String person )
  {
    entries.insertElementAt( new ResourceEntry( person, "Added Entry" ), 0 );
    while ( entries.size() > 10 )
      entries.pop();
  }
  
  public synchronized void clearEntries( String person )
  {
    entries.clear();
    entries.push( new ResourceEntry( person, "Cleared entries" ) );
  }
  
  public List<ResourceEntry> getEntries()
  {
    return entries;
  }
}
