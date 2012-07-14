/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

import java.util.List;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.queries.LocalResultRecord;
import org.openide.util.lookup.ServiceProvider;

/**
 * A simple wrapper class representing locally the a query result record. This is basically a
 * LocalObjectLight and a variable number of extra columns
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalResultRecord.class)
public class LocalResultRecordImpl implements LocalResultRecord{
    private LocalObjectLight object;
    private List<String> extraColumns;

    public LocalResultRecordImpl(){}

    public LocalResultRecordImpl(LocalObjectLight object, List<String> extraColumns) {
        this.object = object;
        this.extraColumns = extraColumns;
    }

    public List<String> getExtraColumns() {
        return extraColumns;
    }

    public LocalObjectLight getObject() {
        return object;
    }
}
