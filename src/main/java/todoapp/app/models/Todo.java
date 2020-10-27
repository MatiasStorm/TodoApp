package todoapp.app.models;

import java.util.Date;

public class Todo {
    private int id;
    private String text;
    private Date created;
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
