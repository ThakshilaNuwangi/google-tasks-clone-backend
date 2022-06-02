package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dto.UserDTO;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserDAO {

    public boolean existsUser(Connection connection, String emailOrId) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("SELECT id FROM user WHERE email=? OR id=?");
        stm.setString(1, emailOrId);
        stm.setString(2, emailOrId);
        return (stm.executeQuery().next());
    }

    public UserDTO saveUser(Connection connection, UserDTO user) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("INSERT INTO user(id, email, password, full_name, profile_pic) VALUES (?,?,?,?,?)");
        stm.setString(1, user.getId());
        stm.setString(2, user.getEmail());
        stm.setString(3, user.getPassword());
        stm.setString(4, user.getName());
        stm.setString(5, user.getPicture());

        if (stm.executeUpdate()!=1) {
            throw new SQLException("Failed to save the user");
        }
        return user;
    }

    public void updateUser(Connection connection, UserDTO user) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE user SET full_name=?, password=?, profile_pic=? WHERE id=?");
        stm.setString(1, user.getName());
        stm.setString(2, user.getPassword());
        stm.setString(3, user.getPicture());
        stm.setString(4, user.getId());
        if (stm.executeUpdate()!=1) {
            throw new SQLException("Failed to update the user");
        }
    }

    public void deleteUser(Connection connection, String userId) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("DELETE FROM user WHERE id=?");
        stm.setString(1, userId);
        if (stm.executeUpdate()!=1) {
            throw new SQLException("Failed to delete the user");
        }
    }
    public UserDTO getUser(Connection connection, String emailOrId) throws SQLException {
        PreparedStatement stm = connection.
                prepareStatement("SELECT * FROM user WHERE email = ? OR id = ?");
        stm.setString(1, emailOrId);
        stm.setString(2, emailOrId);
        ResultSet rst = stm.executeQuery();
        if (rst.next()) {
            return new UserDTO(rst.getString("id"),
                    rst.getString("full_name"),
                    rst.getString("email"),
                    rst.getString("password"),
                    rst.getString("profile_pic"));
        } else {
            return null;
        }
    }
}
