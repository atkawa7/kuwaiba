/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Esta anotación sirve para marcar clases como de metadata y que no es necesario meterlas en lo
 * que se manda a los clientes para que administren su información
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME) //No descarte la anotación después de la compilación
                                    //ya que precisamente requerimos esa anotación en runtime
public @interface Metadata {

}
