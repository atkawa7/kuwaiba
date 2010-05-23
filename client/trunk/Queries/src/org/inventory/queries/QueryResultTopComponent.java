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
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectChildren;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows the result of the current query
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class QueryResultTopComponent extends TopComponent implements Provider{

    private ExplorerManager em = new ExplorerManager();
    private ListView lv;

    QueryResultTopComponent(LocalObjectLight[] found,String title) {
        lv = new ListView();
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(em));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(em));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(em));

        //Now the keystrokes (doesn't seem to be working)
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        keys.put(KeyStroke.getKeyStroke("control C"), DefaultEditorKit.copyAction);
        keys.put(KeyStroke.getKeyStroke("control X"), DefaultEditorKit.cutAction);
        keys.put(KeyStroke.getKeyStroke("control V"), DefaultEditorKit.pasteAction);

        associateLookup(ExplorerUtils.createLookup(em, map));

        em.setRootContext(new AbstractNode(new ObjectChildren(found)));
        setDisplayName("Search results for "+ title);

        setLayout(new BorderLayout());
        this.add(lv,BorderLayout.CENTER);

        Mode myMode = WindowManager.getDefault().findMode("explorer");
        myMode.dockInto(this);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public ExplorerManager getExplorerManager() {
        return em;
    }
}
