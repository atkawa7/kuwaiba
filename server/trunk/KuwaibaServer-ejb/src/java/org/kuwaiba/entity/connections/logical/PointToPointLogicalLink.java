/**
 *  Copyright 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.entity.connections.logical;

import javax.persistence.Entity;
import org.kuwaiba.entity.connections.GenericConnection;

/**
 * Represents a point to point logical connection (this is, without hops, just the  two endpoints),
 * for example a STM-x or a simple IP conversation where the number of underlying equipment on the
 * route is not relevant (at least at layer 3 level)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class PointToPointLogicalLink extends GenericConnection {

}
