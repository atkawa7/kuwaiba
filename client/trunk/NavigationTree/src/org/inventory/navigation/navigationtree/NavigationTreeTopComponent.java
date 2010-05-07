package org.inventory.navigation.navigationtree;

import java.awt.BorderLayout;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectChildren;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

@ConvertAsProperties(dtd = "-//org.inventory.navigation.navigationtree//NavigationTree//EN",
autostore = false)
public final class NavigationTreeTopComponent extends TopComponent
        implements ExplorerManager.Provider{

    //private static NavigationTreeTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/inventory/navigation/navigationtree/res/icon.png";
    static final String ROOT_ICON_PATH = "org/inventory/navigation/navigationtree/res/root.png";

    private final ExplorerManager em = new ExplorerManager();
    private NavigationTreeService nts;

    public NavigationTreeTopComponent() {
        initComponents();
        initComponentsCustom();
        setName(NbBundle.getMessage(NavigationTreeTopComponent.class, "CTL_NavigationTreeTopComponent"));
        setToolTipText(NbBundle.getMessage(NavigationTreeTopComponent.class, "HINT_NavigationTreeTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }



    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 466, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    /*
     * Adiciona e inicializa los componentes no creados mediante el editor de GUIs
     */
    public void initComponentsCustom(){
        ActionMap ac = this.getActionMap();
        //ac.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(em));
        //ac.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(em));
        //Asocia un lookup, un espacio para registrar los elementos
        //usar InstanceContent para lookups dinámicos, y ProxyLookup si se desea
        //presentar varios lookups en un mismo sitio
        ac.put(DefaultEditorKit.deleteNextCharAction, ExplorerUtils.actionDelete(em, true));
        nts = new NavigationTreeService(this);
        associateLookup(ExplorerUtils.createLookup(em, ac));
        setLayout(new BorderLayout());
        BeanTreeView treeView = new BeanTreeView();
        treeView.setWheelScrollingEnabled(true);
        //tm = new TreeManager();
        //AbstractNode root = new AbstractNode(tm);
        LocalObjectLight[] rootChildren = nts.getRootChildren();
        if (rootChildren != null){
            AbstractNode root = new AbstractNode(new ObjectChildren(rootChildren));
            root.setIconBaseWithExtension(ROOT_ICON_PATH);
            em.setRootContext(root);
            em.getRootContext().setDisplayName("Inicio");
            add(treeView,BorderLayout.CENTER);
        }
        //add(new PropertySheetView(),BorderLayout.SOUTH);
        
        //Le dice de manera dinámica al WindowManager que este componente irá
        //en el modo explorador (parte izquierda). Esto se hace aquí, ya que en
        //el layer.xml se ha quitado toda referencia a ese modo porque este módulo
        //ya no es singleton
        Mode myMode = WindowManager.getDefault().findMode("explorer");
        myMode.dockInto(this);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    /*public static synchronized NavigationTreeTopComponent getDefault() {
    if (instance == null) {
    instance = new NavigationTreeTopComponent();
    }
    return instance;
    }*/

    public static synchronized NavigationTreeTopComponent showComponent(){
       return new NavigationTreeTopComponent();
    }

    /**
     * Obtain the NavigationTreeTopComponent instance. Never call {@link #getDefault} directly!
     */
    /*public static synchronized NavigationTreeTopComponent findInstance() {
    TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
    if (win == null) {
    Logger.getLogger(NavigationTreeTopComponent.class.getName()).warning(
    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
    return getDefault();
    }
    if (win instanceof NavigationTreeTopComponent) {
    return (NavigationTreeTopComponent) win;
    }
    Logger.getLogger(NavigationTreeTopComponent.class.getName()).warning(
    "There seem to be multiple components with the '" + PREFERRED_ID
    + "' ID. That is a potential source of errors and unexpected behavior.");
    return getDefault();
    }*/

    //Debería guardar el estado de las cosas contenidas?
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    //El displayname del árbol
    @Override
    public void componentOpened() {
        setDisplayName("Árbol de Navegación");
    }

    @Override
    public void componentClosed() {
        
    }

    /*
     * IMPORTANT: Este par de métodos son bacanos porque sirven para congelar
     * el estado de un componente. Uno aquí usando el método "put" crea nuevas
     * propiedades, como nombre de la ventana, posición del scroll, etc, que
     * luego se leerán con "readProperties" y se restaurarán!
     */
    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    Object readProperties(java.util.Properties p) {
        return this;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    public ExplorerManager getExplorerManager() {
        return em;
    }
}
