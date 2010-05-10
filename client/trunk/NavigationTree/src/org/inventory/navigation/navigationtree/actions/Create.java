package org.inventory.navigation.navigationtree.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter.Popup;


public final class Create extends AbstractAction implements Popup{
    private LocalObjectLight lol;
    private ObjectNode node;
    

    public Create(LocalObjectLight _lol, ObjectNode _node) {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/actions/Bundle").getString("LBL_NEW"));
        this.lol=_lol;
        this.node = _node;
    }

    public void actionPerformed(ActionEvent ev) {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        LocalObjectLight myLol = CommunicationsStub.getInstance().createObject(
                ((JMenuItem)ev.getSource()).getName(),
                this.lol.getOid(),
                null);
        if (myLol == null)
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/actions/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.ERROR,
                    CommunicationsStub.getInstance().getError());
        else{
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/actions/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.INFO,
                    java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/actions/Bundle").getString("LBL_CREATED"));
        }
    }

    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/actions/Bundle").getString("LBL_NEW"));

        List<LocalClassMetadataLight> items = CommunicationsStub.getInstance().
                getPossibleChildren(lol.getPackageName()+"."+lol.getClassName());

        if (items.size() == 0) mnuPossibleChildren.setEnabled(false);
        else
            for(LocalClassMetadataLight item: items){
                JMenuItem smiChildren = new JMenuItem(item.getClassName());
                smiChildren.setName(item.getPackageName()+"."+item.getClassName());
                smiChildren.addActionListener(this);
                mnuPossibleChildren.add(smiChildren);
            }

        return mnuPossibleChildren;
    }
}