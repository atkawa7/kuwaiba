/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package businesslogic;

import core.todeserialize.ObjectUpdate;
import core.toserialize.ClassInfo;
import core.toserialize.ClassInfoLight;
import core.toserialize.RemoteObjectLight;
import core.toserialize.RemoteTreeNode;
import javax.ejb.Remote;

/**
 * Interface exposing the methods within BackEndbean
 *
 * @author Charles Edward Bedón Cortázar <charles.bedon@zoho.com>
 */
@Remote
public interface BackendBeanRemote {

    public void createInitialDataset(); //just for testing purposes
    
    public Long getDummyRootId();
    public Class getDummyRootClass();
    public RemoteTreeNode getObjectInmediateHierarchy(java.lang.Long oid, String objectClass);
    public core.toserialize.RemoteObject getObjectInfo(java.lang.String objectClass, java.lang.Long oid);
    public boolean updateObject(ObjectUpdate obj) throws core.exceptions.ObjectNotFoundException;
    public java.lang.String getError();
    public boolean setObjectLock(java.lang.Long oid, java.lang.String objectClass, java.lang.Boolean value);
    public RemoteObjectLight[] getObjectChildren(Long oid, String objectClass);
    public RemoteObjectLight createObject(String objectClass, Long parentOid, String template);
    public ClassInfo[] getMetadata();
    public ClassInfo getMetadataForClass(String className);
    public void buildMetaModel();
    public core.toserialize.ObjectList getMultipleChoice(java.lang.String className);
    public Boolean addPossibleChildren(Long parentClassId, Long[] possibleChildren);
    public Boolean removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved);
    public boolean removeObject(Class className, Long oid);
    public ClassInfoLight[] getPossibleChildren(java.lang.Class parentClass);
    public ClassInfoLight[] getRootPossibleChildren();
    public ClassInfoLight[] getLightMetadata();
    
}
