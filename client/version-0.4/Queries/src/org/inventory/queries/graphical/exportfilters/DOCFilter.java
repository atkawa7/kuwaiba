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

package org.inventory.queries.graphical.exportfilters;

/**
 * Exports to Microsoft Word .doc format
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class DOCFilter implements ExportFilter{

    public String getDisplayName() {
        return "DOC - Microsoft Word Document";
    }

    public String getExtension() {
        return ".doc"; //NOI18N
    }

    public boolean export(Object[][] result, String fileName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
