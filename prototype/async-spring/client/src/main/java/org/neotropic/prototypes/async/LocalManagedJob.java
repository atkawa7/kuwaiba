/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.prototypes.async;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * The local representation of the remote ManagedJob, which is a class that contains 
 * information about a given job (id, progress, state and a possible list of messages associated 
 * with the execution )
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalManagedJob {
    /**
    * The job ended successfully. 
    */
   public static final int STATE_END_SUCCESS = 1;
   /**
    * The job is in progress. 
    */
   public static final int STATE_RUNNING = 2;
   /**
    * The job is stopped. 
    */
   public static final int STATE_STOPPED = 3;
   /**
    * The job ended with error. 
    */
   public static final int STATE_END_ERROR = 4;
   /**
    * The job stopped killed by the user. 
    */
   public static final int STATE_END_KILLED = 5;
   /**
    * UUID to uniquely identify the job. This id will be used by the consumers of the service to 
    * keep track of the execution of each job polling constantly to inquire for the current state.
    */
   public String id;
   /**
    * The message associated to the requested descriptor. The idea behind this field, 
    * is that relevant messages are stacked here as each thread runs to indicate status changes or long error/information messages.
    */
   private List<String> messages;
   /**
    * Type of result. See STATE_XX for possible values.
    */
   private int state;
   /**
    * Current job progress as a percentage.
    */
   private float progress;
   
   public String getId() {
       return this.id;
   }

   public List<String> getMessages() {
       return this.messages;
   }

   public void setMessages(List<String> messages) {
       this.messages = messages;
   }

   public int getState() {
       return this.state;
   }

   public void setState(int state) {
       this.state = state;
   }

   public float getProgress() {
       return progress;
   }

   public void setProgress(float progress) {
       this.progress = progress;
   }

   @Override
   public String toString() {
       return this.id;
   }
}
