/*
 * Copyright 2022 maber01.
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
package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author maber01
 */
public class PeerGroupForm implements Serializable
{
  String id;
  String name;
  ArrayList<String> fieldIds;
  HashMap<String,Field> fields;

  public String getId()
  {
    return id;
  }

  public void setId( String id )
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public ArrayList<String> getFieldIds()
  {
    return fieldIds;
  }

  public void setFieldIds( ArrayList<String> fieldIds )
  {
    this.fieldIds = fieldIds;
  }

  public HashMap<String, Field> getFields()
  {
    return fields;
  }

  public void setFields( HashMap<String, Field> fields )
  {
    this.fields = fields;
  }
  
  
  
  public static class Field implements Serializable
  {
    String id;
    String description;
    FieldType type;
    int minimum;
    int maximum;

    public String getId()
    {
      return id;
    }

    public void setId( String id )
    {
      this.id = id;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription( String description )
    {
      this.description = description;
    }

    public FieldType getType()
    {
      return type;
    }

    public void setType( FieldType type )
    {
      this.type = type;
    }

    public int getMinimum()
    {
      return minimum;
    }

    public void setMinimum( int minimum )
    {
      this.minimum = minimum;
    }

    public int getMaximum()
    {
      return maximum;
    }

    public void setMaximum( int maximum )
    {
      this.maximum = maximum;
    }
    
  }
  
  public enum FieldType
  {
    INTEGER,
    FLOAT
  }
}
