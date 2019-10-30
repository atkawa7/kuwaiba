/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.apis.web.gui.navigation.trees;

import com.vaadin.event.LayoutEvents;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropListener;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Tree representation build using the Vaadin Components of Vertical Layouts, Horizontal Layouts,
 * Buttons, Labels, and Images. Created to solve the problems in the default Vaadin Tree
 * which does not have enable lazy load of children and the acceptation of drop events over nodes. 
 * This is a simple implementation used to add root nodes, children on demand, and
 * accept select and drop events, each node can contain a icon and html caption.
 * If you need a stable implementation use the Vaadin Tree
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @param <T> The item type
 */
public class TreeLayout<T> extends VerticalLayout implements LayoutEvents.LayoutClickListener {
    /** Set of items and its graphical representation */
    private final LinkedHashMap<T, ItemLayout<T>> items = new LinkedHashMap<>();
    /** Set of current selected items */
    private final List<T> selectedItems = new ArrayList();
    /** Set of listener when a item node in the tree is expanded */
    private final List<ExpandItemListener> expandItemListeners = new ArrayList();
    /** Set of listener when a item node in the tree is collapsed */
    private final List<CollapseItemListener> collapseItemListeners = new ArrayList();
    /** Listener used to notify that a item node receive a drop event */
    private DropListener dropListener;
    /** Set of root items */     
    private final List<T> rootItems = new ArrayList();
    
    private DropEffect dropEffect = DropEffect.NONE;
    /**  Listener used to keep updated the items set in the tree */
    private final NewItemListener newItemListener = new NewItemListener() {
        @Override
        public void addItem(ItemLayout itemLayout) {
            itemLayout.addItemDropListener(getDropListener());
            items.put((T) itemLayout.getItem(), itemLayout);
        }
    };
    /**  Listener used to keep updated the items set in the tree */
    private final DeleteItemListener deleteItemListener = new DeleteItemListener() {
        @Override
        public void removeItem(ItemLayout itemLayout) {
            items.remove((T) itemLayout.getItem());
        }
    };
    
    public TreeLayout() {
        initTreeLayout();
    }
    
    private void initTreeLayout() {
        setMargin(false);
        setSpacing(false);
    }
    
    public void setDropEffect(DropEffect dropEffect) {
        this.dropEffect = dropEffect;
        for (ItemLayout itemLayout : items.values())
            itemLayout.setDropEfect(getDropEffect());
    }
    
    public DropEffect getDropEffect() {
        return dropEffect;
    }
    
    public List<T> getRootItems() {
        return rootItems;
    }
    
    public void addExpandItemListener(ExpandItemListener expandItemListener) {
        expandItemListeners.add(expandItemListener);
    }
    
    public void removeExpandItemListener(ExpandItemListener expandItemListener) {
        expandItemListeners.remove(expandItemListener);
    }
    
    public void addCollapseItemListener(CollapseItemListener collapseItemListener) {
        collapseItemListeners.add(collapseItemListener);
    }
    
    public void removeCollapseItemListener(CollapseItemListener collapseItemListener) {
        collapseItemListeners.remove(collapseItemListener);
    }
                
    public void addRootItem(T item, String itemCaption, Resource itemResource) {
        ItemLayout itemLayout = new ItemLayout(item, itemCaption, itemResource, 
            expandItemListeners, collapseItemListeners, newItemListener, deleteItemListener);
        
        items.put(item, itemLayout);
        rootItems.add(item);
        
        itemLayout.addItemClickListener(this);
        itemLayout.addItemDropListener(getDropListener());
        itemLayout.setDropEfect(dropEffect);
        addComponent(itemLayout);
    }
    
    public void addItem(T item, T child, String childCaption, Resource childResource) {
        if (items.containsKey(item)) {
            ItemLayout itemLayout = items.get(item).addChild(item, child, childCaption, childResource);
            itemLayout.addItemClickListener(this);
            itemLayout.addItemDropListener(getDropListener());
            itemLayout.setDropEfect(dropEffect);
        }
    }
    
    public void removeItem(T item) {
        if (items.containsKey(item)) {
            ItemLayout itemLayout = items.get(item);
            if (itemLayout.getItemParent() == null) { /** If the item to remove is a root item */
                
                for (Object child : itemLayout.getChildren())
                    itemLayout.removeChild(child);
                
                if (getComponentIndex(itemLayout) != -1)
                    removeComponent(itemLayout);
                items.remove(item);
                rootItems.remove(item);
            } else {
                T itemParent = (T) itemLayout.getItemParent();
                if (items.containsKey(itemParent)) {
                    ItemLayout itemParentLayout = items.get(itemParent);
                    itemParentLayout.removeChild(item);
                }
            }
        }        
    }
    
    public void expand(T item) {
        if (items.containsKey(item)) {
            ItemLayout itemLayout = items.get(item);
            for (ExpandItemListener expandItemListener : expandItemListeners)
                expandItemListener.expandItem(itemLayout);
            itemLayout.expand();
        }
    }
    
    public void collapse(T item) {
        if (items.containsKey(item)) {
            ItemLayout itemLayout = items.get(item);
            for (CollapseItemListener collapseItemListener : collapseItemListeners)
                collapseItemListener.collapseItem(itemLayout);
            itemLayout.collapse();
        }
    }
    
    public List<T> getSelectedItems() {
        return selectedItems;
    }
        
    public void addDropListener(DropListener dropListener) {
        this.dropListener = dropListener;
        for (ItemLayout itemLayout : items.values())
            itemLayout.addItemDropListener(getDropListener());
    }
    
    public DropListener getDropListener() {
        return dropListener;
    }
    
    public void clearSelectedItems() {
        for (ItemLayout itemLayout : items.values()) {
            Component component = itemLayout.getComponent(0);
            if (component instanceof ItemHorizontalLayout) {
                String styleName = "458bdc";
                if (component.getStyleName() != null && component.getStyleName().contains(styleName))
                    component.removeStyleName(styleName);
            }
        }
        selectedItems.clear();
    }
    
    
    @Override
    public void layoutClick(LayoutEvents.LayoutClickEvent event) {
        Component component = event.getComponent();
        if (component instanceof ItemHorizontalLayout) {
            String styleName = "458bdc";
            if (component.getStyleName() != null) {
                if (!component.getStyleName().contains(styleName)) {
                    UI.getCurrent().getPage().getStyles().add(""+
                        ".v-horizontallayout-" + styleName + " {"+
                        " background-color: #" + styleName + "; " +
                        "}");
                    component.setStyleName(styleName);


                    Object item = ((ItemHorizontalLayout) component).getItem();
                    selectedItems.add((T) item);
                }
                else {
                    component.removeStyleName(styleName);
                    Object item = ((ItemHorizontalLayout) component).getItem();
                    selectedItems.remove((T) item);
                }
            }
        }
    }
    /** Graphical representation of an item
     * @param <V> The item data type
     */
    public class ItemLayout<V> extends VerticalLayout {
        private static final float BUTTON_CARET_WIDTH = 16;
        private static final float BUTTON_CARET_HEIGHT = 16;
        private static final float ICON_WIDTH = 16;
        private static final float ICON_HEIGHT = 16;
        private V itemParent;
        private V item;
        private final ItemHorizontalLayout<V> layoutItem;
        private final DropTargetExtension<ItemHorizontalLayout> dropTarget;
        /** Layout used to contain the children items */
        private final VerticalLayout layoutItems = new VerticalLayout();
        /** Set of children to the current item */
        private final LinkedHashMap<V, ItemLayout<V>> children = new LinkedHashMap<>();
        /** True if children are visible. False if the children are hidden */
        private boolean isExpanded = false;
        /** Layout used to contain a padding left layout and the items layout */
        private final HorizontalLayout hl = new HorizontalLayout();
        /** Button used to hide/show the item children */
        private final Button btnCaret = new Button();
        /** Listener used to notify that the item children are visible */
        private final List<ExpandItemListener> expandItemListeners;
        /** Listener used to notify that the item children are hide */
        private final List<CollapseItemListener> collapseItemListeners;
        /** Listener used to notify to the main tree layout that a child was added */
        private final NewItemListener newItemListener;
        /** Listener used to notify to the main tree layout that a child was removed */
        private final DeleteItemListener deleteItemListener;
                        
        public ItemLayout(V item, String itemCaption, Resource itemResource, 
            List<ExpandItemListener> expandItemListeners, 
            List<CollapseItemListener> collapseItemListeners, 
            NewItemListener newItemListener, 
            DeleteItemListener deleteItemListener) {
            
            this.item = item;            
            this.expandItemListeners = expandItemListeners;
            this.collapseItemListeners = collapseItemListeners;
            this.newItemListener = newItemListener;
            this.deleteItemListener = deleteItemListener;
            
            layoutItems.setMargin(false);
            layoutItems.setSpacing(false);
                        
            layoutItem = new ItemHorizontalLayout<>(item);
            dropTarget = new DropTargetExtension<>(layoutItem);
            dropTarget.setDropEffect(DropEffect.NONE);
            
            layoutItems.setVisible(false);                        
            layoutItem.setMargin(false);
            layoutItem.setSpacing(false);
                                    
            btnCaret.setWidth(BUTTON_CARET_WIDTH, Unit.PIXELS);
            btnCaret.setHeight(BUTTON_CARET_HEIGHT, Unit.PIXELS);
            btnCaret.setIcon(VaadinIcons.CARET_RIGHT);
            btnCaret.addStyleName(ValoTheme.BUTTON_BORDERLESS);
            btnCaret.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btnCaret.addStyleName(ValoTheme.BUTTON_TINY);
                        
            btnCaret.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (layoutItems.isVisible()) {
                        for (CollapseItemListener collapseItemListener : getCollapseItemListeners())
                            collapseItemListener.collapseItem(ItemLayout.this);
                        collapse();
                    } else {
                        for (ExpandItemListener expandItemListener : getExpandItemListeners())
                            expandItemListener.expandItem(ItemLayout.this);
                        expand();
                    }
                }
            });
            layoutItem.addComponent(btnCaret);
            layoutItem.setComponentAlignment(btnCaret, Alignment.MIDDLE_CENTER);
            
            
            if (itemResource != null) {
                Image image = new Image(null, itemResource);
                image.setWidth(ICON_WIDTH, Unit.PIXELS);
                image.setHeight(ICON_HEIGHT, Unit.PIXELS);
                layoutItem.addComponent(image);
                layoutItem.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
            }
                        
            Label label = new Label(itemCaption != null ? itemCaption : item.toString(), ContentMode.HTML);
            label.addStyleName(ValoTheme.LABEL_NO_MARGIN);
            label.addStyleName(ValoTheme.LABEL_LIGHT);
            layoutItem.addComponent(label);
            layoutItem.setComponentAlignment(label, Alignment.BOTTOM_CENTER);
            
            setMargin(false);
            setSpacing(false);
            addComponent(layoutItem);            
            
            hl.setMargin(false);
            hl.setSpacing(false);
            
            VerticalLayout vl = new VerticalLayout();
            vl.setMargin(false);
            vl.setSpacing(false);
            vl.setWidth(BUTTON_CARET_WIDTH, Unit.PIXELS);
            
            hl.addComponent(vl);
            hl.addComponent(layoutItems);
        }
        
        public ItemLayout(V item, String itemCaption, Resource itemResource, 
            List<ExpandItemListener> expandItemListeners, 
            List<CollapseItemListener> collapseItemListeners, 
            NewItemListener newItemListener, 
            DeleteItemListener deleteItemListener, V itemParent) {
            this(item, itemCaption, itemResource, expandItemListeners, collapseItemListeners, newItemListener, deleteItemListener);
            this.itemParent = itemParent;
        }
        
        public void setDropEfect(DropEffect dropEffect) {
            dropTarget.setDropEffect(dropEffect);
        }
        
        public DropEffect getDropEfect() {
            return dropTarget.getDropEffect();
        }
        
        public V getItemParent() {
            return itemParent;
        }
        
        private List<ExpandItemListener> getExpandItemListeners() {
            return expandItemListeners;
        }
                
        private List<CollapseItemListener> getCollapseItemListeners() {
            return collapseItemListeners;
        }
        
        private NewItemListener getnewItemListener() {
            return newItemListener;
        }
        
        private DeleteItemListener getDeleteItemListener() {
            return deleteItemListener;
        }
        
        public boolean isExpanded() {
            return isExpanded;
        }
        
        public void addItemClickListener(LayoutEvents.LayoutClickListener layoutClickListener) {
            layoutItem.addLayoutClickListener(layoutClickListener);
        }
        
        public void addItemDropListener(DropListener dropListener) {
            if (dropListener != null)
                dropTarget.addDropListener(dropListener);
        }
        
        public List<V> getChildren() {
            List<V> result = new ArrayList();
            for (V child : children.keySet())
                result.add(child);
            return result;
        }
        
        public ItemLayout addChild(V parent, V child, String caption, Resource childResource) {
            ItemLayout itemLayout = new ItemLayout(child, caption, childResource, 
                getExpandItemListeners(), getCollapseItemListeners(), 
                getnewItemListener(), getDeleteItemListener(), parent);
            children.put(child, itemLayout);
            getnewItemListener().addItem(itemLayout);
            
            if (isExpanded())
                expand();
            return itemLayout;
        }
        
        public void removeChild(V child) {
            if (children.containsKey(child)) {
                ItemLayout childLayout = children.get(child);
                
                for (Object theChild : childLayout.getChildren())
                    childLayout.removeChild(theChild);
                
                if (layoutItems.getComponentIndex(childLayout) != -1)
                    layoutItems.removeComponent(childLayout);
                                
                children.remove(child);
                getDeleteItemListener().removeItem(childLayout);
            }
                        
        }
        
        public V getItem() {
            return item;
        }
        
        public void setItem(V item) {
            this.item = item;            
        }
        
        public void expand() {
            btnCaret.setIcon(VaadinIcons.CARET_DOWN);
            
            isExpanded = true;
            layoutItems.setVisible(true);
            layoutItems.removeAllComponents();
            
            if (children.isEmpty())
                return;
            
            if (getComponentIndex(hl) == -1)
                addComponent(hl);
            
            for (ItemLayout child : children.values())
                layoutItems.addComponent(child);                                
        }
        
        public void collapse() {
            btnCaret.setIcon(VaadinIcons.CARET_RIGHT);
            
            isExpanded = false;
            layoutItems.setVisible(false);
            layoutItems.removeAllComponents();    
            
            if (getComponentIndex(hl) != -1)
                removeComponent(hl);
        }        
    }
    /** Item layout used only to accept the selection event */
    public class ItemHorizontalLayout<S> extends HorizontalLayout {
        private S item;
        
        public ItemHorizontalLayout(S item) {
            this.item = item;
        }
                
        public S getItem() {
            return item;
        }
    }
    /** Listener used to notify that an item are expanding */
    public interface ExpandItemListener {
        void expandItem(TreeLayout.ItemLayout itemLayout);
    }
    /** Listener used to notify that an item are collapse */
    public interface CollapseItemListener {
        void collapseItem(TreeLayout.ItemLayout itemLayout);
    }
    /** Listener used to notify that an item is adding */
    public interface NewItemListener {
        void addItem(TreeLayout.ItemLayout itemLayout);
    }
    /** Listener used to notify that an item is removing */
    public interface DeleteItemListener {
        void removeItem(TreeLayout.ItemLayout itemLayout);
    }
}
