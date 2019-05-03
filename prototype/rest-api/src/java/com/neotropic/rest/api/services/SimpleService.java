/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.rest.api.services;

import com.neotropic.rest.api.classes.Database;
import com.neotropic.rest.api.classes.SimpleObject;
import java.util.List;
import javax.ejb.Stateless;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * A simple REST-API service that updates a list of simple object loaded in memory
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Stateless
@Path("/objects")
public class SimpleService{
    
    private Database db = Database.getInstance();
    
    public SimpleService() {}

    //read with filter
    //http://localhost:8080/rest-api/objects/search/1/uno
    @GET
    @Path("/search/{id}/{name}")
    @Produces({MediaType.APPLICATION_JSON})
    public SimpleObject getObject(@PathParam("id") String id, @PathParam("name") String name) {
        return db.find(new SimpleObject(id, name));
    }
    
    //reads all
    //http://localhost:8080/RESTDemo/rest-api/objects
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SimpleObject> getObjects() {
        return db.findAll();	
    }

    //update by id and name as parameter
    //QueryParam, are parameters in url
    //http://localhost:8080/RESTDemo/rest-api/objects/upd/1?name=one
    @PUT
    @Path("/upd/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String edit(
            @PathParam("id") String id,
            @QueryParam("name") String name) {
        
        SimpleObject oldObj = db.findById(id);
        
        if (db.merge(new SimpleObject(id, name)))
            return String.format("name update from: %s to: %s", oldObj.getName(), name);
        else
            return "nothing updated";
    }
    
    //create one
    //http://localhost:8080/RESTDemo/rest-api/objects/add/1/one
    @POST
    @Path("/add/{id}/{name}")
    @Produces(MediaType.APPLICATION_JSON)		
    public boolean anyNamecanBeUsedInTheseMethods(
            @PathParam("id") String id, 
            @PathParam("name") String name) 
    {
        return db.persist(new SimpleObject(id, name));
    }
    
    //Deletes by id
    //http://localhost:8080/RESTDemo/rest-api/rm/1/one
    @DELETE @Path("/rm/{id}/{name}") @Produces(MediaType.APPLICATION_JSON)
    public boolean detele(@PathParam("id") String id, @PathParam("name") String name) {
        return db.remove(new SimpleObject(id, name));
    }
    
}
