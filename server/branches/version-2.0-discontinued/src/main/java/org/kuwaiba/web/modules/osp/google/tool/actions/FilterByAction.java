/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.google.tool.actions;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.OptionGroup;
import de.steinwedel.messagebox.MessageBox;
import java.util.ArrayList;
import java.util.Collection;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.web.modules.osp.google.CustomGoogleMap;
import org.kuwaiba.web.modules.osp.google.GoogleMapWrapper;

/**
 *
 *  @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FilterByAction extends AbstractAction {
    private final OptionGroup ogConnectionsFilter;
    private final OptionGroup ogNodesFilter;
    private final FormLayout form;
    private boolean firstCall = true;
    
    public FilterByAction(String caption, String resourceId) {
        super(caption, new ThemeResource(resourceId));
        form = new FormLayout();
        
        ogConnectionsFilter = new OptionGroup("Connections Filter by:");
        ogConnectionsFilter.setMultiSelect(true);
        
        form.addComponent(ogConnectionsFilter);
        
        ogNodesFilter = new OptionGroup("Nodes Filter by:");
        ogNodesFilter.setMultiSelect(true);
        
        form.addComponent(ogNodesFilter);
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        CustomGoogleMap map = ((GoogleMapWrapper) targetObject).getMap();
                
        Collection<String> selectedConnFilter = (Collection<String>) ogConnectionsFilter.getValue();
        ogConnectionsFilter.addItems(map.getConnectionsFilter());
        
        if (firstCall) {
            
            for (String filter : map.getConnectionsFilter())
                ogConnectionsFilter.select(filter);
        } else {
            
            for (String filter : map.getConnectionsFilter()) {
                if (selectedConnFilter.contains(filter))
                    ogConnectionsFilter.select(filter);
            }
        }
        
        Collection<String> selectedNodeFilter = (Collection<String>) ogNodesFilter.getValue();
        ogNodesFilter.addItems(map.getNodeFilter());
        
        if (firstCall) {
            for (String filter : map.getNodeFilter())
                ogNodesFilter.select(filter);
        } else {
            
            for (String filter : map.getNodeFilter()) {
                if (selectedNodeFilter.contains(filter))
                    ogNodesFilter.select(filter);
            }
        }
        
        if (firstCall)
            firstCall = false;
        
        MessageBox mbFilter = MessageBox
            .createQuestion()
            .withMessage(form)
            .withOkButton(() -> {
                map.filterby(
                    new ArrayList((Collection<String>) ogNodesFilter.getValue()), 
                    new ArrayList((Collection<String>) ogConnectionsFilter.getValue()));
            })
            .withCancelButton();
        
        mbFilter.open();
    }
    
}
