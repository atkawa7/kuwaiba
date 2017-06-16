/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.kuwaiba.ws.toserialize.application;

import java.io.Serializable;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This is a record in a query result. It's composed of a RemoteObjectLight column
 * and a number of extra columns based on the visibleAttributes parameter provided in the
 * execute query call
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultRecord implements Serializable {

    /**
     * The main remote object light for this record
     */
    private RemoteObjectLight object;
    /**
     * The extra columns as strings. (i.e OwnerName, type, etc)
     */
    private ArrayList<String> extraColumns;
    
    //No-arg constructor required
    private ResultRecord() {    }

    public ResultRecord(RemoteObjectLight object, ArrayList<String> columns) {
        this.object = object;
        this.extraColumns = columns;
    }
}
