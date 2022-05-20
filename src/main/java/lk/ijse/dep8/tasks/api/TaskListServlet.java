package lk.ijse.dep8.tasks.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep8.tasks.dto.TaskListDTO;
import lk.ijse.dep8.tasks.util.HttpServlet2;
import lk.ijse.dep8.tasks.util.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "TaskListServlet")
public class TaskListServlet extends HttpServlet2 {

    private final Logger logger = Logger.getLogger(TaskListServlet.class.getName());
    private AtomicReference<DataSource> pool;

    @PostConstruct
    public void init(){
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/pool");
            pool = new AtomicReference<>(ds);
        } catch (NamingException e) {
            logger.log(Level.SEVERE, "Failed to locate the JNDI pool", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().startsWith("application/json")) {
            throw new ResponseStatusException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Invalid content type or content type is empty");
        }

        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/?";
        if (!req.getPathInfo().matches(pattern)) {
            System.out.println(req.getPathInfo().matches(pattern));
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid end point for POST request");
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);

        try (Connection connection = pool.get().getConnection()) {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM user WHERE id=?");
            stm.setString(1, userId);
            ResultSet rst = stm.executeQuery();
            if (!rst.next()) {
                throw new ResponseStatusException(HttpServletResponse.SC_NOT_FOUND, "Invalid user ID");
            }
            Jsonb jsonb = JsonbBuilder.create();
            TaskListDTO taskListDTO = jsonb.fromJson(req.getReader(), TaskListDTO.class);

            if (taskListDTO.getTitle().trim().isEmpty()) {
                throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid title or title is empty");
            }

            stm = connection.prepareStatement("INSERT INTO task_list (name, user_id) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, taskListDTO.getTitle());
            stm.setString(2, userId);

            if (stm.executeUpdate()!=1) {
                throw new SQLException("Failed to save the task list");
            }

            rst = stm.getGeneratedKeys();
            rst.next();
            taskListDTO.setId(rst.getInt(1));

            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            jsonb.toJson(taskListDTO,resp.getWriter());

        } catch (SQLException | JsonbException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        TaskListDTO taskList = getTaskList(req);
        try (Connection connection = pool.get().getConnection()) {
            PreparedStatement stm = connection.prepareStatement("DELETE FROM task_list WHERE id=?");
            stm.setInt(1, taskList.getId());
            if (stm.executeUpdate()!=1) {
                throw new SQLException("Failed to delete the task list");
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    private TaskListDTO getTaskList(HttpServletRequest req) {
        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/(\\d+)/?";
        if (!req.getPathInfo().matches(pattern)) {
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    String.format("Invalid end point for %s request", req.getMethod()));
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);
        int taskListId = Integer.parseInt(matcher.group(2));

        try (Connection connection = pool.get().getConnection()) {
            PreparedStatement stm = connection.
                    prepareStatement("SELECT * FROM task_list t WHERE t.id=? AND t.user_id=?");
            stm.setInt(1, taskListId);
            stm.setString(2, userId);
            ResultSet rst = stm.executeQuery();
            if (rst.next()) {
                int id = rst.getInt("id");
                String title = rst.getString("name");
                return new TaskListDTO(id, title, userId);
            } else {
                throw new ResponseStatusException(404, "Invalid user id or task list id");
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(500, "Failed to fetch task list details");
        }
    }
}
