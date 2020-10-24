package todoapp.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import todoapp.app.annotations.DatabaseField;
import todoapp.app.models.Todo;

import java.lang.reflect.Field;
import java.util.ArrayList;

@Component
public class TodoService {
    ArrayList<Todo> todos = new ArrayList<>();
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TodoService(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
        createTable();
    }

    private void createTable(){
        String sqlStatement = "CREATE TABLE IF NOT EXISTS `" + Todo.class.getSimpleName() + "` (";
        Class<Todo> todoModelClass = Todo.class;
        for(Field field : todoModelClass.getDeclaredFields()){
            if(field.isAnnotationPresent(DatabaseField.class)){
                DatabaseField annotation = field.getAnnotation(DatabaseField.class);
                sqlStatement += String.format(" `%s` %s,", field.getName(), annotation.dataType());
            }
        }
        jdbcTemplate.execute(sqlStatement.substring(0, sqlStatement.length() - 1) + ")");
    }

    public ArrayList<Todo> getTodos(){
        return todos;
    }
}
