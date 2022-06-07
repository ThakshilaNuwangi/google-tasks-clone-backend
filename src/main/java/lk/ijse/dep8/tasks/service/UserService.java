package lk.ijse.dep8.tasks.service;

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

    private final Logger logger = Logger.getLogger(UserService.class.getName());

    public boolean existsUser(Connection connection, String userIdOrEmail) throws SQLException {
        return new OldUserDAO().existsUser(connection, userIdOrEmail);
    }

    public UserDTO registerUser(Connection connection, Part picture, String appLocation, UserDTO user) throws SQLException {
        try {
            connection.setAutoCommit(false);
            user.setId(UUID.randomUUID().toString());

            if (picture != null) {
                user.setPicture(user.getPicture() + user.getId());
            }
            user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
            UserDTO savedUser = new OldUserDAO().saveUser(connection, user);

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

    public void updateUser(Connection connection, UserDTO user, Part picture, String appLocation) throws SQLException {
        try {
            connection.setAutoCommit(false);

            user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
            new OldUserDAO().updateUser(connection, user);

            Path path = Paths.get(appLocation, "uploads");
            Path picturePath = path.resolve(user.getId());

            if (picture != null) {
                if (Files.notExists(path)) {
                    Files.createDirectory(path);
                }

                Files.deleteIfExists(picturePath);
                picture.write(picturePath.toAbsolutePath().toString());
            } else {
                Files.deleteIfExists(picturePath);
            }

            connection.commit();
        } catch (Throwable e){
            connection.rollback();
            throw new RuntimeException(e);
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void deleteUser(Connection connection, String userId, String appLocation) throws SQLException {
        new OldUserDAO().deleteUser(connection, userId);

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
        return new OldUserDAO().getUser(connection, userIdOrEmail);
    }
}
