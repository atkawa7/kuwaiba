/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.usermanager.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.inventory.communications.core.LocalUserGroupObject;
import org.openide.nodes.Children.Array;
import org.openide.nodes.Node;

/**
 * Represents a the list of groups
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GroupChildren extends Array{
    private List<LocalUserGroupObject> list;

    public GroupChildren(LocalUserGroupObject[] _list){
        this.list = new ArrayList<LocalUserGroupObject>();
        this.list.addAll(Arrays.asList(_list));
    }

    @Override
    protected Collection<Node> initCollection(){
        List<Node> myNodes = new ArrayList<Node>();
        for (LocalUserGroupObject group: list)
            myNodes.add(new GroupNode(group));
        return myNodes;
    }

    public List<LocalUserGroupObject> getList(){
        return list;
    }
}
