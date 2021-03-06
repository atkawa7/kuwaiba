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

package org.inventory.core.usermanager.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.inventory.core.services.interfaces.LocalUserObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Represents the list of users
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class UserChildren extends Children.Array{

    private LocalUserObject[] list;

    public UserChildren(LocalUserObject[] _list){
        this.list = _list;
    }

    @Override
    protected Collection<Node> initCollection(){
        List<Node> myNodes = new ArrayList<Node>();
        for (LocalUserObject user: list)
            myNodes.add(new UserNode(user));
        return myNodes;
    }
}
