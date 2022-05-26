package lk.ijse.dep8.tasks.service;

import lk.ijse.dep8.tasks.dao.UserDAO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    public boolean existsUser(Connection connection, String userIdOrEmail) throws SQLException {
        return UserDAO.existsUser(connection, userIdOrEmail);
    }

    public UserDTO registerUser(Connection connection, Part picture, String appLocation, UserDTO user) throws SQLException {
        try {
            connection.setAutoCommit(false);
            user.setId(UUID.randomUUID().toString());

            if (picture != null) {
                user.setPicture(user.getPicture() + user.getId());
            }
            user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
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
            throw new RuntimeException(e);
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void updateUser(UserDTO user) {

    }

    public void deleteUser(Connection connection, String userId, String appLocation) throws SQLException {
        UserDAO.deleteUser(connection, userId);

        new Thread(()->{
            Path imagePath = Paths.get(appLocation, "uploads", userId);
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                logger.warning("Failed to delete the image:"+imagePath.toAbsolutePath());
            }
        }).start();
    }

    public UserDTO getUser(Connection connection, String userIdOrEmail) throws SQLException {
        return UserDAO.getUser(connection, userIdOrEmail);
    }
}
