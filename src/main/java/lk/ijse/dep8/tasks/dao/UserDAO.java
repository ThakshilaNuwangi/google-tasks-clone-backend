package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dto.UserDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDAO {

    public static boolean existsUser(Connection connection, String email) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("SELECT id FROM user WHERE email=?");
        stm.setString(1, email);
        return (stm.executeQuery().next());
    }

    public static void saveUser(UserDTO user) {

    }
    public static void updateUser(UserDTO user) {

    }
    public static void deleteUser(String userId) {

    }
    public static UserDTO getUser(String userId) {
        return null;
    }
}
