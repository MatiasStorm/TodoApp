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
    JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseContext(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public <T> boolean createTableIfNotExists(Class<T> cls){
        String sqlStatement = "CREATE TABLE IF NOT EXISTS `" + cls.getSimpleName() + "` (";
        for(Field field : cls.getDeclaredFields()){
            if(field.isAnnotationPresent(DatabaseField.class)){
                DatabaseField annotation = field.getAnnotation(DatabaseField.class);
                sqlStatement += String.format(" `%s` %s,", field.getName(), annotation.dataType());
            }
        }
        jdbcTemplate.execute(sqlStatement.substring(0, sqlStatement.length() - 1) + ")");
        return true;
    }

    public <T> T addRow(T obj) throws IllegalAccessException {
        Class<?> cls = obj.getClass();
        String insertStatement = String.format("INSERT INTO `%s`", cls.getSimpleName());
        HashMap<String, Object> valuePairs = new HashMap<>();
        for(Field field : cls.getDeclaredFields()){
            if(field.isAnnotationPresent(DatabaseField.class)){
                DatabaseField annotation = field.getAnnotation(DatabaseField.class);
                field.setAccessible(true);
                if(annotation.create() && field.get(obj) != null){
                    valuePairs.put(field.getName(), field.get(obj));
                }
            }
        }
        Object[] keys = valuePairs.keySet().toArray();
        Object[] values = new Object[keys.length];
        String columnStatement = "";
        String valueStatement = "";
        Object key;
        for(int i = 0; i < keys.length; i++){
            key = keys[i];
            values[i] = valuePairs.get(key);
            if(i == 0){
                columnStatement += key;
                valueStatement += "?";
            }
            else {
                columnStatement += ", " + key;
                valueStatement += ", ?";
            }
        }
        insertStatement += " ( " + columnStatement + ") VALUES( " + valueStatement + " )";
        jdbcTemplate.update(insertStatement, values);
        int id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        T t = (T) selectById(id, cls);
        return t;
    }

    private <T> T selectById(int id, Class<T> cls){
        String sqlStatement = "SELECT * FROM " + cls.getSimpleName() + " WHERE id = " + id;
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
        try {
            objects = jdbcTemplate.queryForObject("SELECT * FROM " + cls.getSimpleName(), (rs, rowNum) -> {
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
