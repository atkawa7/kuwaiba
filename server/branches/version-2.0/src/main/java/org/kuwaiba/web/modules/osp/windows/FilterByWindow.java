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
package org.kuwaiba.web.modules.osp.windows;

import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.kuwaiba.apis.web.gui.windows.MessageDialogWindow;

/**
 * Window to select the elements that will be shown in the map
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FilterByWindow extends MessageDialogWindow {
    private OptionGroup optGrpConnectionsFilter;
    private OptionGroup optGrpNodesFilter;
            
    public FilterByWindow(Window.CloseListener closeListener) {
        super(closeListener, "", MessageDialogWindow.OK_CANCEL_OPTION);
    }
    
    public void setNodesFilter(List<String> nodesList) {
        optGrpNodesFilter.removeAllItems();
        optGrpNodesFilter.addItems(nodesList);
    }
    
    public void setConnectionFilter(List<String> connectionsList) {
        optGrpConnectionsFilter.removeAllItems();
        optGrpConnectionsFilter.addItems(connectionsList);
    }    
    
    public List<String> getSeletedNodesFilter() {
        return new ArrayList((Collection<String>) optGrpNodesFilter.getValue());
    }
    
    public List<String> getSelectedConnectionsFilter() {
        return new ArrayList((Collection<String>) optGrpConnectionsFilter.getValue());
    }

    @Override
    public Component initSimpleMainComponent() {
        return null;
    }

    @Override
    public void initComplexMainComponent() {
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        
        optGrpConnectionsFilter = new OptionGroup("Connections Filter by:");
        optGrpConnectionsFilter.setMultiSelect(true);
        
        content.addComponent(optGrpConnectionsFilter);
        
        optGrpNodesFilter = new OptionGroup("Nodes Filter by:");
        optGrpNodesFilter.setMultiSelect(true);
                
        content.addComponent(optGrpNodesFilter);
        
        setMainComponent(content);
    }
}
