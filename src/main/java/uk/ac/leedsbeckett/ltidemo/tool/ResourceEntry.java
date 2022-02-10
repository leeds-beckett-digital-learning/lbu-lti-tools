/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltidemo.tool;

/**
 *
 * @author jon
 */
public class ResourceEntry
{
  long timestamp;
  String person;
  String message;

  public ResourceEntry( String person, String message )
  {
    this.timestamp = System.currentTimeMillis();
    this.person = person;
    this.message = message;
  }
  
  public long getTimestamp()
  {
    return timestamp;
  }

  public String getPerson()
  {
    return person;
  }

  public String getMessage()
  {
    return message;
  }
}
