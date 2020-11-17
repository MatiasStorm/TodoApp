package todoapp.app.models;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
public class TodoDAO {
    private JdbcTemplate jdbcTemplate;
    private final String createTableStatement = "CREATE TABLE IF NOT EXISTS `todo`"             // Create the table todo
            + "( `id` INT PRIMARY KEY AUTO_INCREMENT"         // Column 'id' is an auto incremented int and the primary key
            + ", `text` VARCHAR(100) "                        // todo text is a varialbe char with a max of 100 characters
            + ", `created` DATETIME DEFAULT CURRENT_TIMESTAMP"// created is a datetime with a default of current timestamp
            + ", `done` BOOLEAN DEFAULT false"                // done is a boolean which defaults to false
            + ")";
    private final String updateStatement = "UPDATE `todo` SET `text` = ?, `done` = ? where `id` = ?";
    private final String insertStatement = "INSERT INTO `todo` (`text`,`done`) VALUES ( ?, ?)";
    private final String selectStatement = "SELECT * FROM `todo`";


    @Autowired
    public TodoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTable() {
        jdbcTemplate.execute(createTableStatement);
    }

    public Todo insert(Todo t) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(insertStatement, new String[]{"id"});
            preparedStatement.setString(1, t.getText());
            preparedStatement.setBoolean(2, t.isDone());
            return preparedStatement;
        }, keyHolder);
        int todoId = keyHolder.getKey().intValue();
        return select(todoId);
    }

    public Todo select(int id) {
        String sqlStatement = selectStatement + " WHERE `id` = " + id;
        return jdbcTemplate.queryForObject(sqlStatement, (rs, rowNum) -> mapRowToObject(rs));
    }

    private Todo mapRowToObject(ResultSet rs) throws SQLException {
        Todo t = new Todo();
        t.setText(rs.getString("text"));
        t.setId(rs.getInt("id"));
        t.setCreated(rs.getTimestamp("created"));
        t.setDone(rs.getBoolean("done"));
        return t;
    }

     public boolean update(Todo t) {
         int result = jdbcTemplate.update(updateStatement, t.getText(), t.isDone(), t.getId());
         return result == 1;
     }

    public ArrayList<Todo> selectAll() {
        ArrayList<Todo> todos;
        try {
            todos = jdbcTemplate.queryForObject(selectStatement, (ResultSet rs, int rowNum) -> {
                ArrayList<Todo> ts = new ArrayList<>();
                do {
                    ts.add(mapRowToObject(rs));
                } while (rs.next());
                return ts;
            });
        } catch (EmptyResultDataAccessException e) {
            todos = new ArrayList<>();
        }
        return todos;
    }
}
