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

package entity.core;

import entity.adapters.ObjectViewAdapter;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

/**
 * Subclasses of this class have views
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class ViewableObject extends RootObject {
    @ManyToMany(cascade=CascadeType.PERSIST)//(mappedBy = "elements")
    protected List<ObjectViewAdapter> views;

        public List<ObjectViewAdapter> getViews() {
        return views;
    }

    public void setViews(List<ObjectViewAdapter> views) {
        this.views = views;
    }
}
