package todoapp.app.dbcontext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DatabaseField {
    public String dataType() default "";
    public boolean edit() default true;
    public boolean create() default true;
}
