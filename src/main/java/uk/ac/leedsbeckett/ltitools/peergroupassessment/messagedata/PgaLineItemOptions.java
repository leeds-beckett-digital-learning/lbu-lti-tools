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
package uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

/**
 *
 * @author maber01
 */
public class PgaLineItemOptions
{
  private final String suffix;
  private final boolean[] lineItemIncluded;

  public PgaLineItemOptions( 
          @JsonProperty("suffix")           String    suffix,
          @JsonProperty("lineItemIncluded") boolean[] lineItemIncluded )
  {
    this.suffix = suffix;
    this.lineItemIncluded = Arrays.copyOf( lineItemIncluded, lineItemIncluded.length );
  }

  public String getSuffix()
  {
    return suffix;
  }

  public boolean[] getLineItemIncluded()
  {
    return lineItemIncluded;
  }
}
