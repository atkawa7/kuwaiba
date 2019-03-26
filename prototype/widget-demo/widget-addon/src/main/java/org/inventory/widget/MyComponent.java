package org.inventory.widget;

import com.vaadin.ui.Notification;
import org.inventory.widget.client.MyComponentClientRpc;
import org.inventory.widget.client.MyComponentServerRpc;
import org.inventory.widget.client.MyComponentState;


// This is the server-side UI component that provides public API 
// for MyComponent
public class MyComponent extends com.vaadin.ui.AbstractComponent {
    private String text;

    public MyComponent() {

        // To receive events from the client, we register ServerRpc
        MyComponentServerRpc rpc = this::handleClick;
        registerRpc(rpc);
    }
    
    public void setText(String text) {
        this.text = text;
        getRpcProxy(MyComponentClientRpc.class).alert(text);
    }
    
    public String getText() {
        return text;
    }
    
    // We must override getState() to cast the state to MyComponentState
    @Override
    protected MyComponentState getState() {
        return (MyComponentState) super.getState();
    }
    
    private void handleClick(String newText) {
        Notification.show("Old text " + text, Notification.Type.TRAY_NOTIFICATION);
        Notification.show("New text " + newText, Notification.Type.TRAY_NOTIFICATION);
        Notification.show("The label text changed from the client (browser)", Notification.Type.TRAY_NOTIFICATION);
        this.text = newText;
    }
}
