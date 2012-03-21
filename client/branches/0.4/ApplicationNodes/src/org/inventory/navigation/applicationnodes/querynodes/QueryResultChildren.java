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

package org.inventory.navigation.applicationnodes.querynodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.inventory.core.services.api.queries.LocalResultRecord;
import org.openide.nodes.Children.Array;
import org.openide.nodes.Node;

/**
 * Children for a query result list
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 * TODO: Wrong use of generics. The whole node class hierarchy should be reconsidered
 */
public class QueryResultChildren<T extends LocalResultRecord> extends Array{

    private ArrayList<T> keys;

    public QueryResultChildren(T[] res){
        keys = new ArrayList<T>();
        keys.addAll(Arrays.asList(res));
    }

    @Override
    protected Collection<Node> initCollection(){
        ArrayList<Node> myNodes = new ArrayList<Node>();
        for (T myObject : keys)
            myNodes.add(new QueryResultRecordNode(myObject));
        return myNodes;
    }
}