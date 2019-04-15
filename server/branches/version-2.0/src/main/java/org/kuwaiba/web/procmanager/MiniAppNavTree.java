/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
 */
package org.kuwaiba.web.procmanager;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import java.util.Properties;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.procmanager.connections.ComponentConnectionTarget;

/**
 * Mini Application used to show the Physical Path View given the object Id and Class Name
 * @author Jalbersson Guillermo Plazas {@literal <jalbersson.plazas@kuwaiba.org>}
 */
public class MiniAppNavTree extends AbstractMiniApplication<Component, Component> {

    public MiniAppNavTree(Properties inputParameters) {
        super(inputParameters);
    }

    @Override
    public String getDescription() {
        return "Mini Application used to show a Navigation Tree Starting from the parameter";
    }

    @Override
    public Component launchDetached() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * the input parameters recieved for this mini App are:
     * startingId: the id of an object that is going to be the starting point of the Navigation Tree
     * name: the name of the Navigation Tree root object
     * className: the name of the Navigation Tree root object
     */
    @Override
    public Component launchEmbedded() {
        String id = getInputParameters().getProperty("startingId") != null ? getInputParameters().getProperty("startingId") : "-1";
        String rootName = getInputParameters().getProperty("name");
        String rootClassName = getInputParameters().getProperty("className");
        RemoteObjectLight root = new RemoteObjectLight(rootClassName, id, rootName);
        Panel pnlNavTree = new Panel();
        pnlNavTree.setSizeFull();
        pnlNavTree.setContent(new ComponentConnectionTarget(root, wsBean));
        return pnlNavTree;
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
    
}
