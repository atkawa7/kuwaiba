/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.ws.toserialize.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;

/**
 * This is a wrapper class for the entity class ViewObjectLight (see Persistence Abstraction Layer API docs for details). It's the object returned
 * when a view is requested
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ViewInfoLight implements Serializable {
    /**
     * View id
     */
    private long id;
    /**
     * View type
     */
    private int type;
    /**
     * View name
     */
    private String name;
    /**
     * View description
     */
    private String description;

    /**
     * Required by the serializer.
     */
    public ViewInfoLight(){}

    public ViewInfoLight(ViewObjectLight myView) {
        this.id = myView.getId();
        this.type = myView.getViewType();
        this.name = myView.getName();
        this.description = myView.getDescription();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}