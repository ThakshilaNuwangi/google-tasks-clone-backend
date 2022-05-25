package lk.ijse.dep8.tasks.service;

import lk.ijse.dep8.tasks.dao.UserDAO;
import lk.ijse.dep8.tasks.dto.UserDTO;

import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    public static boolean existUser(Connection connection, String email) throws SQLException {
        return UserDAO.existsUser(connection, email);
    }

    public static void saveUser (UserDTO user) {

    }
    public static void  updateUser (UserDTO user) {

    }
    public static void deleteUser (String userId) {

    }
    public static UserDTO getUser(String userId) {
        return null;
    }
}
