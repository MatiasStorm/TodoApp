package todoapp.app.models;

import todoapp.app.dbcontext.DatabaseField;

import java.util.Date;

public class Todo {
    @DatabaseField(dataType = "INT PRIMARY KEY AUTO_INCREMENT", create=false, edit = false, primaryKey = true)
    private int id;

    @DatabaseField(dataType = "VARCHAR(100)")
    private String text;

    @DatabaseField(dataType = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Date created;

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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
