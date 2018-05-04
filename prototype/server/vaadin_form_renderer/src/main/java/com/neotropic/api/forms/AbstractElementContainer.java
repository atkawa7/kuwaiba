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
package com.neotropic.api.forms;

import java.util.ArrayList;
import java.util.List;

/**
 * A element container is an element which can contain other elements containers 
 * and fields.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class AbstractElementContainer extends AbstractElement {
    private List<AbstractElement> children;
    public boolean repaint = false;

    public List<AbstractElement> getChildren() {
        return children;
    }
    
    public void addChild(AbstractElement child) {
        if (children == null)
            children = new ArrayList();
        children.add(child);
    }
    
    public void removeChild(AbstractElement child) {
        if (children != null)
            children.remove(child);
    }
    
    public boolean repaint() {
        return repaint;        
    }
    
    public void setRepaint(boolean repaint) {
        this.repaint = repaint;                
    }
    
    public void clean() {
        cleanRecursive(this);                                
    }
    
    private void cleanRecursive(AbstractElementContainer parent) {
        if (parent.getChildren() != null) {
            for (AbstractElement child : parent.getChildren()) {
                if (child instanceof AbstractElementField) {
                    Object oldValue = ((AbstractElementField) child).getValue();
                    Object newValue = null;
                    
                    if (((AbstractElementField) child).isCleanable()) {
                        ((AbstractElementField) child).setValue(newValue);

                        child.fireElementEvent(new EventDescriptor(
                            Constants.EventAttribute.ONPROPERTYCHANGE, 
                            Constants.Property.VALUE, newValue, oldValue));
                    }
                } else if (child instanceof AbstractElementContainer)
                    cleanRecursive((AbstractElementContainer) child);                
            }
        }
    }
    
    @Override
    public void propertyChange() {
        if (hasProperty(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.REPAINT)) {
            
            boolean oldValue = repaint();
            boolean newValue = (boolean) getNewValue(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.REPAINT);

            setRepaint(newValue);

            firePropertyChangeEvent();
            
            fireElementEvent(new EventDescriptor(
                Constants.EventAttribute.ONPROPERTYCHANGE, 
                Constants.Property.REPAINT, newValue, oldValue));
            
            for (AbstractElement child : getChildren()) {
                if (child instanceof AbstractElementContainer)
                    child.propertyChange();
            }
        }
        super.propertyChange();        
    }
}
