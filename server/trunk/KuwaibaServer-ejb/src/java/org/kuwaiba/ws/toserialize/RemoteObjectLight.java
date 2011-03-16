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
 */package org.kuwaiba.ws.toserialize;

import org.kuwaiba.entity.core.RootObject;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This class is a simple representation of an object. It's used for trees and view. This is jus an entity wrapper
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObjectLight {
    /**
     * The object's display name. It's private because a RmoteObject could provide his own display name with more information
     */
    protected Long oid;
    protected String className;
    private String displayName;
    
    /**
     * Misc flags used to give more information about the object
     */
    protected List<Validator> validators;

    /**
     * Default constructor. Not used
     */
    protected RemoteObjectLight(){}

    public RemoteObjectLight(Long oid, String className, String displayName) {
        this.displayName = displayName;
        this.oid = oid;
        this.className = className;
    }


    public RemoteObjectLight(Object obj){
        this.className = obj.getClass().getSimpleName();
        //TODO: It should be possible to the user to change the display name using a customization tool
        this.displayName = ((RootObject)obj).getName();
        this.oid = ((RootObject)obj).getId();
    }

    public String getClassName() {
        return className;
    }

    /**
     * Shouldn't be inherit because displayName is private
     */
    public final String getDisplayName() {
        return displayName;
    }

    public Long getOid() {
        return oid;
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


    /**
     * This method is useful to transform the returned value from queries (Entities)
     * into serialize RemoteObjectLight
     * @param objs objects to be transformed
     * @return an array with ROL
     */
    public static RemoteObjectLight[] toArray(List objs){
        RemoteObjectLight[] res = new RemoteObjectLight[objs.size()];
        int i=0;
        for (Object obj : objs){
            res[i] = new RemoteObjectLight(obj);
            i++;
        }
        return res;
    }
}