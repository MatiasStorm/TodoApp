package todoapp.app.models;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
public class TodoDAO {
    private JdbcTemplate jdbcTemplate;
    private final String createTableStatement = "CREATE TABLE IF NOT EXISTS `todo`"
                                              + "( `id` INT PRIMARY KEY AUTO_INCREMENT"
                                              + ", `text` VARCHAR(100) "
                                              + ", `created` DATETIME DEFAULT CURRENT_TIMESTAMP"
                                              + ", `done` BOOLEAN DEFAULT false"
                                              + ")";
    private final String updateStatement = "UPDATE `todo` SET `text` = ?, `done` = ? where `id` = ?";
    private final String insertStatement = "INSERT INTO `todo` (`text`,`done`) VALUES ( ?, ?)";
    private final String selectStatement = "SELECT * FROM `todo`";


    @Autowired
    public TodoDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTable(){
        jdbcTemplate.execute(createTableStatement);
    }

    public Todo insert(Todo t) {
        jdbcTemplate.update(insertStatement, t.getText(), t.isDone());
        Integer id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return select(id);
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

    public ArrayList<Todo> selectAll(){
        ArrayList<Todo> todos;
        try{
            todos = jdbcTemplate.queryForObject(selectStatement, (rs, rowNum) -> {
                ArrayList<Todo> ts = new ArrayList<>();
                while(rs.next()){
                    ts.add(mapRowToObject(rs));
                }
                return ts;
            });
        }
        catch (EmptyResultDataAccessException e) {
            todos = new ArrayList<>();
        }
        return todos;
    }
}
