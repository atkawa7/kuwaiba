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
package org.neotropic.kuwaiba.modules.core.search;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.util.visual.icons.IconGenerator;

/**
 * Renders the result of a search in the navigation module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class BusinessObjectSearchResultRenderer extends ComponentRenderer<VerticalLayout, BusinessObjectLight> {
    //TODO integrate this with the render of the service module
    private final List<AbstractVisualInventoryAction> actions;
    private final SearchResultCallback<BusinessObjectLight> resultCallback;
    private final IconGenerator iconGenerator;
    
    /**
     * Main constructor.
     * @param actions The list of actions associated to the present search result.
     * @param searchResultCallback What code should be trigger upon clicking on a search result.
     * @param iconGenerator
     */
    public BusinessObjectSearchResultRenderer(List<AbstractVisualInventoryAction> actions, 
            SearchResultCallback<BusinessObjectLight> searchResultCallback, IconGenerator iconGenerator) {
        super();
        this.actions = actions;
        this.resultCallback = searchResultCallback;
        this.iconGenerator = iconGenerator;
    }

    @Override
    public VerticalLayout createComponent(BusinessObjectLight obj) {
        VerticalLayout lytSearchResult = new VerticalLayout();
        lytSearchResult.setSizeFull();
        lytSearchResult.setPadding(false);
        
        Div divTitle = new Div(new Label(obj.toString()));
        divTitle.setClassName("search-result-title");
        divTitle.setWidthFull();

        Image objIcon = new Image(StreamResourceRegistry.getURI(iconGenerator.apply(obj)).toString(), "-");
        objIcon.setHeight(Integer.toString(ResourceFactory.DEFAULT_SMALL_ICON_HEIGHT) + "px");
        objIcon.setWidth(Integer.toString(ResourceFactory.DEFAULT_SMALL_ICON_WIDTH) + "px");
        
        HorizontalLayout lytTitle = new HorizontalLayout(objIcon, divTitle);

        lytTitle.setBoxSizing(BoxSizing.BORDER_BOX);  
        lytTitle.setSpacing(true);
        lytTitle.setMargin(false);
        lytTitle.setPadding(false);
        lytTitle.setDefaultVerticalComponentAlignment(
                FlexComponent.Alignment.CENTER);
                
//        divTitle.addClickListener( e -> {
//            replaceContent(this.resultCallback.buildSearchResultDetailsPage(result));
//        });

        HorizontalLayout lytActions = new HorizontalLayout();
        lytActions.setClassName("search-result-actions");
        actions.stream().forEach( anAction -> {
            Button btnAction = new Button(anAction.getModuleAction().getDisplayName(), anAction.getModuleAction().getIcon());
            btnAction.setClassName("search-result-action-button");
            btnAction.getElement().setProperty("title", anAction.getModuleAction().getDescription());
            btnAction.addClickListener( event -> {
                ((Dialog)anAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(DeleteBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT, obj)))).open();
            });
            lytActions.add(btnAction);
        });

        lytSearchResult.add(lytTitle, lytActions);
        return lytSearchResult;
    } 
}
