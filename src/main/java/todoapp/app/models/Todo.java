package todoapp.app.models;

import todoapp.app.annotations.DatabaseField;

public class Todo {
    @DatabaseField(dataType = "VARCHAR(100)")
    private String text;

    @DatabaseField(dataType = "BOOLEAN")
    private boolean done;
}
