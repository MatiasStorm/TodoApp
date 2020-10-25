package todoapp.app.dbcontext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import todoapp.app.annotations.DatabaseField;

import java.lang.reflect.Field;

@Component
public class DatabaseContext {
    JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseContext(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public <T> boolean createTableIfNotExists(Class<T> cls){
        String sqlStatement = "CREATE TABLE IF NOT EXISTS `" + cls.getSimpleName() + "` (";
        for(Field field : cls.getDeclaredFields()){
            if(field.isAnnotationPresent(DatabaseField.class)){
                DatabaseField annotation = field.getAnnotation(DatabaseField.class);
                sqlStatement += String.format(" `%s` %s,", field.getName(), annotation.dataType());
            }
        }
        jdbcTemplate.execute(sqlStatement.substring(0, sqlStatement.length() - 1) + ")");
        return true;
    }
}
