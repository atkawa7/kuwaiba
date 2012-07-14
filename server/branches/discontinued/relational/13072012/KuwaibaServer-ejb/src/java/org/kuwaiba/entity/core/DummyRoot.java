/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.kuwaiba.entity.core;

import org.kuwaiba.core.annotations.Dummy;
import javax.persistence.Entity;

/**
 * This is a dummy class used *only* to hold container information about the root node. Besides
 * it's different an object with no parent (attribute parent = null) and an object with the root
 * object as parent (attribute parent = dummy_root). This class should have ONLY one instance
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Dummy
public class DummyRoot extends RootObject{
    @Override
    public String toString() {
        return "DummyRoot"; //NOI18N
    }

}
