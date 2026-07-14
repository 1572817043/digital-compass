package com.a0000.digicompass.modules.user.mapper;

import com.a0000.digicompass.modules.user.dto.UserListItem;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminUserMapper {

    private final JdbcTemplate jdbc;

    public AdminUserMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<UserListItem> findUsers(String keyword, String role, Integer status) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, username, nickname, role, status,
                       DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS created_at,
                       DATE_FORMAT(updated_at, '%Y-%m-%d %H:%i') AS updated_at
                FROM dc_user WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (username LIKE ? OR nickname LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like); params.add(like);
        }
        if (role != null && !role.isBlank()) { sql.append(" AND role = ?"); params.add(role); }
        if (status != null) { sql.append(" AND status = ?"); params.add(status); }
        sql.append(" ORDER BY id DESC");
        return jdbc.query(sql.toString(), params.toArray(), (rs, rn) -> new UserListItem(
                rs.getLong("id"), rs.getString("username"), rs.getString("nickname"),
                rs.getString("role"), rs.getInt("status"),
                rs.getString("created_at"), rs.getString("updated_at")
        ));
    }

    public void updateRole(Long id, String role) {
        jdbc.update("UPDATE dc_user SET role = ? WHERE id = ?", role, id);
    }

    public void updateStatus(Long id, int status) {
        jdbc.update("UPDATE dc_user SET status = ? WHERE id = ?", status, id);
    }

    public void updatePassword(Long id, String passwordHash) {
        jdbc.update("UPDATE dc_user SET password_hash = ? WHERE id = ?", passwordHash, id);
    }
}
