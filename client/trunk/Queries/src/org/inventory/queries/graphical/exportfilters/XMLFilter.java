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
 * Exports to XML in a structure explained at the <a href="http://is.gd/kcl1a">project's wiki</a>
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class XMLFilter implements ExportFilter{

    public String getDisplayName() {
        return "XML - Markup Language";
    }

    public String getExtension() {
        return ".xml"; //NOI18N
    }

    public boolean export(Object[][] result, String fileName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
