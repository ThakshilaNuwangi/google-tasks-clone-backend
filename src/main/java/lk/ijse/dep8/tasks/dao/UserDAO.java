package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dto.UserDTO;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class UserDAO {

    public static boolean existsUser(Connection connection, String email) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("SELECT id FROM user WHERE email=?");
        stm.setString(1, email);
        return (stm.executeQuery().next());
    }

    public static UserDTO saveUser(Connection connection, UserDTO user) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("INSERT INTO user(id, email, password, full_name, profile_pic) VALUES (?,?,?,?,?)");
        String id = UUID.randomUUID().toString();
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
    public static void updateUser(UserDTO user) {

    }
    public static void deleteUser(String userId) {

    }
    public static UserDTO getUser(String userId) {
        return null;
    }
}
