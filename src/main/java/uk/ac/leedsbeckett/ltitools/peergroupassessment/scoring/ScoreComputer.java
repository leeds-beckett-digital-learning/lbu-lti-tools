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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.leedsbeckett.lti.services.ags.data.LineItem;
import uk.ac.leedsbeckett.lti.services.ags.data.Score;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm.Field;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource.Group;
import static uk.ac.leedsbeckett.ltitools.peergroupassessment.scoring.LineItemType.MAX_SCORE;
import static uk.ac.leedsbeckett.ltitools.peergroupassessment.scoring.LineItemType.REL_SCORE;
import static uk.ac.leedsbeckett.ltitools.peergroupassessment.scoring.LineItemType.SCORE;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.store.StoreCluster;

/**
 *
 * @author maber01
 */
public class ScoreComputer
{
//  private String name;
//  private String resourceId;
//  private int maxgroupsize;
//  private int maxscore;
  
  private HashMap<LineItemType,LineItem> lineitemmap = new HashMap<>();
  
  public ScoreComputer( StoreCluster store, PeerGroupResource resource, String name, String resourceId )
  {
//    this.name = name;
//    this.resourceId = resourceId;
//    this.maxgroupsize = maxgroupsize;
//    this.maxscore = maxscore;

    PeerGroupForm form = store.getForm( resource.getFormId() );
    int maxscore = 0;
    for ( String fid : form.getFieldIds() )
    {
      Field f = form.getFields().get( fid );
      maxscore += f.getMaximum();
    }
    
    int maxgroupsize = 1;
    for ( String gid : resource.groupIdsInOrder )
    {
      Group g = resource.getGroupById( gid );
      int sz = g.getMembers().size();
      if ( sz > maxgroupsize ) maxgroupsize = sz;
    }
    
    
    for ( LineItemType type : LineItemType.values() )
    {
      BigDecimal max = null;
      switch ( type )
      {
        case SCORE:       max = new BigDecimal( maxscore ); break;
        case MAX_SCORE:   max = new BigDecimal( maxscore ); break;
        case REL_SCORE:   max = new BigDecimal( 100.0 ); break;
        case GROUP_SIZE:  max = new BigDecimal( maxgroupsize ); break;
        case TOTAL_SCORE: max = new BigDecimal( maxscore*maxgroupsize ); break;
        case MEAN_SCORE:  max = new BigDecimal( maxscore );
      }
      lineitemmap.put( type, 
              new LineItem(
                            null,
                            max,
                            type.getName() + " " + name,
                            resourceId,
                            false,
                            null,
                            null ) );
    }
  }
  
  public LineItem getLineItem( LineItemType type )
  {
    return lineitemmap.get( type );    
  }
  
  public void setLineItem( LineItemType type, LineItem li )
  {
    lineitemmap.put( type, li );
  }
  
  public Map<LineItemType,Score> getScores( String ltiid, int score, int grpmax, int total , int count )
  {
    HashMap<LineItemType,Score> currentscoremap = new HashMap<>();
    HashMap<LineItemType,BigDecimal> valmap     = new HashMap<>();

    BigDecimal s = new BigDecimal( score,  MathContext.UNLIMITED );
    BigDecimal x = new BigDecimal( grpmax,  MathContext.UNLIMITED );
    BigDecimal r = s.multiply( BigDecimal.valueOf( 100L ), MathContext.UNLIMITED ).divide( x, 2, RoundingMode.HALF_UP );
    BigDecimal z = new BigDecimal( count,  MathContext.UNLIMITED );
    BigDecimal t = new BigDecimal( total,  MathContext.UNLIMITED );
    BigDecimal a = t.divide( z, 2, RoundingMode.HALF_UP );
    
    valmap.put(  LineItemType.SCORE,        s );
    valmap.put(  LineItemType.MAX_SCORE,    x );
    valmap.put(  LineItemType.REL_SCORE,    r );
    valmap.put(  LineItemType.GROUP_SIZE,   z );
    valmap.put(  LineItemType.TOTAL_SCORE,  t );
    valmap.put(  LineItemType.MEAN_SCORE,   a );

    Instant ts = Instant.now();
    for ( LineItemType type : LineItemType.values() )
      currentscoremap.put( type, new Score( 
              ltiid,
              valmap.get( type ),
              lineitemmap.get( type ).getScoreMaximum(),
              null,
              ts,
              "Completed",
              "FullyGraded",
              null
                ) );
    return currentscoremap;
  }
}
