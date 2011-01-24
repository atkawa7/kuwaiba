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

package org.inventory.communications.core;

import java.util.List;
import org.inventory.core.services.interfaces.LocalObjectLight;

/**
 * A simple wrapper class representing locally the a query result record. This is basically a
 * LocalObjectLight and a variable number of extra columns
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalResultRecord {
    private LocalObjectLight object;
    private List<String> extraColumns;

    public LocalResultRecord(LocalObjectLight object, List<String> extraColumns) {
        this.object = object;
        this.extraColumns = extraColumns;
    }

    public List<String> getExtraColumns() {
        return extraColumns;
    }

    public LocalObjectLight getObject() {
        return object;
    }

    public static Object[][] toMatrix(LocalResultRecord[] results){
        if (results == null)
            return null;
        if (results.length == 0)
            return new Object[0][0];

        Object[][] asMatrix =new Object[results.length][results[0].getExtraColumns().size() + 1];
        for (int i = 0; i < results.length; i++){
            asMatrix[i][0] = results[i].getObject();
            for (int j = 0; j < results[i].getExtraColumns().size();j++)
                asMatrix[i][j + 1] = results[i].getExtraColumns().get(j);
        }
        
        return asMatrix;
    }
}
