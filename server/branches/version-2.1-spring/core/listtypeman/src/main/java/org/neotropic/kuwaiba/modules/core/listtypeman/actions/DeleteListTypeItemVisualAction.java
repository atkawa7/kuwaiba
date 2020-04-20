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

package org.neotropic.kuwaiba.modules.core.listtypeman.actions;

import java.util.List;
import java.util.HashMap;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new customer action that provides means to choose the service pool and type.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class DeleteListTypeItemVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteListTypeItemAction deleteListTypeItemAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight seletedListTypeItem = null;
        if (parameters != null & parameters.length > 0) {
            for (ModuleActionParameter param : parameters) {
                if (param.getName().equals("listTypeItem"))
                    seletedListTypeItem = (BusinessObjectLight) param.getValue();
            }
        }
        if (seletedListTypeItem == null) {
            return null;
        }
        BusinessObjectLight listTypeItem = seletedListTypeItem;
        
        ConfirmDialog wdwDeleteListTypeItem = new ConfirmDialog(ts.getTranslatedString("module.general.labels.confirmcaption"),
                ts.getTranslatedString("module.general.labels.confirmdeletemessage"),
                ts.getTranslatedString("module.general.labels.delete"));
        wdwDeleteListTypeItem.getBtnConfirm().addClickListener((ev) -> {
            try {
                
                deleteListTypeItemAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>("className", listTypeItem.getClassName()),
                        new ModuleActionParameter<>("oid", listTypeItem.getId())));
                
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCESS,
                        ts.getTranslatedString("module.listtypeman.actions.delete-list-type-item.ui.item-created-success"), NewListTypeItemAction.class));
                wdwDeleteListTypeItem.close();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewListTypeItemAction.class));
                wdwDeleteListTypeItem.close();
            }
        });
        return wdwDeleteListTypeItem; 
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteListTypeItemAction;
    }
}
