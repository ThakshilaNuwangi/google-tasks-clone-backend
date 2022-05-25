package lk.ijse.dep8.tasks.security;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.util.HttpResponseErrorMsg;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;

@WebFilter(filterName = "SecurityFilter", urlPatterns = "/*")
public class SecurityFilter extends HttpFilter {

    @Resource(name = "java:comp/env/jdbc/pool")
    private volatile DataSource pool;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        /*Excludes from security filter*/
        String appContextPath = req.getContextPath();
        if (req.getRequestURI().matches(appContextPath+"/v1/users/?") && req.getMethod().equals("POST")) {
            chain.doFilter(req, res);
            return;
        }

        String authorization = req.getHeader("Authorization");

        if (authorization==null || !authorization.startsWith("Basic")) {
            sendErrorResponse(req, res);
            return;
        }

        String base64Credentials = authorization.replaceFirst("Basic ", "");
        byte[] decodedByteArray = Base64.getDecoder().decode(base64Credentials);
        String userCredentials = new String(decodedByteArray);

        String[] split = userCredentials.split(":", 2);
        String username = split[0];
        String password = split[1];

        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM user WHERE email=?");
            stm.setString(1, username);
            ResultSet rst = stm.executeQuery();

            if (!rst.next()) {
                sendErrorResponse(req, res);
                return;
            }

            if (!DigestUtils.sha256Hex(password).equals(rst.getString("password"))) {
                sendErrorResponse(req, res);
                return;
            }

            SecurityContextHolder.setPrincipal(new UserDTO(rst.getString("id"),
                    rst.getString("full_name"), rst.getString("email"),
                    rst.getString("password"), rst.getString("profile_pic")));

            chain.doFilter(req, res);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendErrorResponse(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setStatus(401);
        Jsonb jsonb = JsonbBuilder.create();
        jsonb.toJson(new HttpResponseErrorMsg(new Date().getTime(), 401, null, "permission denied", req.getRequestURI()), res.getWriter());
    }
}
