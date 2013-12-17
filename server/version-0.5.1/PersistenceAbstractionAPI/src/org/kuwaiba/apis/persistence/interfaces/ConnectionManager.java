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

import java.util.List;
import org.kuwaiba.apis.persistence.exceptions.ConnectionException;

/**
 * Interface providing the general methods to manage the db/backend connection
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public interface ConnectionManager<T> {
    public void openConnection() throws ConnectionException;
    public void closeConnection();
    public void startTransaction();
    public void commitTransaction();
    public void rollbackTransaction();
    public ConnectionManager spawnConnection();
    public boolean isSpawned();
    public List<ConnectionManager> getConnectionPool();
    public T getConnectionHandler();
    public void shutDown();
    public void printConnectionDetails();
}
