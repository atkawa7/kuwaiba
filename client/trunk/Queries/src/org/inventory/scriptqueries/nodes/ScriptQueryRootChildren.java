/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
 */
package org.inventory.scriptqueries.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalScriptQuery;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.scriptqueries.ScriptQueriesManagerService;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ScriptQueryRootChildren extends Children.Keys<LocalScriptQuery>  {
    
    @Override
    public void addNotify() {
        List<LocalScriptQuery> scriptQueries = CommunicationsStub.getInstance().getScriptQueries();
        if (scriptQueries == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            setKeys(Collections.EMPTY_SET);
        } else {
            Collections.sort(scriptQueries);
            setKeys(scriptQueries);
        }
    }
    
    @Override
    public void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
    
    @Override
    protected Node[] createNodes(LocalScriptQuery t) {
        t.addChangeListener(ScriptQueriesManagerService.getInstance());
        return new Node[] { new ScriptQueryNode(t)};
    }
    
    @Override
    protected void destroyNodes(Node[] nodes) {
        for (Node node : nodes)
            node.getLookup().lookup(LocalScriptQuery.class).removeChangeListener(ScriptQueriesManagerService.getInstance());
    }
}
