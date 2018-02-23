/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.ws.toserialize.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


/**
 * This class represents a list of RemoteObjectLight instances. It's basically a wrapper for ArrayList&lt;RemoteObjectLight&gt; 
 * It's used only to improve code readability. This class extended from ArrayList of RemoteObjectLight at the beginning, but JAX-WS just refused to treat it as a List
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObjectLightList implements  Serializable {
    private List<RemoteObjectLight> list;

    public RemoteObjectLightList() {
        list = new ArrayList<>();
    }
    
    public RemoteObjectLightList(List<RemoteObjectLight> list) {
        this.list = list;
    }
    
    public boolean add(RemoteObjectLight element) {
        return list.add(element);
    }
    
    public boolean addAll(Collection<? extends RemoteObjectLight> elements) {
        return list.addAll(elements);
    }
    
    public boolean remove(RemoteObjectLight element) {
        return list.remove(element);
    }
}