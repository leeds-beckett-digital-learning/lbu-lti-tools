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

package uk.ac.leedsbeckett.ltitools.tool;

/**
 * A log entry for use in a stack of log entries.
 * 
 * @author jon
 */
public class ResourceEntry
{
  long timestamp;
  String person;
  String message;

  /**
   * Construct a log entry with a given name and message.
   * 
   * @param person Name of author of the message.
   * @param message The message itself.
   */
  public ResourceEntry( String person, String message )
  {
    this.timestamp = System.currentTimeMillis();
    this.person = person;
    this.message = message;
  }
  
  /**
   * When the log entry was created.
   * 
   * @return timestamp as a long
   */
  public long getTimestamp()
  {
    return timestamp;
  }

  /**
   * Get the name of author of the message.
   * 
   * @return Name of author
   */
  public String getPerson()
  {
    return person;
  }

  /**
   * Get the text of the log entry.
   * 
   * @return The message.
   */
  public String getMessage()
  {
    return message;
  }
}
