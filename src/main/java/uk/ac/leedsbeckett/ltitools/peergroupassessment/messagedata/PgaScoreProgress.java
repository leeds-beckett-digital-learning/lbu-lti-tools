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

/**
 *
 * @author maber01
 */
public class PgaScoreProgress
{
  private final int percentage;

  public PgaScoreProgress( @JsonProperty("percentage") int percentage )
  {
    this.percentage = percentage;
  }

  public int getPercentage()
  {
    return percentage;
  }
}
