/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inventory.core.services.interfaces;

import java.util.HashMap;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalObject {
    public HashMap<String,Object> getAttributes();
    public String getClassName();
    public Long getOid();
    public Object getAttribute(String name);
    public LocalClassMetadata getObjectMetadata();
    public void setLocalObject(String className, String[] attributes, Object[] values);
    public void setObjectMetadata(LocalClassMetadata metaForClass);
    public void setOid(Long oid);
}
