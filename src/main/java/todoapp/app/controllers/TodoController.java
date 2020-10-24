package todoapp.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import todoapp.app.services.TodoService;

@Controller
public class TodoController {
    TodoService todoService;

    public TodoController(TodoService todoService){
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String index(Model model){
        model.addAttribute("todos", todoService.getTodos());
        todoService.getTodos();
        return "index";
    }

}
