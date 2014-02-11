/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.services.api.export;

/**
 * All implementors of this interface are capable of providing results that can be exported to a
 * file using a ExportFilter
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface Exportable {
    public enum Range{
        ALL(0,"All"),
        CURRENT_PAGE(1,"Current Page");
        int id;
        String label;
        Range(int id, String label){
            this.id = id;
            this.label =label;
        }

        public int id(){return id;}
        public String label(){return label;}

        @Override
        public String toString(){
            return label;
        }
    };
    public Object[][] getResults(Range range);
}
