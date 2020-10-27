package todoapp.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import todoapp.app.models.TodoDAO;
import todoapp.app.models.Todo;

import java.util.ArrayList;

@Component
public class TodoService {
    ArrayList<Todo> todos;
    private final TodoDAO dbContext;

    @Autowired
    public TodoService(TodoDAO dbContext){
        this.dbContext = dbContext;
        dbContext.createTable();
        todos = dbContext.selectAll();
    }

    public ArrayList<Todo> getTodos(){
        return todos;
    }

    public void addTodo(Todo todo) {
        Todo t = dbContext.insert(todo);
        if(t == null) return;
        todos.add(t);
    }

    public void updateTodo(Todo todo){
        dbContext.update(todo);
        todos.removeIf(t -> t.getId() == todo.getId());
        todos.add(todo);
    }
}
