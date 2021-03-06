package org.kuwaiba.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark some classes as dummy, it is to say, classes useful only for special and particular purposes.
 * They're not part of the data model itself like DummyRoot. Usually these dummy classes have only one instance
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME) //Don't discard this annotation afeter compilation
                                    //In fact, we need it at runtime
public @interface Dummy {

}
