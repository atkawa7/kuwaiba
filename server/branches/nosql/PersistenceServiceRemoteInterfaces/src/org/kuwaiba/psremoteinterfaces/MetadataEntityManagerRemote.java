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

package org.kuwaiba.psremoteinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.kuwaiba.apis.persistence.AttributeMetadata;
import org.kuwaiba.apis.persistence.ClassMetadata;
import org.kuwaiba.apis.persistence.ClassMetadataLight;

/**
 * RMI wrapper for the MetadataEntityManager interface
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface MetadataEntityManagerRemote extends Remote{

    public void createDb() throws RemoteException;
    public Long createRoot() throws RemoteException;
    public Long createClass(ClassMetadata classDefinition) throws RemoteException;
    public ClassMetadata getClass(String className) throws RemoteException;
    public ClassMetadata getClass(Long classId) throws RemoteException;
    public boolean addAttribute(String className, AttributeMetadata attributeDefinition) throws RemoteException;
    public boolean  deleteAttribute(String className, String attributeName) throws RemoteException;
    public boolean deleteClass(String className) throws RemoteException;
    public boolean deleteClass(Long classId) throws RemoteException;
}
