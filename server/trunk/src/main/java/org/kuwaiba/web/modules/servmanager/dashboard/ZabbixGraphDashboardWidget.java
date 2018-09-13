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

package org.kuwaiba.web.modules.servmanager.dashboard;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * This widget embeds an image generated by a Zabbix graph. This widget assumes that the graph is named after the selected service .
 * The graph name must have the following structure: ANY_STRING:SERVICE_NAME or simply SERVICE_NAME
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ZabbixGraphDashboardWidget extends AbstractDashboardWidget {
    /**
     * Reference to the selected service
     */
    private RemoteObjectLight service;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    
    public ZabbixGraphDashboardWidget(RemoteObjectLight service, WebserviceBean wsBean) {
        super(String.format("Zabbix Graphs for %s", service));
        this.service = service;
        this.wsBean = wsBean;
        this.createContent();
    }
   
    @Override
    public void createContent() { 
        addComponents(new Label(String.format("<h2>%s</h2>", service), ContentMode.HTML));
        //Login
        try {
            String authResponse = createHttpPostRequestForTextResponse("http://localhost/zabbix/api_jsonrpc.php", "{\n" +
            "    \"jsonrpc\": \"2.0\",\n" +
            "    \"method\": \"user.login\",\n" +
            "    \"params\": {\n" +
            "        \"user\": \"Admin\",\n" +
            "        \"password\": \"zabbix\"\n" +
            "    },\n" +
            "    \"id\": 1,\n" +
            "    \"auth\": null\n" +
            "}");
            
            JsonReader authReader = Json.createReader(new StringReader(authResponse));
            JsonObject authObject = authReader.readObject();
            
            String authKey = authObject.getString("result");
            
            
            String graphResponse = createHttpPostRequestForTextResponse("http://localhost/zabbix/api_jsonrpc.php", "{\n" +
            "    \"jsonrpc\": \"2.0\",\n" +
            "    \"method\": \"graph.get\",\n" +
            "    \"params\": {\n" +
            "        \"output\": \"extend\"\n" +
            "    },\n" +
            "    \"auth\": \"" + authKey + "\",\n" +
            "    \"id\": 1\n" +
            "}");
            
            
            ComboBox<ZabbixGraph> cmbGraphs = new ComboBox<>();
            cmbGraphs.setWidth(20, Unit.PERCENTAGE);
            cmbGraphs.setEmptySelectionCaption("Zabbix graphs for this service");
            
            JsonReader graphReader = Json.createReader(new StringReader(graphResponse));
            JsonObject graphObject = graphReader.readObject();
            
            JsonArray jsonGraphs = graphObject.getJsonArray("result");
            List<ZabbixGraph> zabbixGraphs = new ArrayList<>();
            
            for (JsonValue jsonGraph : jsonGraphs) {
                String[] graphNameTokens = ((JsonObject)jsonGraph).getString("name").split(":");
                
                if (service.getName().equals(graphNameTokens[graphNameTokens.length - 1]))
                    zabbixGraphs.add(new ZabbixGraph(((JsonObject)jsonGraph).getString("graphid"), ((JsonObject)jsonGraph).getString("name")));
            }
            if (zabbixGraphs.isEmpty())
                addComponent(new Label("This service does not have Zabbix graphs associated to it. If this is not the case, please check your naming conventions or contact an administrator"));
            else {
                cmbGraphs.setItems(zabbixGraphs);
                addComponent(cmbGraphs);
                
                cmbGraphs.addSelectionListener((event) -> {
                    
                    if (!cmbGraphs.getSelectedItem().isPresent())
                        return;
                    
                    Window wdwGraphs = new Window(cmbGraphs.getSelectedItem().get().toString());
                    
                    VerticalLayout lytGraph = new VerticalLayout();
                    lytGraph.setSizeUndefined();
                    
                    Button btnRefresh = new Button("Refresh", VaadinIcons.REFRESH);
                    Panel pnlZabbixGraph = new Panel(new Image(cmbGraphs.getSelectedItem().get().toString(), 
                            new ExternalResource("http://localhost/zabbix/chart2.php?graphid=" + cmbGraphs.getSelectedItem().get().id + 
                                    "&period=3600&stime=20180913084803&isNow=1&profileIdx=web.graphs&profileIdx2=798&width=1194&screenid=")));
                    
                    lytGraph.addComponents(btnRefresh, pnlZabbixGraph);
                    
                    lytGraph.setExpandRatio(btnRefresh, 1);
                    btnRefresh.addClickListener((e) -> {
                        pnlZabbixGraph.setContent(new Image(cmbGraphs.getSelectedItem().get().toString(), 
                            new ExternalResource("http://localhost/zabbix/chart2.php?graphid=" + cmbGraphs.getSelectedItem().get().id + 
                                    "&period=3600&stime=20180913084803&isNow=1&profileIdx=web.graphs&profileIdx2=798&width=1194&screenid=")));
                    });
                    
                    wdwGraphs.setModal(true);
                    wdwGraphs.setHeight(80, Unit.PERCENTAGE);
                    wdwGraphs.setWidth(100, Unit.PERCENTAGE);
                    wdwGraphs.setContent(lytGraph);
                    
                    UI.getCurrent().addWindow(wdwGraphs);
                    
                });
                
            }
            //Logout
            createHttpPostRequestForTextResponse("http://localhost/zabbix/api_jsonrpc.php", "{\n" +
                    "    \"jsonrpc\": \"2.0\",\n" +
                    "    \"method\": \"user.logout\",\n" +
                    "    \"params\": [],\n" +
                    "    \"id\": 1,\n" +
                    "    \"auth\": \"" + authKey +  " \"\n" +
                    "}");
            
            
        } catch (InvalidArgumentException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
    }
    
    /**
     * Creates an HTTP requests and expects a text-based response
     * @param url Target URL
     * @return The response as a text
     * @throws InvalidArgumentException If there's an error while contacting the server 
     */
    private String createHttpPostRequestForTextResponse(String url, String content) throws InvalidArgumentException {
        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json-rpc");
            connection.setDoOutput(true);
            
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
            outputStreamWriter.write(content);
            outputStreamWriter.flush();
            
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { //HTTP OK
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null)
                        response.append(inputLine);
                    
                    return response.toString();
                }
            } else 
                throw new InvalidArgumentException(String.format("The Zabbix server returned an error code %s. Contact your administrator", responseCode));
        } catch (IOException ex) {
            throw new InvalidArgumentException(String.format("An unexpected error occurred: %s", ex.getLocalizedMessage()));
        }
    }

    /**
     * A simple POJO that represents a Zabbix graph and that can be used in lists and combo boxes
     */
    private class ZabbixGraph {
        /**
         *  Graph id
         */
        private String id;
        /**
         * Graph name
         */
        private String name;

        public ZabbixGraph(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        @Override
        public String toString(){
            return name;
        }
    }
}
