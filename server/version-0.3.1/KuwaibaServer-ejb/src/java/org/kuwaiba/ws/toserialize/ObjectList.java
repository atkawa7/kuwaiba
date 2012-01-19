package org.kuwaiba.ws.toserialize;

import org.kuwaiba.entity.multiple.GenericObjectList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This class represents a list type attribute (packing many list items)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectList {
    private String className;
    private HashMap<Long,String> list; //Or maybe RemoteObjects?

    public ObjectList() {
    }

    public ObjectList(String _className, List<GenericObjectList> _list){
        this.className = _className;
        this.list = new HashMap<Long, String>();
        String displayName;
        for (GenericObjectList item : _list)
            this.list.put(item.getId(), (item.getDisplayName() == null)?
                                            item.getName():item.getDisplayName());
    }
}
