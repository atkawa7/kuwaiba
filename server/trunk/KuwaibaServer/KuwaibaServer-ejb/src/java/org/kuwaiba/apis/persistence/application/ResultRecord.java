/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.persistence.application;

import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import java.util.List;

/**
 * Represents a single record resulting from a query. It basically contains the very basic
 * information about an object, as well extra columns based on the "visibleAttributes" argument
 * provided when the query was executed
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ResultRecord extends RemoteBusinessObjectLight{
    private List<String> extraColumns;

    public ResultRecord(long id, String name, String className) {
        super(id,name, className);
    }

    public List<String> getExtraColumns() {
        return extraColumns;
    }

    public void setExtraColumns(List<String> extraColumns) {
        this.extraColumns = extraColumns;
    }
}
