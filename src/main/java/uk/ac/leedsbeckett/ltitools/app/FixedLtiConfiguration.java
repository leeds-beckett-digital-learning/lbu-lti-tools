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

package uk.ac.leedsbeckett.ltitools.app;

/**
 * Some properties that are fixed but may in the future be shifted into
 * the configuration file.
 * 
 * @author jon
 */
public class FixedLtiConfiguration
{
  /**
   * The URL where the LTI login servlet is accessed.
   */
  public static final String LOGIN_PATTERN  = "/login";
  
  /**
   * The URL where the LTI launch servlet is accessed.
   */
  public static final String LAUNCH_PATTERN = "/launch";
}
