package lk.ijse.dep8.tasks.service;

import lk.ijse.dep8.tasks.dao.UserDAO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.Part;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class UserService {

    public static boolean existsUser(Connection connection, String email) throws SQLException {
        return UserDAO.existsUser(connection, email);
    }

    public static UserDTO registerUser(Connection connection, Part picture, String pictureUrl, String appLocation, UserDTO user) throws SQLException {
        try {
            connection.setAutoCommit(false);
            user.setId(UUID.randomUUID().toString());
            user.setPassword(DigestUtils.sha256Hex(user.getPassword()));

            if (picture != null) {
                pictureUrl += "/uploads/" + user.getId();
            }
            user.setPicture(pictureUrl);
            UserDTO savedUser = UserDAO.saveUser(connection, user);

            if (picture != null) {
                Path path = Paths.get(appLocation, "uploads");
                if (Files.notExists(path)) {
                    Files.createDirectory(path);
                }

                String picturePath = path.resolve(user.getId()).toAbsolutePath().toString();
                picture.write(picturePath);
            }

            connection.commit();
            return savedUser;

        } catch (Throwable e) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw new RuntimeException(e);
        }
    }

    public static void updateUser(UserDTO user) {

    }

    public static void deleteUser(String userId) {

    }

    public static UserDTO getUser(String userId) {
        return null;
    }
}
