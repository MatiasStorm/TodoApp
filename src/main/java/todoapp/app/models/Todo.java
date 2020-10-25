package todoapp.app.models;

import todoapp.app.dbcontext.DatabaseField;

public class Todo {
    @DatabaseField(dataType = "INT PRIMARY KEY AUTO_INCREMENT", create=false, edit = false)
    private int id;

    @DatabaseField(dataType = "VARCHAR(100)")
    private String text;

    @DatabaseField(dataType = "BOOLEAN DEFAULT false")
    private boolean done;

    public Todo(){}

    public Todo(String text) {
        this.text = text;
    }

    public Todo(int id, String text, boolean done){
        this.id = id;
        this.text = text;
        this.done = done;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
