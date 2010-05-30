package core.toserialize;

import entity.multiple.GenericObjectList;
import java.util.Hashtable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This class represents a list type attribute (pacckeing many list items)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectList {
    private String className;
    private Hashtable<Long,String> list; //Or maybe RemoteObjects?

    public ObjectList() {
    }

    public ObjectList(String _className, List<GenericObjectList> _list){
        this.className = _className;
        this.list = new Hashtable<Long, String>();
        for (GenericObjectList item : _list)
            this.list.put(item.getId(), (item.getDisplayName() == null)?
                                            item.getName():item.getDisplayName());
    }
}
