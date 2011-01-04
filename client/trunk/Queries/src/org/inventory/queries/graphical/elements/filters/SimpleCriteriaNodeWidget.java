/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.queries.graphical.elements.filters;

import org.inventory.queries.graphical.QueryEditorNodeWidget;
import org.inventory.queries.graphical.QueryEditorScene;
import org.netbeans.api.visual.vmd.VMDFactory;

/**
 * This class represents a simple searching criteria, this is, related to a simple data type.
 * Subclasses should provide proper filters depending on the data type (numeric, dates, booleans, etc)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public abstract class SimpleCriteriaNodeWidget extends QueryEditorNodeWidget{

    public SimpleCriteriaNodeWidget(QueryEditorScene scene) {
        super(scene,VMDFactory.getOriginalScheme ());
    }
    
    /**
     * Enumeration with all possible filters to be used within a criteria widget
     * @return The component used to filter
     */
    public abstract Object getValue();

    protected enum Criteria{
        EQUAL("Equal to",0),
        LESS_THAN("Less than",1),
        EQUAL_OR_LESS_THAN("Equals or less than",2),
        GREATER_THAN("Greater than",3),
        EQUAL_OR_GREATER_THAN("Equal or greater than",4),
        BETWEEN("Between",5),
        LIKE("Like",6);
        private final String label;
        private final int id;

        Criteria(String label, int id){
            this.label = label;
            this.id = id;
        }

        public String label(){return label;}
        public int id(){return id;}

        @Override
        public String toString(){return label;}
    }
}