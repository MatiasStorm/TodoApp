package todoapp.app.dbcontext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class DatabaseContext {
    private JdbcTemplate jdbcTemplate;
    private final String createTableStatement = "CREATE TABLE IF NOT EXISTS `%s`";
    private final String updateStatement = "UPDATE `%s` SET";
    private final String insertStatement = "INSERT INTO `%s`";
    private final String selectStatement = "SELECT * FROM `%s`";


    @Autowired
    public DatabaseContext(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public <T> boolean createTableIfNotExists(Class<T> cls){
        String sqlStatement = String.format(createTableStatement, cls.getSimpleName()) + " (";
        for(Field field : cls.getDeclaredFields()){
            if(field.isAnnotationPresent(DatabaseField.class)){
                DatabaseField annotation = field.getAnnotation(DatabaseField.class);
                sqlStatement += String.format(" `%s` %s,", field.getName(), annotation.dataType());
            }
        }
        jdbcTemplate.execute(sqlStatement.substring(0, sqlStatement.length() - 1) + ")");
        return true;
    }

    public <T> void update(T object) throws IllegalAccessException {
        Class<?> cls = object.getClass();
        String sqlStatment = String.format(updateStatement, cls.getSimpleName());
        Object primaryKeyValue = "";
        String setClause = "";
        String whereClause = "WHERE ";
        ArrayList<Object> values = new ArrayList<>();
        for(Field field : cls.getDeclaredFields()){
            if(field.isAnnotationPresent(DatabaseField.class)){
                DatabaseField annotation = field.getAnnotation(DatabaseField.class);
                field.setAccessible(true);
                if (annotation.primaryKey()){
                    whereClause += field.getName() + " = ?";
                    primaryKeyValue = field.get(object);
                }
                else if(annotation.edit() && field.get(object) != null){
                    values.add(field.get(object));
                    setClause += " " + field.getName() + " = ?,";
                }
            }
        }
        setClause = setClause.substring(0, setClause.length() - 1);
        sqlStatment += setClause + " " + whereClause;
        values.add(primaryKeyValue);
        jdbcTemplate.update(sqlStatment, values.toArray());
    }

    public <T> T insert(T object) throws IllegalAccessException {
        Class<?> cls = object.getClass();
        String sqlStatement = String.format(insertStatement, cls.getSimpleName());
        ArrayList<Object> values = new ArrayList<>();
        String valueStatement = " VALUES(";
        String columnStatement = " (";
        String primaryKeyName = "";
        for(Field field : cls.getDeclaredFields()){
            if(field.isAnnotationPresent(DatabaseField.class)){
                DatabaseField annotation = field.getAnnotation(DatabaseField.class);
                field.setAccessible(true);
                if(annotation.primaryKey()){
                    primaryKeyName = field.getName();
                }
                else if(annotation.create() && field.get(object) != null){
                    values.add(field.get(object));
                    valueStatement += " ?,";
                    columnStatement += " " + field.getName() + ",";
                }
            }
        }
        valueStatement = valueStatement.substring(0, valueStatement.length() - 1) + ")";
        columnStatement = columnStatement.substring(0, columnStatement.length() - 1) + ")";
        sqlStatement += columnStatement + valueStatement;
        jdbcTemplate.update(sqlStatement, values.toArray());
        Integer primaryKeyValue = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        T t = (T) select(cls, String.format("WHERE %s = %s", primaryKeyName, primaryKeyValue));
        return t;
    }

    private <T> T select(Class<T> cls, String whereClause){
        String sqlStatement = String.format(selectStatement, cls.getSimpleName()) + " " + whereClause;
        T t = jdbcTemplate.queryForObject(sqlStatement, (rs, rowNum) -> mapRowToObject(rs, cls));
        return t;
    }

    private <T> T mapRowToObject(ResultSet rs, Class<T> cls) throws SQLException {
        T t = getNewInstance(cls);
        if(t == null) return t;
        for(Field field : cls.getDeclaredFields() ){
            setFieldValue(rs, t, field);
        }
        return t;
    }

    private <T> T getNewInstance(Class<T> cls){
        T t = null;
        try {
            t = cls.getDeclaredConstructor().newInstance();
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return t;
    }

    public <T> void setFieldValue(ResultSet rs, T t, Field field) throws SQLException {
        if(field.isAnnotationPresent(DatabaseField.class)){
            String fieldName = field.getName();
            field.setAccessible(true);
            try {
                String fieldClass = field.getType().getSimpleName();
                if(fieldClass.equals("String")){
                    field.set(t, rs.getString(fieldName));
                }
                else if(fieldClass.equals( "int" )){
                    field.set(t, rs.getInt(fieldName));
                }
                else if(fieldClass.equals( "float" )){
                    field.set(t, rs.getFloat(fieldName));
                }
                else if(fieldClass.equals( "double" )){
                    field.set(t, rs.getDouble(fieldName));
                }
                else if(fieldClass.equals( "boolean" )){
                    field.set(t, rs.getBoolean(fieldName));
                }
            }
            catch (IllegalAccessException e){
                System.out.println(e.getMessage());
            }

        }
    }
    public <T> ArrayList<T> selectAll(Class<T> cls){
        ArrayList<T> objects;
        String sqlStatement = String.format(selectStatement, cls.getSimpleName());
        try {
            objects = jdbcTemplate.queryForObject( sqlStatement, (rs, rowNum) -> {
                ArrayList<T> o = new ArrayList<>();
                while(rs.next()){
                    o.add(mapRowToObject(rs, cls));
                }
                return o;
            });
        }
        catch (EmptyResultDataAccessException e) {
            objects = new ArrayList<>();
        }
        return objects;
    }

}
