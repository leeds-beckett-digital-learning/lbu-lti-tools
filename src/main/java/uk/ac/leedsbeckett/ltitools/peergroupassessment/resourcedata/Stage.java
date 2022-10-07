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
package uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata;

import java.io.Serializable;

/**
 *
 * @author maber01
 */
public enum Stage implements Serializable
{
  /**
   * Instructor is setting up the resource and students must wait.
   */
  SETUP,

  /**
   * Students can join groups.
   */
  JOIN,

  /**
   * Students in groups can enter data.
   */
  DATAENTRY,

  /**
   * Data in the resource is frozen.
   */
  RESULTS  
}
