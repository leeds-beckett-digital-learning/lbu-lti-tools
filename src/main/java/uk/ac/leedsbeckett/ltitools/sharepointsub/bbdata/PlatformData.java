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
package uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata;

import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.leedsbeckett.jesharepoint.Sharepoint;
import uk.ac.leedsbeckett.jesharepoint.SharepointSettings;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Deadline;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.DeadlineUsage;

/**
 *
 * @author maber01
 */
public class PlatformData
{
  public ArrayList<ModuleData> modules = new ArrayList<>();
  public HashMap<String,SitePerson> sitepersonmapbybbid = new HashMap<>();
  public HashMap<String,SitePerson> sitepersonmapbyemail = new HashMap<>();
  public SharepointSettings sharepointSettings;
  public Sharepoint sp;
  
  public HashMap<Long,DeadlineUsage> deadlineUsageMap = new HashMap<>();
    
  public void addDeadlineUse( Long deadline, String course, String dropbox )
  {
    CourseDropboxNames cdn = new CourseDropboxNames( course, dropbox );
    DeadlineUsage du = deadlineUsageMap.get( deadline );
    if ( du == null )
    {
      du = new DeadlineUsage();
      deadlineUsageMap.put( deadline, du );
    }
    du.add( cdn );
  }
}
