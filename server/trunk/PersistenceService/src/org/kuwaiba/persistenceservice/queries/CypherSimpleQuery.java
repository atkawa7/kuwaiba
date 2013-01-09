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

package org.kuwaiba.persistenceservice.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CypherSimpleQuery {

    public void setClassNodes(Map<String, Node> nodesFromQuery) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void createQuery(ExtendedQuery query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<ResultRecord> getResultList() {
        return new ArrayList<ResultRecord>();
    }

}
