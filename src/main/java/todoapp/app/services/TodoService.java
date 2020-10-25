package todoapp.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import todoapp.app.dbcontext.DatabaseField;
import todoapp.app.dbcontext.DatabaseContext;
import todoapp.app.models.Todo;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
public class TodoService {
    ArrayList<Todo> todos;
    private final String tableName = String.format("`%s`", Todo.class.getSimpleName());
    private String insertStatement = String.format("INSERT INTO %s (text) VALUES(?)", tableName);
    private String selectStatement = String.format("SELECT * FROM %s", tableName);
    private String updateStatement = String.format("UPDATE %s SET text = ?, done = ? where id = ?", tableName);
    private String selectLastIdStatement = "SELECT LAST_INSERT_ID()";
    private final JdbcTemplate jdbcTemplate;
    private final DatabaseContext dbContext;

    @Autowired
    public TodoService(JdbcTemplate jdbcTemplate, DatabaseContext dbContext){
        this.jdbcTemplate = jdbcTemplate;
        this.dbContext = dbContext;
//        createTable();
        dbContext.createTableIfNotExists(Todo.class);
        loadTodos();
    }

    private void loadTodos(){
        try {
            todos = jdbcTemplate.queryForObject(selectStatement, (rs, rowNum) -> {
                ArrayList<Todo> loadedTodos = new ArrayList<>();
                while(rs.next()){
                    loadedTodos.add(readRow(rs));
                }
                return loadedTodos;
            });
        }
        catch (EmptyResultDataAccessException e) {
            this.todos = new ArrayList<>();
        }
    }

    private Todo readRow(ResultSet rs) throws SQLException {
        Todo t = new Todo();
        for(Field field : Todo.class.getDeclaredFields() ){
            if(field.isAnnotationPresent(DatabaseField.class)){
                String fieldName = field.getName();
                field.setAccessible(true);

                try {
                    String fieldClass = field.getType().getSimpleName();
                    if(fieldClass.equals("String")){
                        field.set(t, rs.getString(fieldName));
                    }
                    else if(fieldClass.equals( "int" )){
                        field.set(t, rs.getInt(fieldName));
                    }
                    else if(fieldClass.equals( "float" )){
                        field.set(t, rs.getFloat(fieldName));
                    }
                    else if(fieldClass.equals( "double" )){
                        field.set(t, rs.getDouble(fieldName));
                    }
                    else if(fieldClass.equals( "boolean" )){
                        field.set(t, rs.getBoolean(fieldName));
                    }
                }
                catch (IllegalAccessException e){
                    System.out.println(e.getMessage());
                }

            }
        }
        return t;
    }

    public ArrayList<Todo> getTodos(){
        return todos;
    }

    public void addTodo(Todo todo){
        jdbcTemplate.update(insertStatement, todo.getText());
        int todoId = jdbcTemplate.queryForObject(selectLastIdStatement, Integer.class);
        Todo dbTodo = selectTodoById(todoId);
        todos.add(dbTodo);
    }

    private Todo selectTodoById(int id){
        String sqlStatement = selectStatement + " WHERE id = " + id;
        Todo todo = jdbcTemplate.queryForObject(sqlStatement, (rs, rowNum) -> (readRow(rs)));
        return todo;
    }

    public void updateTodo(Todo todo){
        jdbcTemplate.update(updateStatement,  todo.getText(), todo.isDone(), todo.getId());
        for(int i = todos.size() - 1; i >= 0; i--){
            if(todos.get(i).getId() == todo.getId()){
                todos.remove(i);
                break;
            }
        }
        todos.add(todo);
    }

    public void deleteTodo(Todo todo){
    }
}
