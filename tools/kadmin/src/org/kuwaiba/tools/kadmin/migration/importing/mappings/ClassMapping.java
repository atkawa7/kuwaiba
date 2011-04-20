/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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
 * 
 */

package org.kuwaiba.tools.kadmin.migration.importing.mappings;

import java.util.List;

/**
 * Represents a mapped attribute, it indicates if a given attribute has been renamed, deleted,
 * or if it's type has been changed
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassMapping {
    private String originalName;
    private String renamedTo;
    private boolean removed;
    private boolean mapped;

    public boolean isMapped() {
        return mapped;
    }

    public void setMapped(boolean mapped) {
        this.mapped = mapped;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public String getRenamedTo() {
        return renamedTo;
    }

    public void setRenamedTo(String renamedTo) {
        this.renamedTo = renamedTo;
    }
}
