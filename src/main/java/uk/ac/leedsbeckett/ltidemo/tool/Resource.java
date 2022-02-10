/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltidemo.tool;

import java.util.List;
import java.util.Stack;

/**
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
