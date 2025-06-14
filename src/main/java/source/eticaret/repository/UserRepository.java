package source.eticaret.repository;

import source.eticaret.model.User;
import source.eticaret.service.DatabaseService;

import java.sql.*;
import java.util.Optional;

public class UserRepository {
    private final DatabaseService databaseService;

    public UserRepository(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT u.*, r.name as role_name FROM users u " +
                   "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
                   "LEFT JOIN roles r ON ur.role_id = r.id " +
                   "WHERE u.username = ?";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                user.setRoleName(rs.getString("role_name"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean save(User user) {
        String userSql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
        String roleSql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, (SELECT id FROM roles WHERE name = ?))";

        Connection conn = null;
        try {
            conn = databaseService.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, user.getUsername());
                userStmt.setString(2, user.getEmail());
                userStmt.setString(3, user.getPasswordHash());

                int affectedRows = userStmt.executeUpdate();

                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }


                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long userId = generatedKeys.getLong(1);
                        user.setId(userId);
                        if (user.getRoleName() != null && !user.getRoleName().isEmpty()) {
                            try (PreparedStatement roleStmt = conn.prepareStatement(roleSql)) {
                                roleStmt.setLong(1, userId);
                                roleStmt.setString(2, user.getRoleName());
                                roleStmt.executeUpdate();
                            }
                        }

                        conn.commit();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    


    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null) {
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return user;
    }
}