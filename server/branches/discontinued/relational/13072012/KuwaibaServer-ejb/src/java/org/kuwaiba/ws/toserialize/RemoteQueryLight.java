/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.kuwaiba.ws.toserialize;

import org.kuwaiba.entity.queries.Query;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Simple version of RemoteQuery
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteQueryLight extends RemoteObjectLight{
    private String description;
    private String name;
    private boolean isPublic;

    public RemoteQueryLight() {
    }

    public RemoteQueryLight(Long id, String name, String description, boolean isPublic) {
        this.oid = id;
        this.name = name;
        this.description = description;
        this.className = "Query"; //NOI18N
        this.isPublic = isPublic;
    }

    public RemoteQueryLight(Query query) {
        this.oid = query.getId();
        this.description = query.getDescription();
        this.className = "Query"; //NOI18N
        this.isPublic = (query.getOwner() == null);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
