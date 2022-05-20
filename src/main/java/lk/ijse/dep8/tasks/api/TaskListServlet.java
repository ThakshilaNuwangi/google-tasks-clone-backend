package lk.ijse.dep8.tasks.api;

import lk.ijse.dep8.tasks.util.HttpServlet2;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "TaskListServlet", value = "/v1/*")
public class TaskListServlet extends HttpServlet2 {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null){
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        String pattern = "/[A-Fa-f0-9\\-]{36}/lists/?.*";
        if (pathInfo.matches(pattern)){
            super.service(req, resp);
        }else{
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
