/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.dashboard;

import java.awt.Component;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalTaskScheduleDescriptor;
import org.inventory.navigation.dashboard.widgets.AbstractWidget;
import org.inventory.navigation.dashboard.widgets.TaskResultWidget;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 * The Dashboard TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.navigation.dashboard//DashBoard//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "DashBoardTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "org.inventory.navigation.dashboard.DashBoardTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DashBoardAction",
        preferredID = "DashBoardTopComponent"
)
@Messages({
    "CTL_DashBoardAction=DashBoard",
    "CTL_DashBoardTopComponent=DashBoard",
    "HINT_DashBoardTopComponent=All the relevant information in a single place"
})
public final class DashBoardTopComponent extends TopComponent {
    private boolean loaded = false;
    public DashBoardTopComponent() {
        initComponents();
        setName(Bundle.CTL_DashBoardTopComponent());
        setToolTipText(Bundle.HINT_DashBoardTopComponent());        
    }

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        if (loaded)
            loadWidgets();
        else
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    loadWidgets();
                    loaded = true;
                }
              });
    }

    @Override
    public void componentClosed() {
        for (Component component : this.getComponents()) {
            try {
                ((TaskResultWidget)component).done();
            } catch (AbstractWidget.InvalidStateException ex) {}
        }
        removeAll();
    }
    
    public void loadWidgets() {
        List<LocalTask> allTasks = CommunicationsStub.getInstance().getTasks();
        for (LocalTask aTask : allTasks) {
            if (aTask.getExecutionType() == LocalTaskScheduleDescriptor.TYPE_LOGIN) {
                TaskResultWidget aWidget = new TaskResultWidget();
                try {
                    HashMap<String, Object> parameters = new HashMap<>();
                    parameters.put("task", aTask);
                    aWidget.setup(parameters);
                    aWidget.init();
                } catch (AbstractWidget.InvalidStateException |  InvalidParameterException ex) {}
                add(aWidget);
            }
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
