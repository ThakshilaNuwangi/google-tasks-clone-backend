package lk.ijse.dep8.tasks.listener;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class DBInitializer implements ServletContextListener {

    private final Logger logger = Logger.getLogger(DBInitializer.class.getName());

    @Resource(name = "java:comp/env/jdbc/pool")
    private DataSource pool;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        final String[] tables = {"task", "task_list", "user", "sub_task"};
        final List<String> tableList = new ArrayList<>();

        try (Connection connection = pool.getConnection()) {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SHOW TABLES");

            while (rst.next()) {
                tableList.add(rst.getString(1));
            }
            Arrays.sort(tables);

            if (!Arrays.equals(tables, tableList.toArray())) {
                InputStream is = this.getClass().getResourceAsStream("/db-script.sql");
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                String sqlScript = new String(bytes);

                stm.execute(sqlScript);
            }


        } catch (SQLException | IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
}
