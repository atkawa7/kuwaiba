package org.inventory.navigation.applicationnodes.objectnodes.windows;

import java.awt.BorderLayout;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows an editor for a given object embedding a PropertySheetView
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectEditorTopComponent extends TopComponent{

    static final String ICON_PATH = "org/inventory/navigation/applicationnodes/res/edit.png";

    private PropertySheetView editor;
    private Node[] nodes;

    public ObjectEditorTopComponent(){}

    public ObjectEditorTopComponent(Node[] _nodes) {
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        editor = new PropertySheetView();
        this.nodes = _nodes;

        this.setDisplayName(nodes[0].getDisplayName());

        //This requires that CoreUI to be enable in the project
        Mode myMode = WindowManager.getDefault().findMode("properties");
        if (myMode != null)
            myMode.dockInto(this);
        else{
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("Display Warning", NotificationUtil.WARNING, "\"Properties\" Window Mode not available");
        }

        setLayout(new BorderLayout());

        add(editor,BorderLayout.CENTER);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        //This is important. If setNodes is called in the constructor, it won't work!
        editor.setNodes(nodes);
    }
}
