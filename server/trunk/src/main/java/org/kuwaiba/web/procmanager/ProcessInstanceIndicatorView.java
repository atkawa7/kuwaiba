/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.procmanager;

import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Random;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * This view shows a set of activity indicators, in a tabular representation. 
 * The first column show the path to the current activity
 * @author johnyortega
 */
public class ProcessInstanceIndicatorView extends VerticalLayout {
    private final RemoteProcessInstance remoteProcessInstance;
    private final WebserviceBean webserviceBean;
    private final RemoteSession remoteSession;
    
    public ProcessInstanceIndicatorView(RemoteProcessInstance remoteProcessInstance, WebserviceBean webserviceBean, RemoteSession remoteSession) {
        this.remoteProcessInstance = remoteProcessInstance;
        this.webserviceBean = webserviceBean;
        this.remoteSession = remoteSession;
        
        setWidthUndefined();
        setHeightUndefined();
        initialize();
    }
    
    private void initialize() {        
        addStyleName("indicatortable");
        // Column 1: Activity
        // Column 2: Role
        // Column 3: Status
        // Column 4: Start
        // Column 5: Finish
        List<RemoteActivityDefinition> lstActivities = null;
                
        try {
            lstActivities = webserviceBean.getProcessInstanceActivitiesPath(
                remoteProcessInstance.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());
        } catch (ServerSideException ex) {
            //Exceptions.printStackTrace(ex);
            return;
        }
        
        float width = 200;
        float height = 35;
        int columns = 7;
        int rows = lstActivities.size();
        
        GridLayout header = new GridLayout();
        
        header.setColumns(columns);
        header.setRows(1);
        
        for (int j = 0; j < columns; j += 1) {
            
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setHeight(height, Unit.PIXELS);
            verticalLayout.setWidth(width + (j == 0 ? width + 100 : 0), Unit.PIXELS);
            
            header.addComponent(verticalLayout, j, 0);
        }
        Label lblActivity = new Label("Activity");
        lblActivity.setStyleName(ValoTheme.LABEL_BOLD);
        
        Label lblRole = new Label("Role");
        lblRole.setStyleName(ValoTheme.LABEL_BOLD);
        
        Label lblStart = new Label("Start");
        lblStart.setStyleName(ValoTheme.LABEL_BOLD);
        
        Label lblFinish = new Label("Finish");
        lblFinish.setStyleName(ValoTheme.LABEL_BOLD);   
        
        Label lblExpectedDuration = new Label("Expected Duration");
        lblExpectedDuration.setStyleName(ValoTheme.LABEL_BOLD);
        
        Label lblRealDuration = new Label("Real Duration");
        lblRealDuration.setStyleName(ValoTheme.LABEL_BOLD);
        
        Label lblStatus = new Label("Status");
        lblStatus.setStyleName(ValoTheme.LABEL_BOLD);
                
        ((VerticalLayout) header.getComponent(0, 0)).addComponent(lblActivity);
        ((VerticalLayout) header.getComponent(0, 0)).setComponentAlignment(lblActivity, Alignment.MIDDLE_CENTER);
        
        ((VerticalLayout) header.getComponent(1, 0)).addComponent(lblRole);
        ((VerticalLayout) header.getComponent(1, 0)).setComponentAlignment(lblRole, Alignment.MIDDLE_CENTER);
                
        ((VerticalLayout) header.getComponent(2, 0)).addComponent(lblStart);
        ((VerticalLayout) header.getComponent(2, 0)).setComponentAlignment(lblStart, Alignment.MIDDLE_CENTER);
        
        ((VerticalLayout) header.getComponent(3, 0)).addComponent(lblFinish);
        ((VerticalLayout) header.getComponent(3, 0)).setComponentAlignment(lblFinish, Alignment.MIDDLE_CENTER);
        
        ((VerticalLayout) header.getComponent(4, 0)).addComponent(lblExpectedDuration);
        ((VerticalLayout) header.getComponent(4, 0)).setComponentAlignment(lblExpectedDuration, Alignment.MIDDLE_CENTER);
        
        ((VerticalLayout) header.getComponent(5, 0)).addComponent(lblRealDuration);
        ((VerticalLayout) header.getComponent(5, 0)).setComponentAlignment(lblRealDuration, Alignment.MIDDLE_CENTER);
        
        ((VerticalLayout) header.getComponent(6, 0)).addComponent(lblStatus);
        ((VerticalLayout) header.getComponent(6, 0)).setComponentAlignment(lblStatus, Alignment.MIDDLE_CENTER);
        
        GridLayout data = new GridLayout();
        
        data.setColumns(columns);
        data.setRows(rows);
        for (int j = 0; j < columns; j += 1) {
            
            for (int i = 0; i < rows; i += 1) {
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setHeight(height, Unit.PIXELS);
                verticalLayout.setWidth(width + (j == 0 ? width + 100 : 0), Unit.PIXELS);
                                
                data.addComponent(verticalLayout, j, i);
            }
        }
        setSpacing(false);
        
        addComponent(header);
        addComponent(data);
        
        setComponentAlignment(header, Alignment.MIDDLE_CENTER);
        setComponentAlignment(data, Alignment.MIDDLE_CENTER);
        
        initializeData(data, lstActivities);
    }
    
    private void initializeData(GridLayout data, List<RemoteActivityDefinition> lstActivities) {
        Random r = new Random();
        
////        int columns = data.getColumns();
        int rows = data.getRows();
        
        VerticalLayout cell = null;
        Label lblCell = null;
        
        for (int i = 0; i < rows; i += 1) {
            
            cell = (VerticalLayout) data.getComponent(0, i);
            lblCell = new Label(lstActivities.get(i).getName());
            cell.addComponent(lblCell);
            cell.setComponentAlignment(lblCell, Alignment.MIDDLE_LEFT);

            cell = (VerticalLayout) data.getComponent(1, i);
            lblCell = new Label(lstActivities.get(i).getActor().getName());
            cell.addComponent(lblCell);
            cell.setComponentAlignment(lblCell, Alignment.MIDDLE_LEFT);

            cell = (VerticalLayout) data.getComponent(2, i);
            lblCell = new Label(LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)));
            cell.addComponent(lblCell);
            cell.setComponentAlignment(lblCell, Alignment.MIDDLE_LEFT);

            int c = r.nextInt(10);
            int d = r.nextInt(10);

            cell = (VerticalLayout) data.getComponent(3, i);
            lblCell = new Label(LocalDate.now().plusDays(c).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)));
            cell.addComponent(lblCell);
            cell.setComponentAlignment(lblCell, Alignment.MIDDLE_LEFT);

            cell = (VerticalLayout) data.getComponent(4, i);
            lblCell = new Label("" + d);
            cell.addComponent(lblCell);
            cell.setComponentAlignment(lblCell, Alignment.MIDDLE_LEFT);
            r.nextInt(10);

            cell = (VerticalLayout) data.getComponent(5, i);
            lblCell = new Label("" + c);
            cell.addComponent(lblCell);
            cell.setComponentAlignment(lblCell, Alignment.MIDDLE_LEFT);

            cell = (VerticalLayout) data.getComponent(6, i);

            if (c > d) {
                cell.addStyleName("critical");
            }
            else {
                if (c > 5) {
                    cell.addStyleName("warning");
                } else {
                    cell.addStyleName("normal");
                }
            }
        }
    }
}
