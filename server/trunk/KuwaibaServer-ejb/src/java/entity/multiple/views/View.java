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

package entity.multiple.views;

import core.annotations.Administrative;
import entity.core.AdministrativeItem;
import entity.core.ConfigurationItem;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * This class represents a view for a given element, this is, all the elements to
 * be rendered in order to have a graphical representation of the object
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Administrative
@Table(name="ElementViews") //"View" is a reserved keyword
public class View extends AdministrativeItem implements Serializable {
    
    protected byte[] svgFile; //The file containing how the elements should be rendered

    @ManyToMany
    protected List<ConfigurationItem> elements;

    public List<ConfigurationItem> getElements() {
        return elements;
    }

    public void setElements(List<ConfigurationItem> elements) {
        this.elements = elements;
    }

    public byte[] getSvgFile() {
        return svgFile;
    }

    public void setSvgFile(byte[] svgFile) {
        this.svgFile = svgFile;
    }
}
