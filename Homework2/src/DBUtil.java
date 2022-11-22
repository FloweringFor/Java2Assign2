import java.sql.*;

public class DBUtil {

    // 获得驱动
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 建立连接
    public static Connection getCollection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/java2", "postgres", "741128");
    }


    // 关流操作
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }


    // 查询用户
    public static User select(Connection conn, String username) {
        PreparedStatement preState = null;
        ResultSet rs = null;
        try {
            preState = conn.prepareStatement("select * from client where username = ?");
            preState.setString(1, username);
            rs = preState.executeQuery();
            User user = null;
            while (rs.next()) {
                user = new User(rs.getString("username"), rs.getString("password"), rs.getInt("win"), rs.getInt("lose"), rs.getInt("tie"));
            }
            return user;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBUtil.close(null, preState,rs);
        }
        return null;
    }


    // 添加用户
    public static boolean add(Connection conn, String username, String password) {
        PreparedStatement preState = null;
        try {
            preState = conn.prepareStatement("insert into client values(?, ?, ?, ?, ?)");
            preState.setString(1, username);
            preState.setString(2, password);
            preState.setInt(3, 0);
            preState.setInt(4, 0);
            preState.setInt(5, 0);
            int change = preState.executeUpdate();
            return change == 1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBUtil.close(null, preState, null);
        }
        return false;
    }

    // 更新战局
    public static boolean update(Connection conn, String username, int win, int lose, int tie) {
        PreparedStatement preState = null;
        try {
            User user = select(conn, username);
            preState = conn.prepareStatement("update client set win = ?, lose = ?, tie = ? where username = ?");
            preState.setInt(1, user.win + win);
            preState.setInt(2, user.lose + lose);
            preState.setInt(3, user.tie + tie);
            preState.setString(4, username);
            int change = preState.executeUpdate();
            return change == 1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBUtil.close(null, preState, null);
        }
        return false;
    }
}

