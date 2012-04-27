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
package org.kuwaiba.ws.toserialize.business;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.ws.toserialize.application.Validator;

/**
 * This class is a simple representation of an object. It's used for trees and view. This is jus an entity wrapper
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObjectLight {
    /**
     * Object's oid
     */
    private Long oid;
    /**
     * Object's name
     */
    private String name;
    /**
     * Object's class name
     */
    private String className;

    
    /**
     * Misc flags used to give more information about the object (i.e. is it already connected?)
     */
    protected List<Validator> validators;

    /**
     * Default constructor. Not used
     */
    protected RemoteObjectLight(){}

    public RemoteObjectLight(Long oid, String name, String className) {
        this.oid = oid;
        this.name = name;
        this.className = className;
    }


    public RemoteObjectLight(RemoteBusinessObjectLight obj){
        this.className = obj.getClassName();
        this.name = obj.getName();
        this.oid = obj.getId();
    }

    public String getClassName() {
        return className;
    }

    public Long getOid() {
        return oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Validators are flags indicating things about objects. Of course, every instance may have
     * something to expose or not. For instance, a port has an indicator to mark it as "connected physically",
     * but a Building (so far) has nothing to "indicate". This is done in order to avoid a second call to query
     * for a particular information that could affect the performance. I.e:
     * Call 1: getPort (retrieving a LocalObjectLight)
     * Call 2: isThisPortConnected (retrieving a boolean according to a condition)
     *
     * With this method there's only one call
     * getPort (a LocalObjectLight with a flag to indicate that the port is connected)
     *
     * Why not use getPort retrieving a LocalObject? Well, because the condition might be complicated, and
     * it's easier to compute its value at server side. Besides, it can involve complex queries that would require
     * more calls to the webservice
     */
    public List<Validator> getValidators() {
        return this.validators;
    }

    public void addValidator(Validator newValidator){
        if (this.validators == null)
            this.validators = new ArrayList<Validator>();
        this.validators.add(newValidator);
    }

    public final static RemoteObjectLight[] toRemoteObjectLightArray(List<RemoteBusinessObjectLight> toBeWrapped){
        if (toBeWrapped == null)
            return null;

        RemoteObjectLight[] res = new RemoteObjectLight[toBeWrapped.size()];
        for (int i = 0; i < toBeWrapped.size(); i++)
            res[i] = new RemoteObjectLight(toBeWrapped.get(i));

        return res;
    }
}