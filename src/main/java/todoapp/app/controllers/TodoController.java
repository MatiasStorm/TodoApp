package todoapp.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import todoapp.app.models.Todo;
import todoapp.app.services.TodoService;

@Controller
public class TodoController {
    TodoService todoService;

    public TodoController(TodoService todoService){
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String index(Model model, Todo todo){
        model.addAttribute("todos", todoService.getTodos());
        return "index";
    }

    @PostMapping("/createTodo")
    public String createTodo(Todo todo) throws IllegalAccessException {
        todoService.addTodo(todo);
        return "redirect:/";
    }

    @PostMapping("/updateTodo")
    public String updateTodo(Todo todo) throws IllegalAccessException {
        todoService.updateTodo(todo);
        return "redirect:/";
    }

    @PostMapping("/deleteTodo")
    public String deleteTodo(Todo todo){
        todoService.deleteTodo(todo);
        return "redirect:/";
    }
}
