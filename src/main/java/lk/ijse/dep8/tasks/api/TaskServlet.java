package lk.ijse.dep8.tasks.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep8.tasks.dto.TaskDTO;
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

@WebServlet(name = "TaskServlet")
public class TaskServlet extends HttpServlet2 {

    private final Logger logger = Logger.getLogger(TaskServlet.class.getName());
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

        String pattern = "^/([A-Fa-f0-9\\-]{36})/lists/(\\d+)/tasks/?$";
        if (!req.getPathInfo().matches(pattern)) {
            System.out.println(req.getPathInfo().matches(pattern));
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid end point for POST request");
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);
        int taskListId = Integer.parseInt(matcher.group(2));

        Connection connection=null;
        try {
            connection = pool.get().getConnection();

            PreparedStatement stm = connection.prepareStatement("SELECT * FROM task_list t WHERE t.id=? AND t.user_id=?");
            stm.setInt(1, taskListId);
            stm.setString(2, userId);
            if (!stm.executeQuery().next()) {
                throw new ResponseStatusException(HttpServletResponse.SC_NOT_FOUND, "Invalid user id or task id");
            }

            Jsonb jsonb = JsonbBuilder.create();
            TaskDTO task = jsonb.fromJson(req.getReader(), TaskDTO.class);

            if (task.getTitle()==null || task.getTitle().trim().isEmpty()) {
                throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid title or title is empty");
            }
            task.setPosition(0);
            task.setStatus(TaskDTO.Status.NEEDS_ACTION.toString());
            connection.setAutoCommit(false);

            pushDown(connection, 0);
            stm = connection.prepareStatement("INSERT INTO task (title, details, position, status,task_list_id) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, task.getTitle());
            stm.setString(2, task.getNotes());
            stm.setInt(3, task.getPosition());
            stm.setString(4, task.getStatus().toString());
            stm.setInt(5, taskListId);

            if (stm.executeUpdate()!=1) {
                throw new SQLException("Failed to save the task");
            }

            ResultSet rst = stm.getGeneratedKeys();
            rst.next();
            task.setId(rst.getInt(1));

            connection.setAutoCommit(true);

            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            jsonb.toJson(task,resp.getWriter());

        } catch (SQLException | JsonbException e) {
            e.printStackTrace();
        } finally {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void pushDown(Connection connection, int position) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE task t SET position = position+1 WHERE t.position>=? ORDER BY t.position");
        stm.setInt(1, position);
        stm.executeUpdate();
    }

    private TaskDTO getTask(HttpServletRequest req) {
        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/(\\d+)/tasks/(\\d+)/?";
        if (!req.getPathInfo().matches(pattern)) {
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    String.format("Invalid end point for %s request", req.getMethod()));
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);
        int taskListId = Integer.parseInt(matcher.group(2));
        int taskId = Integer.parseInt(matcher.group(3));

        try (Connection connection = pool.get().getConnection()) {
            PreparedStatement stm = connection.
                    prepareStatement("SELECT * FROM task_list tl INNER JOIN task t WHERE t.id=? AND tl.id=? AND tl.user_id=?");
            stm.setInt(1, taskId);
            stm.setInt(2, taskListId);
            stm.setString(3, userId);
            ResultSet rst = stm.executeQuery();
            if (rst.next()) {
                int id = rst.getInt("id");
                String title = rst.getString("title");
                String details = rst.getString("details");
                int position = rst.getInt("position");
                String status = rst.getString("status");
                return new TaskDTO(id, title,position, details, status);
            } else {
                throw new ResponseStatusException(404, "Invalid user id or task id");
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(500, "Failed to fetch task details");
        }
    }
}
