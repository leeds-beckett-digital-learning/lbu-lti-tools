/*
 * Copyright 2025 maber01.
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
package uk.ac.leedsbeckett.ltitools.hugeupload.messagedata;

import java.util.ArrayList;

/**
 *
 * @author maber01
 */
public class HuFileMap
{
  String name;
  long lastModified;
  long size;
  String type;
  ArrayList<HuFileMapChunk> map;

  public String getName()
  {
    return name;
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public long getLastModified()
  {
    return lastModified;
  }

  public void setLastModified( long lastModified )
  {
    this.lastModified = lastModified;
  }

  public long getSize()
  {
    return size;
  }

  public void setSize( long size )
  {
    this.size = size;
  }

  public String getType()
  {
    return type;
  }

  public void setType( String type )
  {
    this.type = type;
  }

  public ArrayList<HuFileMapChunk> getMap()
  {
    return map;
  }

  public void setMap( ArrayList<HuFileMapChunk> map )
  {
    this.map = map;
  }
  
}
