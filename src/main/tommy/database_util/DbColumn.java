package database_util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
@Data
public class DbColumn {
    String name;
    int type;
    int idx;
    Object get(ResultSet rs) throws SQLException {
        return rs.getObject(idx + 1);
    }
}
