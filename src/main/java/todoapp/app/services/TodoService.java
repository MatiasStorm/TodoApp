package todoapp.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import todoapp.app.models.TodoDAO;
import todoapp.app.models.Todo;

import java.util.ArrayList;

@Component
public class TodoService {
    ArrayList<Todo> todos;
    private final TodoDAO todoDao;

    @Autowired
    public TodoService(TodoDAO todoDao) {
        this.todoDao = todoDao;
        todoDao.createTable();
        todos = todoDao.selectAll();
    }

    public ArrayList<Todo> getTodos() {
        return todos;
    }

    public void addTodo(Todo todo) {
        Todo t = todoDao.insert(todo);
        if (t == null) return;
        todos.add(t);
    }

    public void updateTodo(Todo todo) {
        todoDao.update(todo);
        todos.removeIf(t -> t.getId() == todo.getId());
    }
}
