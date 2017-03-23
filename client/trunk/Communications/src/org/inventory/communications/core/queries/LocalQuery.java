/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.communications.core.queries;

import org.inventory.communications.wsclient.RemoteQuery;

/**
 * This is the local representation of an entity.queries.Query with the information necessary to store it
 * <b>Note:</b> This query is used ONLY for storing purposes (when an user creates/saves a query).
 * For queries to be executed only see TransientQuery
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalQuery extends LocalQueryLight {
    private byte[] structure;

    public LocalQuery(Long id, String name, String description, boolean isPublic, byte[] structure){
        super(id, name, description, isPublic);
        this.structure = structure;

    }
    public LocalQuery(RemoteQuery remoteQuery) {
        super(remoteQuery);
        this.structure = remoteQuery.getContent();
    }

    public byte[] getStructure() {
        return structure;
    }

    public void setStructure(byte[] structure) {
        this.structure = structure;
    }
}
