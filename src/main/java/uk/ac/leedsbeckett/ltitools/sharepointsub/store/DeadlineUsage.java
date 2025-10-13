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
package uk.ac.leedsbeckett.ltitools.sharepointsub.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.CourseDropboxNames;

/**
 *
 * @author maber01
 */
public class DeadlineUsage
{
  HashSet<CourseDropboxNames> courseDropboxes = new HashSet<>();
  
  public void add( CourseDropboxNames cD )
  {
    if ( !courseDropboxes.contains( cD ) )
      courseDropboxes.add( cD );
  }
  
  public Collection<CourseDropboxNames> getAll()
  {
    return courseDropboxes;
  }
}
