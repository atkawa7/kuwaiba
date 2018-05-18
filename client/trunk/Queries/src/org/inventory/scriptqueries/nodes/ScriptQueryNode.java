/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import org.inventory.communications.core.LocalScriptQuery;
import org.inventory.communications.util.Constants;
import org.inventory.scriptqueries.nodes.actions.ScriptQueriesManagerActionsFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ScriptQueryNode extends AbstractNode implements VetoableChangeListener {
    private static final String BASE_EXT = "org/inventory/scriptqueries/res/script_query.png";
    
    public ScriptQueryNode(LocalScriptQuery scriptQuery) {
        super(Children.LEAF, Lookups.singleton(scriptQuery));
        setIconBaseWithExtension(BASE_EXT);
        scriptQuery.addChangeListener(this);
    }
    
    @Override
    public String getDisplayName() {
        LocalScriptQuery scriptQuery = getLookup().lookup(LocalScriptQuery.class);
        return scriptQuery.getName() == null || scriptQuery.getName().isEmpty() ? "<No Name>" : scriptQuery.getName();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            ScriptQueriesManagerActionsFactory.getAddParameterToScriptQueryAction(), 
            ScriptQueriesManagerActionsFactory.getRemoveParameterFromScriptQueryAction(), 
            null,
            ScriptQueriesManagerActionsFactory.getExecuteScriptQueryAction(), 
            ScriptQueriesManagerActionsFactory.getExecuteScriptQueryCollectionAction(), 
            null, 
            ScriptQueriesManagerActionsFactory.getDeleteScriptQueryAction()
        };                
    }
    
    public void resetPropertySheet() {
        setSheet(createSheet());
    }
    
    @Override
    protected Sheet createSheet() {
        LocalScriptQuery scriptQuery = getLookup().lookup(LocalScriptQuery.class);
        
        Sheet sheet = Sheet.createDefault();
        try {
            Sheet.Set generalPropertySet = Sheet.createPropertiesSet();
            generalPropertySet.setName("General");
            generalPropertySet.setDisplayName("General");
            
            PropertySupport.Reflection<String> nameProperty = new PropertySupport.Reflection(scriptQuery, String.class, Constants.PROPERTY_NAME);
            generalPropertySet.put(nameProperty);
            
            PropertySupport.Reflection<String> descriptionProperty = new PropertySupport.Reflection(scriptQuery, String.class, Constants.PROPERTY_DESCRIPTION);
            generalPropertySet.put(descriptionProperty);
            
            PropertySupport.Reflection<String> scriptProperty = new PropertySupport.Reflection(scriptQuery, String.class, Constants.PROPERTY_SCRIPT);
            generalPropertySet.put(scriptProperty);
            
            PropertySupport.Reflection<String> countableProperty = new PropertySupport.Reflection(scriptQuery, String.class, Constants.PROPERTY_COUNTABLE);
            generalPropertySet.put(countableProperty);
            
            Sheet.Set parametersPropertySet = Sheet.createPropertiesSet();
            parametersPropertySet.setName("Parameters");
            parametersPropertySet.setDisplayName("Parameters");
            for (String parameterName : scriptQuery.getParameters().keySet()) {
                PropertySupport.ReadWrite<String> parameterProperty = new ParameterPropertySupport(scriptQuery, parameterName, String.class, parameterName, "");
                parametersPropertySet.put(parameterProperty);
            }

            sheet.put(generalPropertySet);
            sheet.put(parametersPropertySet);
            
        } catch (NoSuchMethodException ex) {  }
                
        return sheet;
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (Constants.PROPERTY_NAME.equals(evt.getPropertyName()))
            fireNameChange(null, (String) evt.getNewValue()); //TODO: don't work update name
//            setDisplayName((String) evt.getNewValue());
    }
    
    private static class ParameterPropertySupport extends PropertySupport.ReadWrite<String> {
        private final LocalScriptQuery scriptQuery;
        
        public ParameterPropertySupport(LocalScriptQuery scriptQuery, String name, Class<String> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
            this.scriptQuery = scriptQuery;
        }
        
        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return scriptQuery.getParameters().get(getName());
        }

        @Override
        public void setValue(String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            scriptQuery.setParameter(getName(), value);
        }
    }
            
}
