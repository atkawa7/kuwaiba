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
 */package core.toserialize;

import entity.core.RootObject;
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
    private String displayName; 
    protected Long oid;
    protected String className;
    protected String packageName;
    

    public RemoteObjectLight(){} 

    public RemoteObjectLight(Object obj){
        this.className = obj.getClass().getSimpleName();
        this.packageName = obj.getClass().getPackage().getName();
        //TODO: It should be possible to the user to change the sisplay name using a customization tool
        this.displayName = ((RootObject)obj).getName();
        this.oid = ((RootObject)obj).getId();
    }

    public String getClassName() {
        return className;
    }

    //Shouldn't be inherit because displayName is private
    public final String getDisplayName() {
        return displayName;
    }

    public Long getOid() {
        return oid;
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