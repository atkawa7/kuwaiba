/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.core.apis.integration;

/**
 * All components interested in being notified once an action is completed should implement this interface.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface ActionCompletedListener {
    /**
     * Notifies that a module action has been completed.
     * @param ev 
     */
    public void actionCompleted(ActionCompletedEvent ev);
    
    /**
     * This event is generated once a module action is completed.
     */
    public class ActionCompletedEvent {
        /**
         * If the action was completed successfully. 
         */
        public static int STATUS_SUCESS = 1;
        /**
         * If the action was completed with warnings. 
         */
        public static int STATUS_WARNING = 2;
        /**
         * If the action was not completed. 
         */
        public static int STATUS_ERROR = 3;
        /**
         * If the action was not completed because the user canceled it. 
         */
        public static int STATUS_CANCELED = 4;
        /**
         * Indicates what was the result of completing an action.
         */
        private int status;
        /**
         * Textual description of the result of the action.
         */
        private String message;
        /**
         * The action that generates the event;
         */
        private Class clazz;

        public ActionCompletedEvent(int status, String message, Class clazz) {
            this.status= status;
            this.message = message;
            this.clazz = clazz;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public Class getClazz() {
            return clazz;
        }

        public void setClazz(Class clazz) {
            this.clazz = clazz;
        }
    }
}
