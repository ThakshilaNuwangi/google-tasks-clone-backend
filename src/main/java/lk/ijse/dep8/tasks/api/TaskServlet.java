package lk.ijse.dep8.tasks.api;

import jakarta.json.*;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParser;
import lk.ijse.dep8.tasks.dto.TaskDTO;
import lk.ijse.dep8.tasks.util.HttpServlet2;
import lk.ijse.dep8.tasks.util.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
    public void init() {
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

        Connection connection = null;
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

            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
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

            if (stm.executeUpdate() != 1) {
                throw new SQLException("Failed to save the task");
            }

            ResultSet rst = stm.getGeneratedKeys();
            rst.next();
            task.setId(rst.getInt(1));

            connection.setAutoCommit(true);

            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            jsonb.toJson(task, resp.getWriter());

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

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        TaskDTO task = getTask(req);
        Connection connection = null;
        try {
            connection = pool.get().getConnection();
            connection.setAutoCommit(false);
            pushUp(connection, task.getPosition());
            PreparedStatement stm = connection.prepareStatement("DELETE FROM task WHERE id=?");
            stm.setInt(1, task.getId());
            if (stm.executeUpdate() != 1) {
                throw new SQLException("Failed to delete the task list");
            }
            connection.commit();
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        } finally {
            try {
                if (connection != null) {
                    if (!connection.getAutoCommit()) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pattern = "^/([A-Fa-f0-9\\-]{36})/lists/(\\d+)/tasks/?$";
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        if (matcher.find()) {
            String userId = matcher.group(1);
            int taskListId = Integer.parseInt(matcher.group(2));

            try (Connection connection = pool.get().getConnection()) {
                PreparedStatement stm = connection.prepareStatement("SELECT * FROM task_list t WHERE t.id=? AND t.user_id=?");
                stm.setInt(1, taskListId);
                stm.setString(2, userId);
                if (!stm.executeQuery().next()) {
                    throw new ResponseStatusException(HttpServletResponse.SC_NOT_FOUND, "invalid task Id");
                }
                stm = connection.prepareStatement("SELECT * FROM task WHERE task.task_list_id=? ORDER BY position");
                stm.setInt(1, taskListId);
                ResultSet rst = stm.executeQuery();
                List<TaskDTO> tasks = new ArrayList<>();
                while (rst.next()) {
                    tasks.add(new TaskDTO(rst.getInt("id"), rst.getString("title"), rst.getInt("position"),
                            rst.getString("details"), rst.getString("status"), taskListId));
                }
                resp.setContentType("application/json");
                Jsonb jsonb = JsonbBuilder.create();
                String jsonArray = jsonb.toJson(tasks);

                JsonParser parser = Json.createParser(new StringReader(jsonArray));
                parser.next();
                JsonArray taskArray = parser.getArray();

                JsonObject json = Json.createObjectBuilder().add("resource", Json.createObjectBuilder().add("items", taskArray)).build();
                resp.getWriter().println(json);
            } catch (SQLException e) {
                throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
            }

        } else {
            TaskDTO task = getTask(req);
            resp.setContentType("application/json");
            Jsonb jsonb = JsonbBuilder.create();
            jsonb.toJson(task, resp.getWriter());
        }

    }

    private void pushDown(Connection connection, int position) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE task t SET position = position+1 WHERE t.position>=? ORDER BY t.position");
        stm.setInt(1, position);
        stm.executeUpdate();
    }

    private void pushUp(Connection connection, int position) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE task t SET position = position-1 WHERE t.position>=? ORDER BY t.position");
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
                String title = rst.getString("title");
                String details = rst.getString("details");
                int position = rst.getInt("position");
                String status = rst.getString("status");
                return new TaskDTO(taskId, title, position, details, status);
            } else {
                throw new ResponseStatusException(404, "Invalid user id or task id");
            }
        } catch (SQLException e) {
            throw new ResponseStatusException(500, "Failed to fetch task details");
        }
    }
}
