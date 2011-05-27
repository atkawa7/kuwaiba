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

import org.kuwaiba.core.annotations.NoCopy;
import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.entity.views.GenericView;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

/**
 * Subclasses of this class have views
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class ViewableObject extends InventoryObject {
    @OneToMany(cascade=CascadeType.ALL)//(mappedBy = "elements")
    @NoSerialize
    @NoCopy
    protected List<GenericView> views;

    public List<GenericView> getViews() {
        return views;
    }

    public void setViews(List<GenericView> views) {
        this.views = views;
    }

    public void addView(GenericView view) {
        if (this.views == null)
            views = new ArrayList<GenericView>();
        this.views.add(view);
    }
}
