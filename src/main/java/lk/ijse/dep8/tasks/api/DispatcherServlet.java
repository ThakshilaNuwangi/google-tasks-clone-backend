package lk.ijse.dep8.tasks.api;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@MultipartConfig(location = "/tmp", maxFileSize = 10 * 1024 * 1024)
@WebServlet(name = "DispatcherServlet", value = "/v1/users/*")
public class DispatcherServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getPathInfo() == null || req.getPathInfo().equals("/")){
//            /v1/users
//            /v1/users/

            getServletContext().getNamedDispatcher("UserServlet").forward(req,resp);
        } else {
            if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/?")){
//            /v1/users/{{user_uuid}}
//            /v1/users/{{user_uuid}}/
                getServletContext().getNamedDispatcher("UserServlet").forward(req, resp);
            } else if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/lists(/\\d+)?/?")){
//            /v1/users/{{user_uuid}}/lists
//            /v1/users/{{user_uuid}}/lists/
//            /v1/users/{{user_uuid}}/lists/{{taskList_id}}
//            /v1/users/{{user_uuid}}/lists/{{taskList_id}}/
                getServletContext().getNamedDispatcher("TaskListServlet").forward(req, resp);
            }
        }

    }
}
