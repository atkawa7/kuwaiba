package core.toserialize;

import entity.core.metamodel.ClassMetadata;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Same as ClassInfo, but lighter, since it's intended to provide the information to
 * render a node in a view (usually a tree) at client side.
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassInfoLight {
    protected Long id;
    protected Boolean isAbstract;
    protected String className;
    protected String _package;

    public ClassInfoLight(){}

    public ClassInfoLight(Long _id, Boolean _isAbstract ,String _className, String _packageName){
        this.id = _id;
        this.isAbstract = _isAbstract;
        this.className = _className;
        this._package = _packageName;
    }

    public ClassInfoLight(ClassMetadata cm) {
        this.id = cm.getId();
        this.isAbstract = cm.getIsAbstract();
        this.className = cm.getName();
        this._package = cm.getPackageInfo().getName();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

        public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackage() {
        return _package;
    }

    public void setPackage(String packageName) {
        this._package = packageName;
    }
}
