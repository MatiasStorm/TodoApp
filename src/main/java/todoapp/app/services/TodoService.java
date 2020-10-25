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
    ArrayList<Todo> todos = new ArrayList<>();
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
        dbContext.createTableIfNotExists(Todo.class);
        todos = dbContext.selectAll(Todo.class);
    }

    public ArrayList<Todo> getTodos(){
        return todos;
    }

    public void addTodo(Todo todo) throws IllegalAccessException {
        Todo t = (Todo) dbContext.addRow(todo);
        if(t == null) return;
        todos.add(t);
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
