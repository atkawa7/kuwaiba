/**
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

package org.kuwaiba.ws;

import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.kuwaiba.beans.WebServiceBeanRemote;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;

/**
 * Main webservice
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@WebService()
public class Kuwaiba {
    @EJB
    private WebServiceBeanRemote wsBean;

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getMyMetadata")
    public String getMyMetadata() {
        return null;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getClass")
    public String getClass(@WebParam(name = "className")
    String className) throws Exception {
        ClassInfo aClass = wsBean.getClass(className);
        return aClass.getClassName();
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "createClass")
    public Long createClass(@WebParam(name = "ClassMetadata")
    String ClassMetadata) {
        //TODO write your implementation code here:
        return null;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "deleteClass")
    public Long deleteClass(@WebParam(name = "className")
    String className) {
        //TODO write your implementation code here:
        return null;
    }


}
