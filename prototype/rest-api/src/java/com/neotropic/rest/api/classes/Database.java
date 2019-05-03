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
package com.neotropic.rest.api.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the records in memory
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class Database{
        
    private List<SimpleObject> data;
    public static Database db;
    
    private Database() {
        this.data = new ArrayList<>();
        data.add(new SimpleObject("1", "uno"));
        data.add(new SimpleObject("2", "dos"));
    }
    
    public static Database getInstance(){
        if(db == null)
            db = new Database();
        return db;
    }
    
    public SimpleObject find(SimpleObject obj){
        for (int i = 0; i < 10; i++) {
            if(data.get(i).equals((obj)))
                return data.get(i);
        }
        return null;
    }
    
    public boolean persist(SimpleObject entity){
        if (data.contains(entity))
            return false;
        return data.add(entity);
    }
    
    public boolean merge(SimpleObject entity){
        for (int i = 0; i < 10; i++) {
            if(data.get(i).equals((entity))){
                data.set(i, entity);
                return true;
            }
        }
        return false;
    }
    
    public boolean remove(SimpleObject entity){
        return data.remove(entity);
    }
    
    public List<SimpleObject> findAll(){
        return data;
    }
    
    public int count(){
        return data.size();
    }

    public SimpleObject findById(String id) {
        for (int i = 0; i < 10; i++) {
            if(data.get(i).getId().equals((id)))
                return data.get(i);
        }
        return null;
    }
    
}
