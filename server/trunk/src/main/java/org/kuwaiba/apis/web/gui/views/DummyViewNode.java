/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.web.gui.views;

import java.util.Objects;

/**
 * A node that does not represent an inventory object in itself. It is useful for those views that display nodes that represent external, 
 * decorative or temporal elements, such as clouds, boxes or frames.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DummyViewNode extends AbstractViewNode<DummyViewNode.DummyBusinessObject>{

    public DummyViewNode(DummyViewNode.DummyBusinessObject identifier) {
        super(identifier);
    }

    /**
     * A dummy object used to create DummyViewNode instances. It has only a String id and a display name
     */
    public class DummyBusinessObject {
        /**
         * The unique id of the dummy object.
         */
        private String id;
        /**
         * The display name of the dummy object.
         */
        private String displayName;

        public DummyBusinessObject(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DummyBusinessObject))
                return false;
            
            return id.equals(((DummyBusinessObject)obj).id);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.id);
            return hash;
        }
    }
}
