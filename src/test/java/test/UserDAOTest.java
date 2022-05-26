package test;

import lk.ijse.dep8.tasks.dao.UserDAO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {

    private Connection connection;

    @BeforeEach
    void setUp() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep8_tasks", "root", "mysql");
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"thakshila123@gmail.com", "20b23df5-9c80-4b04-93a0-a9a22ac1ce7f", "hgbuy@gmail.com"})
    void existsUser(String arg) throws SQLException {
        boolean result = UserDAO.existsUser(connection, arg);
        assertTrue(result);
    }

    @Test
    void saveUser() throws SQLException {
        String id = UUID.randomUUID().toString();
        UserDTO givenUser = new UserDTO(id, "Kasun", "kasdfgvbhnjun@gmail.com", "abc", null);
        UserDTO savedUser = UserDAO.saveUser(connection, givenUser);
        boolean result = UserDAO.existsUser(connection, savedUser.getEmail());
        assertTrue(result);
        assertEquals(givenUser, savedUser);
    }

    @ParameterizedTest
    @ValueSource(strings = {"thakshila123@gmail.com", "20b23df5-9c80-4b04-93a0-a9a22ac1ce7f", "hgbuy@gmail.com"})
    void getUser(String arg) throws SQLException {
        // When
        UserDTO user = UserDAO.getUser(connection, arg);
        // Then
        assertNotNull(user);
    }
}