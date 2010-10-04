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

package core.toserialize;

import entity.views.GenericView;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This is a wrapper class for the entity class ObjectView. It's the object returned
 * when a view is requested
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ViewInfo {
    private byte[] background;
    private byte[] structure;
    private String description;
    /**
     * What class this view is instance of
     */
    private String viewClass;

    /**
     * Required by the serializer. This empty view is returned when there's no a defaultview associated to the object
     */
    public ViewInfo(){}

    /**
     * Builds a serialized version of the view using directly the entity
     * @param objectView The entity to be serialized
     */
    public ViewInfo(GenericView objectView){
        this.description = objectView.getDescription();
        this.viewClass = objectView.getClass().getName();
        this.background = objectView.getBackground();
        this.structure = objectView.getViewStructure();
    }

    public byte[] getBackground() {
        return background;
    }

    public void setBackground(byte[] background) {
        this.background = background;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getStructure() {
        return structure;
    }

    public void setStructure(byte[] structure) {
        this.structure = structure;
    }

    public String getViewClass() {
        return viewClass;
    }

    public void setViewClass(String viewClass) {
        this.viewClass = viewClass;
    }
}