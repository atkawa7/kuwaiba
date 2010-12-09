package core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this class as intended to be used only for administrative purposes,
 * this is, it won't be available for customizing. Not all classes marked as
 * Administrative are metadata. I.e., UserSession
 * It's also used to decorated those fields that shouldn't be shown in the property window
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME) //Don't discard this annotation after a compilation
                                    //In fact, we need it at runtime
public @interface Administrative {

}
