/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.dashboard.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.security.InvalidParameterException;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalTaskResult;
import org.inventory.communications.core.LocalTaskResultMessage;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.i18n.I18N;

/**
 * A widget that shows the results of a task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TaskResultWidget extends AbstractWidget {
    private static Icon ICON_OK = new ImageIcon(Utils.createCircleIcon(DashboardWidgetUtilities.GREEN, 15));
    private static Icon ICON_WARNING = new ImageIcon(Utils.createCircleIcon(DashboardWidgetUtilities.YELLOW, 15));
    private static Icon ICON_CRITICAL = new ImageIcon(Utils.createCircleIcon(DashboardWidgetUtilities.RED, 15));
    private LocalTask task;
        
    @Override
    public String getName() {
        return "Task Results Widget"; //NOI18N
    }
    
    @Override
    public String getTitle() {
        return String.format(task.getName());
    }
    
    @Override
    public String getDescription() {
        return I18N.gm("Dashboard.description");
    }

    @Override
    public String getVersion() {
        return "1.0"; //NOI18N
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS"; //NOI18N
    }
    
    @Override
    public void setup(HashMap<String, Object> parameters) throws InvalidStateException, InvalidParameterException {
        if (state != WIDGET_STATE_CREATED)
            throw new InvalidStateException("Widget state is not CREATED");
        
        task = (LocalTask)parameters.get("task"); //NOI18N
        if (task == null)
            throw new InvalidParameterException("The parameter \"task\" is missing");
        
        state = WIDGET_STATE_SET_UP;
    }
    
    @Override
    public void init() throws InvalidStateException {
        if (state != WIDGET_STATE_SET_UP)
            throw new InvalidStateException("Widget state is not SET_UP");
        
        setLayout(new BorderLayout());
        
        JScrollPane pnlScrollMain = new JScrollPane();
        
        add(pnlScrollMain);
                
        JPanel pnlInner = new JPanel(new GridBagLayout());
        pnlInner.setBackground(Color.WHITE);
        
        pnlScrollMain.setViewportView(pnlInner);
        
        GridBagConstraints layoutConstraints = new GridBagConstraints();
        
        //The horizontal fill is a general setting
        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
              
        //Let's configure how the title will be placed
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;

        layoutConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        layoutConstraints.weightx = 1; //Fill in the remaining space
        layoutConstraints.weighty = 1; 

        
        //Now, we create the actual title component
        JLabel lblTitle = new JLabel("<html><b>" + getTitle() + "</b></html>", SwingConstants.RIGHT);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(DashboardWidgetUtilities.BLUE);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        add(lblTitle, BorderLayout.NORTH);
        
        LocalTaskResult taskResult = CommunicationsStub.getInstance().executeTask(task.getId());
        
        if (taskResult == null) {
            JLabel lblError = DashboardWidgetUtilities.buildOpaqueLabel(String.format(I18N.gm("error")+" %s", 
                    CommunicationsStub.getInstance().getError()), DashboardWidgetUtilities.LIGHT_RED);
            layoutConstraints.gridy = 1;           
            pnlInner.add(lblError, layoutConstraints);
            
        } else {
            if (taskResult.getMessages().isEmpty()) {
                JLabel lblNoResults = DashboardWidgetUtilities.buildOpaqueLabel(String.format(I18N.gm("no_results_for_task")), DashboardWidgetUtilities.LIGHT_GREEN);

                layoutConstraints.gridy = 1;

                pnlInner.add(lblNoResults, layoutConstraints);
            } else {
                int i = 1;
                for (LocalTaskResultMessage message : taskResult.getMessages()) {
                    Icon theIcon;

                    switch (message.getMessageType()) {
                        default:
                        case LocalTaskResultMessage.STATUS_SUCCESS:
                            theIcon = ICON_OK;
                            break;
                        case LocalTaskResultMessage.STATUS_WARNING:
                            theIcon = ICON_WARNING;
                            break;
                        case LocalTaskResultMessage.STATUS_ERROR:
                            theIcon = ICON_CRITICAL;
                            break;
                    }

                    layoutConstraints.gridy = i;

                    pnlInner.add(DashboardWidgetUtilities.buildDecoratedOpaqueTextField(message.getMessage(), null, theIcon), layoutConstraints);

                    i++;
                }
            }
        }
        
        state = WIDGET_STATE_INITIALIZED;
    }
    
    @Override
    public void refresh() throws InvalidStateException {
        if (state != WIDGET_STATE_INITIALIZED)
            throw new InvalidStateException("Widget state is not INITIALIZED");
        removeAll();
        init();
    }
    
    @Override
    public void done() throws InvalidStateException {
        if (state != WIDGET_STATE_INITIALIZED)
            throw new InvalidStateException("Widget state is not INITIALIZED");
        
        removeAll();
        
        state = WIDGET_STATE_DONE;
    }    
}
