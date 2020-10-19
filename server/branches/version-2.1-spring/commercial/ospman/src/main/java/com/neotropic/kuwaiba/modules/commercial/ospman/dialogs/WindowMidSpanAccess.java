/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Emphasis;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to manage mid-span access and splice of links
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowMidSpanAccess extends Dialog implements ActionCompletedListener {
    private ComboBox<BusinessObjectLight> cmbCable;
    private ComboBox<BusinessObjectLight> cmbDevice;
    private Div divLocation;
    
    private DataProvider<BusinessObjectLight, String> cablesDataProvider;
    private DataProvider<BusinessObjectLight, String> devicesDataProvider;
    private final BusinessObjectLight node;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final PhysicalConnectionsService physicalConnectionsService;
    private final NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    
    public WindowMidSpanAccess(BusinessObjectLight node,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction, PhysicalConnectionsService physicalConnectionsService) {
        
        super();
        Objects.requireNonNull(node);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(bem);
        this.node = node;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.physicalConnectionsService = physicalConnectionsService;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setWidth("90%");
        setHeight("90%");
    }

    @Override
    public void open() {
        try {
            if (bem.hasSpecialAttribute(node.getClassName(), node.getId(), "endpointA") || //NOI18N
            bem.hasSpecialAttribute(node.getClassName(), node.getId(), "endpointB")) { //NOI18N
                
                final String width = "256px";
                H4 lblTitle = new H4(ts.getTranslatedString("module.ospman.mid-span-access.title"));
                lblTitle.setWidth(width);

                ComboBox<BusinessObjectLight> cmbLocation = new ComboBox(ts.getTranslatedString("module.ospman.mid-span-access.location"));
                cmbLocation.setRequiredIndicatorVisible(true);
                cmbLocation.setWidth(width);
                cmbLocation.setItems(Arrays.asList(node));
                cmbLocation.setValue(node);
                cmbLocation.setItemLabelGenerator(BusinessObjectLight::getName);
                cmbLocation.setEnabled(false);

                cmbCable = new ComboBox(ts.getTranslatedString("module.ospman.mid-span-access.cable"));
                cmbCable.setRequiredIndicatorVisible(true);
                cmbCable.setWidth(width);
                cmbCable.setItemLabelGenerator(cable -> cable.getName());
                cmbCable.setRenderer(new ComponentRenderer<>(cable -> {
                    Label lblName = new Label(cable.getName());
                    Emphasis lblClassName = new Emphasis(cable.getClassName());
                    VerticalLayout lytCable = new VerticalLayout(lblName, lblClassName);
                    lytCable.setMargin(false);
                    lytCable.setPadding(false);
                    lytCable.setSpacing(false);
                    return lytCable;
                }));

                cmbCable.addFocusListener(event -> {
                    if (cablesDataProvider != null) {
                        if (cmbCable.isOpened())
                            cmbCable.getDataProvider().refreshAll();
                    }
                    else
                        setCablesDataPovider();
                });        

                cmbDevice = new ComboBox(ts.getTranslatedString("module.ospman.mid-span-access.device"));
                cmbDevice.setRequiredIndicatorVisible(true);
                cmbDevice.setWidth(width);
                cmbDevice.setItemLabelGenerator(device -> device.getName());
                cmbDevice.setEnabled(false);
                cmbDevice.setRenderer(new ComponentRenderer<>(device -> {
                    Label lblName = new Label(device.getName());
                    Emphasis lblClassName = new Emphasis(device.getClassName());
                    VerticalLayout lytDevice = new VerticalLayout(lblName, lblClassName);
                    lytDevice.setMargin(false);
                    lytDevice.setPadding(false);
                    lytDevice.setSpacing(false);
                    return lytDevice;
                 }));

                cmbDevice.addFocusListener(event -> {
                    if (devicesDataProvider != null) {
                        if (cmbDevice.isOpened())
                            cmbDevice.getDataProvider().refreshAll();
                    }
                    else
                        setDevicesDataProvider();
                });

                Button btnNewDevice = new Button(new Icon(VaadinIcon.PLUS), event -> {
                    newBusinessObjectVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT, node))
                    ).open();
                });
                btnNewDevice.setWidth("16px");
                btnNewDevice.setEnabled(false);

                cmbCable.addValueChangeListener(event -> {
                    if (event.getValue() != null) {
                        cmbDevice.setEnabled(true);
                        btnNewDevice.setEnabled(true);

                        if (cmbCable.getValue() != null && cmbDevice.getValue() != null)
                            updateOspLocationView();
                    } else {
                        cmbDevice.setValue(null);
                        cmbDevice.setEnabled(false);
                        btnNewDevice.setEnabled(false);
                    }
                });        
                Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), event -> close());

                HorizontalLayout lytRow3 = new HorizontalLayout(cmbDevice, btnNewDevice);
                lytRow3.setSpacing(false);
                lytRow3.setMargin(false);
                lytRow3.setPadding(false);

                VerticalLayout lytRow4 = new VerticalLayout();
                lytRow4.setSizeFull();

                VerticalLayout lytSelector = new VerticalLayout(lblTitle, cmbLocation, cmbCable, lytRow3, lytRow4, btnClose);
                lytSelector.setWidth("282px");
                lytSelector.setHeightFull();
                lytSelector.setSpacing(false);
                lytSelector.setMargin(false);
                lytSelector.setPadding(false);
                lytSelector.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, btnClose);

                divLocation = new Div();
                divLocation.setSizeFull();
                divLocation.getStyle().set("border-style", "solid"); //NOI18N
                divLocation.getStyle().set("border-width", "1px"); //NOI18N
                divLocation.getStyle().set("border-color", "var(--paper-grey-900)"); //NOI18N

                cmbDevice.addValueChangeListener(event -> {
                    if (cmbCable.getValue() != null && cmbDevice.getValue() != null)
                        updateOspLocationView();
                });

                HorizontalLayout lytMain = new HorizontalLayout(lytSelector, divLocation);
                lytMain.setSpacing(false);
                lytMain.setMargin(false);
                lytMain.setPadding(false);
                lytMain.setSizeFull();
                add(lytMain);
                this.newBusinessObjectVisualAction.registerActionCompletedLister(this);
                super.open();
            } else {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"),
                    ts.getTranslatedString("module.ospman.mid-span-access.there-are-no-cables")
                ).open();
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage()
            ).open();
        }
    }

    @Override
    public void close() {
        this.newBusinessObjectVisualAction.unregisterListener(this);
        super.close();
    }
    
    private void setCablesDataPovider() {
        CableService cableService = new CableService();
        cablesDataProvider = DataProvider.fromFilteringCallbacks(
            query -> cableService.fetch(query.getLimit(), query.getOffset(), query.getFilter().orElse(null)), 
            query -> cableService.count(query.getFilter().orElse(null))
        );
        cmbCable.setDataProvider(cablesDataProvider);
    }
    
    private void setDevicesDataProvider() {
        DeviceService deviceService = new DeviceService();
        devicesDataProvider = DataProvider.fromFilteringCallbacks(
            query -> deviceService.fecth(query.getLimit(), query.getOffset(), query.getFilter().orElse("")), 
            query -> deviceService.count(query.getFilter().orElse(""))
        );
        cmbDevice.setDataProvider(devicesDataProvider);
    }
    
    private void updateOspLocationView() {
        divLocation.removeAll();
        OspLocationView ospLocationView = new OspLocationView(node, cmbCable.getValue(), cmbDevice.getValue(), aem, bem, mem, ts, physicalConnectionsService);
        divLocation.add(ospLocationView);
    }
        
    @Override
    public void actionCompleted(ActionCompletedEvent event) {
        if (event.getStatus() == ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), event.getMessage()).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), event.getMessage()).open();
    }
    
    private class CableService {
        List<BusinessObjectLight> cables = new ArrayList();
        List<BusinessObjectLight> filterCables = new ArrayList();
        
        public Stream<BusinessObjectLight> fetch(int limit, int offset, String filter) {
            return filter.isEmpty() ? cables.stream() : filterCables.stream();
        }
        
        public int count(String filter) {
            
            HashMap<String, List<BusinessObjectLight>> attrs;
            try {
                if (filter.isEmpty()) {
                    cables.clear();
                    attrs = bem.getSpecialAttributes(node.getClassName(), node.getId(),
                            PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA,
                            PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB);
                    attrs.values().forEach(value -> cables.addAll(value));
                }
                else {
                    filterCables = cables.stream().filter(
                        cable -> cable.getName().toLowerCase().contains(filter.toLowerCase())
                    ).collect(Collectors.toList());
                }
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage()
                ).open();
            }
            int size = filter.isEmpty() ? cables.size() : filterCables.size();
            cmbCable.setPageSize(size > 0 ? size : 1);
            return size;
        }
    }
    
    private class DeviceService {
        List<BusinessObjectLight> devices = new ArrayList();
        List<BusinessObjectLight> filterDevices = new ArrayList();
        
        public Stream<BusinessObjectLight> fecth(int limit, int offset, String filter) {
            return filter.isEmpty() ? devices.stream() : filterDevices.stream();
        }
        
        public int count(String filter) {            
            try {
                if (filter.isEmpty()) {
                    devices.clear();
                    devices = bem.getObjectChildren(node.getClassName(), node.getId(), -1);
                } else {
                    filterDevices = devices.stream().filter(
                        device -> device.getName().toLowerCase().contains(filter.toLowerCase())
                    ).collect(Collectors.toList());
                }
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage()
                ).open();
            }
            int size = filter.isEmpty() ? devices.size() : filterDevices.size();
            cmbDevice.setPageSize(size > 0 ? size : 1);
            return size;
        }
    }
}
