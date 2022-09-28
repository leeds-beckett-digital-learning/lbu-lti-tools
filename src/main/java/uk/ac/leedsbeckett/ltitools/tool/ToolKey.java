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
package uk.ac.leedsbeckett.ltitools.tool;

import java.io.Serializable;
import uk.ac.leedsbeckett.ltitools.tool.annotations.LtiTool;

/**
 *
 * @author maber01
 */
public class ToolKey implements Serializable
{
    final String type;
    final String name;

    public ToolKey( String type, String name )
    {
      assert( type != null && name != null );
      this.type = type;
      this.name = name;
    }

    public ToolKey( LtiTool ltiTool )
    {
      this( ltiTool.type(), ltiTool.name() );
    }

    @Override
    public int hashCode()
    {
      return type.hashCode() | name.hashCode();
    }

    @Override
    public String toString()
    {
      return type + " " + name;
    }

    @Override
    public boolean equals( Object obj )
    {
      if ( !(obj instanceof ToolKey) )
        return false;
      ToolKey other = (ToolKey)obj;
      return this.name.equals( other.name ) && this.type.equals( other.type );
    }
    
  
}
