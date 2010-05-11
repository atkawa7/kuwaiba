package org.inventory.customization.hierarchycustomizer;

import java.awt.BorderLayout;
import java.awt.dnd.DragSource;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import org.inventory.customization.hierarchycustomizer.nodes.ClassMetadataChildren;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;


/**
 * Represents the GUI for customizing the container hierarchy
 */
@ConvertAsProperties(dtd = "-//org.inventory.customization.hierarchycustomizer//HierarchyCustomizer//EN",
autostore = false)
public final class HierarchyCustomizerTopComponent extends TopComponent
    implements ExplorerManager.Provider{

    private static HierarchyCustomizerTopComponent instance;
    static final String ICON_PATH = "org/inventory/customization/hierarchycustomizer/res/icon.png";
    private static final String PREFERRED_ID = "HierarchyCustomizerTopComponent";
    private final ExplorerManager em = new ExplorerManager();
    private HierarchyCustomizerService hml;

    public HierarchyCustomizerTopComponent() {
        initComponents();
        initComponentsCustom();
        setName(NbBundle.getMessage(HierarchyCustomizerTopComponent.class, "CTL_HierarchyCustomizerTopComponent"));
        setToolTipText(NbBundle.getMessage(HierarchyCustomizerTopComponent.class, "HINT_HierarchyCustomizerTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

    private void initComponentsCustom() {
        associateLookup(ExplorerUtils.createLookup(em, new ActionMap()));

        hml = new HierarchyCustomizerService(this);

        BeanTreeView bTreeView = new BeanTreeView();
        JList lstClasses = new JList();

        em.setRootContext(new AbstractNode(new ClassMetadataChildren(hml.getAllMeta())));

        bTreeView.setRootVisible(false);

        pnlLeft.add(bTreeView,BorderLayout.CENTER);

        lstClasses.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstClasses.setDragEnabled(true);

        DragSource.getDefaultDragSource().addDragSourceListener(hml);

        pnlHierarchyManagerScrollMain.setViewportView(lstClasses);

    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlHierarchyManagerMain = new javax.swing.JSplitPane();
        pnlLeft = new javax.swing.JPanel();
        pnlRight = new javax.swing.JPanel();
        pnlHierarchyManagerScrollMain = new javax.swing.JScrollPane();
        lblInfo = new javax.swing.JLabel();

        pnlHierarchyManagerMain.setOneTouchExpandable(true);

        pnlLeft.setLayout(new java.awt.BorderLayout());
        pnlHierarchyManagerMain.setLeftComponent(pnlLeft);

        pnlRight.setLayout(new java.awt.BorderLayout());
        pnlRight.add(pnlHierarchyManagerScrollMain, java.awt.BorderLayout.CENTER);

        pnlHierarchyManagerMain.setRightComponent(pnlRight);

        org.openide.awt.Mnemonics.setLocalizedText(lblInfo, org.openide.util.NbBundle.getMessage(HierarchyCustomizerTopComponent.class, "HierarchyCustomizerTopComponent.lblInfo.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlHierarchyManagerMain, javax.swing.GroupLayout.DEFAULT_SIZE, 914, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblInfo)
                        .addContainerGap(20, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlHierarchyManagerMain, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblInfo;
    private javax.swing.JSplitPane pnlHierarchyManagerMain;
    private javax.swing.JScrollPane pnlHierarchyManagerScrollMain;
    private javax.swing.JPanel pnlLeft;
    private javax.swing.JPanel pnlRight;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized HierarchyCustomizerTopComponent getDefault() {
        if (instance == null) {
            instance = new HierarchyCustomizerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the HierarchyCustomizerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HierarchyCustomizerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(HierarchyCustomizerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof HierarchyCustomizerTopComponent) {
            return (HierarchyCustomizerTopComponent) win;
        }
        Logger.getLogger(HierarchyCustomizerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        
    }

    @Override
    public void componentClosed() {

    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public ExplorerManager getExplorerManager() {
        return em;
    }
}
