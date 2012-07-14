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
 * All filters must implement this interface
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface ExportFilter {
    /**
     * To be used as label in the formats combo box
     * @return the name to be displayed
     */
    public String getDisplayName();
    /**
     * Gets the file extension
     * @return the file extension (including the dot)
     */
    public String getExtension();
    /**
     * The export action
     * @return success or failure
     */
    public boolean export(Object[][] result, String fileName);
}
