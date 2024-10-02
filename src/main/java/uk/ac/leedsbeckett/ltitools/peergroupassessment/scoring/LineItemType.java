/*
 * Copyright 2024 maber01.
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
package uk.ac.leedsbeckett.ltitools.peergroupassessment.scoring;


/**
 *
 * @author maber01
 */
public enum LineItemType
{
  SCORE(       "Score"     ),
  MAX_SCORE(   "Max Scr" ),
  REL_SCORE(   "Rel Scr" ),
  GROUP_SIZE(  "Grp Sz"  ),
  TOTAL_SCORE( "Tot Scr" ),
  MEAN_SCORE(  "Avg Scr" );
  
  private final String name;

  private LineItemType( String name )
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
