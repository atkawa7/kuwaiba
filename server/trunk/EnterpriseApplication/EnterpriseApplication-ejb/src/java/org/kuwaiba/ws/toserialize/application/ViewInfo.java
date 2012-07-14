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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.View;
import org.kuwaiba.util.Constants;

/**
 * This is a wrapper class for the entity class View (see Persistence Abstraction Layer API docs for details). It's the object returned
 * when a view is requested
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ViewInfo{
    
    private Long id;
    private int type;
    private byte[] background;
    private byte[] structure;

    /**
     * Required by the serializer.
     */
    public ViewInfo(){}

    public ViewInfo(View myView) {
        this.id = myView.getId();
        this.type = myView.getViewType();
        this.structure = myView.getStructure();       
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getBackground() {
        return background;
    }

    public void setBackground(byte[] background) {
        this.background = background;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getStructure() {
        return structure;
    }

    public void setStructure(byte[] structure) {
        this.structure = structure;
    }
}