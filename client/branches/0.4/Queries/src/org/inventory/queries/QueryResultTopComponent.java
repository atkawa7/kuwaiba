/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
 * 
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.inventory.queries;

import java.awt.BorderLayout;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectChildren;
import org.inventory.navigation.applicationnodes.objectnodes.RootObjectNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows the result of the current query
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class QueryResultTopComponent extends TopComponent{
    private ExplorerManager em = new ExplorerManager();
    private ListView lv;

    QueryResultTopComponent(LocalObjectLight[] found,String title) {
        lv = new ListView();
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(em));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(em));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(em));


        associateLookup(ExplorerUtils.createLookup(em, map));

        //For some weird reason NBP requires the Nodes API to be a dependency for this module
        //but shouldn't, since I'm using RootObjectNode, ObjectChildren which are located in the
        //ApplicationNodes module which in turn depends on the Nodes API
        em.setRootContext(new RootObjectNode(new ObjectChildren(found)));
        setDisplayName("Search results for "+ title);

        setLayout(new BorderLayout());
        this.add(lv,BorderLayout.CENTER);

        Mode myMode = WindowManager.getDefault().findMode("explorer"); //NOI18N
        myMode.dockInto(this);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
}
