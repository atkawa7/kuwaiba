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

package entity.views;

import entity.core.metamodel.ClassMetadata;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * This class represents the root for all those views that applies only to certain classes
 * for example, the views in racks or rooms
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class ClassView extends GenericView{
    @ManyToOne
    protected ClassMetadata myclass;

    public ClassMetadata getMyclass() {
        return myclass;
    }

    public void setMyclass(ClassMetadata myclass) {
        this.myclass = myclass;
    }
}
