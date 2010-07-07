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

import entity.core.ConfigurationItem;
import entity.multiple.views.ObjectView;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This is a wrapper class for the entity class ObjectView. It's the object returned
 * when a view is requested
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class View {
    private List<RemoteObjectLight> elements;
    private byte[] background;
    private byte[] svgFile;

    /**
     * Required by the serializer
     */
    public View(){}

    /**
     * Builds a serialized version of the view using directly the entity
     * @param objectView The entity to be serialized
     */
    public View(ObjectView objectView){
        elements = new ArrayList<RemoteObjectLight>();
        for (ConfigurationItem ci : objectView.getElements())
            elements.add(new RemoteObjectLight(ci));
        this.background = objectView.getBackgroundImage();
    }

    public List<RemoteObjectLight> getElements() {
        return elements;
    }

    public void setElements(List<RemoteObjectLight> elements) {
        this.elements = elements;
    }

    public byte[] getBackground() {
        return background;
    }

    public void setBackground(byte[] background) {
        this.background = background;
    }

    public byte[] getSvgFile() {
        return svgFile;
    }

    public void setSvgFile(byte[] svgFile) {
        this.svgFile = svgFile;
    }
}