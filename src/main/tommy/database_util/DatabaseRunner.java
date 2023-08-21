package database_util;

import java.sql.*;

public class DatabaseRunner {
    private final String url;
    private final String user;
    private final String password;

    Connection connection;
    public Connection start() {
        connection = getConnection();
        return connection;
    }

    public DbResult select(String query) throws SQLException {
        return select(connection, query);
    }

    public void update(String query) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query);) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public void finish() throws SQLException {
        connection.close();;
    }

    private DbResult select(Connection connection, String query) throws SQLException {
        DbResult result = new DbResult();
        PreparedStatement stmt = connection.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
            String columnName = rs.getMetaData().getColumnName(i + 1);
            int columnType = rs.getMetaData().getColumnType(i + 1);
            result.columns.put(columnName, new DbColumn(columnName, columnType, i));
        }
        while (rs.next()) {
            result.addRow(rs);
        }
        return result;
    }

    public DatabaseRunner(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.printf("Connected to database %s successfully\n", url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}
