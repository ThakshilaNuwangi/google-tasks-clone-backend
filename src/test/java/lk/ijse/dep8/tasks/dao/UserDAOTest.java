package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.entity.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static Connection connection;
    private static UserDAO userDAO;

    @BeforeAll
    static void setUp() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep8_tasks", "root", "mysql");
            connection.setAutoCommit(false);
            userDAO = new UserDAO(connection);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void tearDown() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static List<User> getDummyUsers(){
        List<User> dummies = new ArrayList<>();
        dummies.add(new User("U001", "u001@gmail.com", "admin", "Kasun", "picture1"));
        dummies.add(new User("U002", "u002@gmail.com", "admin", "Nuwan", "picture1"));
        dummies.add(new User("U003", "u003@gmail.com", "admin", "Ruwan", null));
        dummies.add(new User("U004", "u004@gmail.com", "admin", "Supun", null));
        dummies.add(new User("U005", "u005@gmail.com", "admin", "Gayal", "picture1"));
        return dummies;
    }

    @Order(1)
    @MethodSource("getDummyUsers")
    @ParameterizedTest
    void saveUser(User givenUser) {
        // when
        User savedUser = userDAO.saveUser(givenUser);

        // then
        assertEquals(givenUser, savedUser);
    }

    @Order(2)
    @Test
    void existsUserById() {
        System.out.println("existsUserById");
    }

    @Order(3)
    @Test
    void findUserById() {
        System.out.println("findUserById");
    }

    @Order(4)
    @Test
    void findAllUsers() {
        System.out.println("findAllUsers");
    }

    @Order(5)
    @Test
    void deleteUserById() {
        System.out.println("deleteUserById");
    }

    @Order(6)
    @Test
    void countUsers() {
        System.out.println("countUsers");
    }
}