package todoapp.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import todoapp.app.annotations.DatabaseField;
import todoapp.app.models.Todo;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
public class TodoService {
    ArrayList<Todo> todos = new ArrayList<>();
    private final String tableName = String.format("`%s`", Todo.class.getSimpleName());
    private String createTableStatement = String.format("CREATE TABLE IF NOT EXISTS %s (?, ?, ?)", tableName );
    private String insertStatement = String.format("INSERT INTO %s (text) VALUES(?)", tableName);
    private String selectStatement = String.format("SELECT * FROM %s", tableName);
    private String selectLastIdStatement = "SELECT LAST_INSERT_ID()";
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

    public void addTodo(Todo todo){
        jdbcTemplate.update(insertStatement, todo.getText());
        int todoId = jdbcTemplate.queryForObject(selectLastIdStatement, Integer.class);
        Todo dbTodo = readTodoFromDb(todoId);
        todos.add(dbTodo);
    }

    private Todo readTodoFromDb(int id){
        String sqlStatement = selectStatement + " WHERE id = " + id;
        Todo todo =jdbcTemplate.queryForObject(sqlStatement, (rs, rowNum) ->
                new Todo(
                        rs.getInt("id"),
                        rs.getString("text"),
                        rs.getBoolean("done")
                ));
        return todo;
    }
}
