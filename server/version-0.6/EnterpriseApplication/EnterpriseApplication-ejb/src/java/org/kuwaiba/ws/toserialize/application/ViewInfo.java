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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.ViewObject;

/**
 * This is a wrapper class for the entity class ViewObject (see Persistence Abstraction Layer API docs for details). It's the object returned
 * when a view is requested
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ViewInfo extends ViewInfoLight {
    
    private byte[] background;
    private byte[] structure;

    public ViewInfo() {
    }

    public ViewInfo(ViewObject myView) {
        super(myView);
        this.structure = myView.getStructure();
    }

    public byte[] getBackground() {
        return background;
    }

    public void setBackground(byte[] background) {
        this.background = background;
    }

    public byte[] getStructure() {
        return structure;
    }

    public void setStructure(byte[] structure) {
        this.structure = structure;
    }
}