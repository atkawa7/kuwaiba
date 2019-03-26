package org.inventory.widget.client;

import org.inventory.widget.MyComponent;

import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

// Connector binds client-side widget class to server-side component class
// Connector lives in the client and the @Connect annotation specifies the
// corresponding server-side component
@Connect(MyComponent.class)
public class MyComponentConnector extends AbstractComponentConnector implements MyComponentClientRpc {

    // ServerRpc is used to send events to server. Communication implementation
    // is automatically created here
    MyComponentServerRpc rpc = RpcProxy.create(MyComponentServerRpc.class, this);

    public MyComponentConnector() {
        
        // To receive RPC events from server, we register ClientRpc implementation 
        registerRpc(MyComponentClientRpc.class, this);

        // We choose listed for mouse clicks for the widget
        getWidget().addClickHandler(event -> {
                browserLog("Fixing label text to New Text");
                String newText = "New Text";
                alert(newText);
                rpc.clicked(newText);
            }
        );

    }
    
    public final native void browserLog(Object obj) /*-{
        $wnd.console.log(obj);
    }-*/;

    // We must implement getWidget() to cast to correct type 
    // (this will automatically create the correct widget type)
    @Override
    public MyComponentWidget getWidget() {
        return (MyComponentWidget) super.getWidget();
    }

    // We must implement getState() to cast to correct type
    @Override
    public MyComponentState getState() {
        return (MyComponentState) super.getState();
    }

    // Whenever the state changes in the server-side, this method is called
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        // State is directly readable in the client after it is set in server
    }

    @Override
    public void alert(String message) {
        getWidget().setText(message);
    }
}
