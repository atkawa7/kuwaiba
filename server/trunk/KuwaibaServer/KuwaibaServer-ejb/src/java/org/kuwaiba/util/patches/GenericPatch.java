/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.kuwaiba.util.patches;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * All patches to update the database model must inherit from this class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class GenericPatch implements Serializable {
    /**
     * A unique identifier of to the patch
     * @return The Patch Id
     */
    public abstract String getId();
    /**
     * A very compact description of what the patch is about
     * @return A title
     */
    public abstract String getTitle();
    /**
     * A more detailed description of what the patch does and what changes will be made
     * @return A description
     */
    public abstract String getDescription();
    /**
     * The version this patch is supposed to migrate/modify information from
     * @return A source description
     */
    public abstract String getSourceVersion();
    /**
     * The version this patch is supposed to migrate the old structure/information to
     * @return The target version
     */
    public abstract String getTargetVersion();
    /**
     * The actual patch logic
     * @return The result of the execution
     */
    public abstract PatchResult executePatch();
    /**
     * Defines if a patch is mandatory
     * @return True if the patch is mandatory
     */
    public abstract String getMandatory();
    
    public class PatchResult {
        /**
         * The patch was applied successfully
         */
        public static final int RESULT_SUCCESS = 1;
        /**
         * The process presented a few problems and might not have finished successfully
         */
        public static final int RESULT_WARNING = 2;
        /**
         * There was a fatal error and the patch could not be applied
         */
        public static final int RESULT_ERROR = 3;
        /**
         * Type of result. See RESULT_XXX for possible values
         */
        private int resultType;
        /**
         * The list of messages generated during the execution 
         */
        private List<String> messages;
        
        public PatchResult() {
            this.messages = new ArrayList<>();
        }

        public int getResultType() {
            return resultType;
        }

        public void setResultType(int resultType) {
            this.resultType = resultType;
        }

        public List<String> getMessages() {
            return messages;
        }

        public void setMessages(List<String> messages) {
            this.messages = messages;
        }
    }
}
