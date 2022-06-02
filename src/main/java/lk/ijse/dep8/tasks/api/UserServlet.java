package lk.ijse.dep8.tasks.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.service.UserService;
import lk.ijse.dep8.tasks.util.HttpServlet2;
import lk.ijse.dep8.tasks.util.ResponseStatusException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Logger;

@WebServlet(name = "UserServlet")
public class UserServlet extends HttpServlet2 {

    private final Logger logger = Logger.getLogger(UserServlet.class.getName());

    @Resource(name = "java:comp/env/jdbc/pool")
    private volatile DataSource pool;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().startsWith("multipart/form-data")) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Invalid content type");
            return;
        }

        if (req.getPathInfo() != null && !req.getPathInfo().equals("/")) {
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid end point for POST requests");
        }

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        Part picture = req.getPart("picture");

        if (name == null || !name.matches("[A-Za-z ]+")) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid name or name is empty");
        } else if (email == null || !email.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid email or email is empty");
        } else if (password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid password or password is empty");
        } else if (picture != null && (picture.getSize() == 0 || !picture.getContentType().startsWith("image"))) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid picture");
        }

        Connection connection = null;
        try {
            connection = pool.getConnection();

            if (new UserService().existsUser(connection, email)) {
                throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT, "User has been already registered");
            }

            String pictureUrl = null;
            if (picture != null) {
                pictureUrl = req.getScheme() + "://" + req.getServerName() + ":"
                        + req.getServerPort() + req.getContextPath() + "/uploads/";
                ;
            }
            UserDTO user = new UserDTO(null, name, email, password, pictureUrl);

            user = new UserService().registerUser(connection, picture,
                    getServletContext().getRealPath("/"), user);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");

            Jsonb jsonb = JsonbBuilder.create();
            jsonb.toJson(user, resp.getWriter());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Throwable e) {
            throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to register the user", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        resp.setContentType("application/json");
        jsonb.toJson(getUser(req), resp.getWriter());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = getUser(req);

        try (Connection connection = pool.getConnection()) {
            new UserService().deleteUser(connection, user.getId(), getServletContext().getRealPath("/"));
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Throwable e) {
            throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (req.getContentType() == null || !req.getContentType().startsWith("multipart/form-data")) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Invalid content type");
            return;
        }

        UserDTO user = getUser(req);

        String name = req.getParameter("name");
        String password = req.getParameter("password");
        Part picture = req.getPart("picture");

        if (name == null || !name.matches("[A-Za-z ]+")) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid name or name is empty");
        } else if (password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid password or password is empty");
        } else if (picture != null && (picture.getSize() == 0 || !picture.getContentType().startsWith("image"))) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid picture");
        }

        try (Connection connection = pool.getConnection()) {

            String pictureUrl = null;
            if (picture != null) {
                pictureUrl = req.getScheme() + "://" + req.getServerName() + ":"
                        + req.getServerPort() + req.getContextPath();
                pictureUrl += "/uploads/" + user.getId();
            }
            new UserService().updateUser(connection, new UserDTO(user.getId(), name, user.getEmail(), password, pictureUrl), picture, getServletContext().getRealPath("/"));

            resp.setStatus(204);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Throwable e) {
            throw new ResponseStatusException(500, e.getMessage(), e);
        }
    }

    private UserDTO getUser(HttpServletRequest req) {
        if (!(req.getPathInfo() != null && (req.getPathInfo().replaceAll("/", "").length() == 36))) {
            throw new ResponseStatusException(404, "Invalid User ID");
        }

        String userId = req.getPathInfo().replaceAll("/", "");

        try (Connection connection = pool.getConnection()) {
            if (!new UserService().existsUser(connection, userId)) {
                throw new ResponseStatusException(HttpServletResponse.SC_NOT_FOUND, "Invalid user Id");
            } else {
                return new UserService().getUser(connection, userId);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Throwable e) {
            throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to fetch the user info", e);
        }
    }
}
