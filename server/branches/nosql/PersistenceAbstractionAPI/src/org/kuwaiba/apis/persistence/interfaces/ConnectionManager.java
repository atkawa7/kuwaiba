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

package org.kuwaiba.apis.persistence.interfaces;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public interface ConnectionManager<T,S> {

    public void openConnection();
    public void closeConnection();
    public S startTransaction();
    public void commitTransaction(S tx);
    public void rollbackTransaction();
    public ConnectionManager spawnConnection();
    public boolean isSpawned();
    public List<ConnectionManager> getConnectionPool();
    public T getConnectionHandler();

}
