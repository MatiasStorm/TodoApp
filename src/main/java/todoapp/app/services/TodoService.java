package todoapp.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import todoapp.app.dbcontext.DatabaseContext;
import todoapp.app.models.Todo;

import java.util.ArrayList;

@Component
public class TodoService {
    ArrayList<Todo> todos;
    private final DatabaseContext dbContext;

    @Autowired
    public TodoService(DatabaseContext dbContext){
        this.dbContext = dbContext;
        dbContext.createTableIfNotExists(Todo.class);
        todos = dbContext.selectAll(Todo.class);
    }

    public ArrayList<Todo> getTodos(){
        return todos;
    }

    public void addTodo(Todo todo) throws IllegalAccessException {
        Todo t = dbContext.insert(todo);
        if(t == null) return;
        todos.add(t);
    }

    public void updateTodo(Todo todo) throws IllegalAccessException {
        dbContext.update(todo);
        todos.removeIf(t -> t.getId() == todo.getId());
        todos.add(todo);
    }

    public void deleteTodo(Todo todo){
    }
}
