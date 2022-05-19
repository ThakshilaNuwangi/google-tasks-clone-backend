package lk.ijse.dep8.tasks.util;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class HttpResponseErrorMsg {
    private long timeStamp;
    private int status;
    private String exception;
    private String message;
    private String path;

    public HttpResponseErrorMsg() {
    }

    public HttpResponseErrorMsg(long timeStamp, int status, String exception, String message, String path) {
        this.timeStamp = timeStamp;
        this.status = status;
        this.exception = exception;
        this.message = message;
        this.path = path;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return Arrays.asList(HttpServletResponse.class.getDeclaredFields())
                .stream().filter(field -> {
                    try {
                        return ((int) field.get(HttpServletResponse.class)) == status;
                    } catch (IllegalAccessException e) {
                        return false;
                    }
                }).findFirst().map(field -> field.getName().replaceFirst("SC_", "")
                        .replace("_", " ")).orElse("Internal Server Error");
    }

    public String getException() {
        return System.getProperty("app.profile.active").equals("dev")? exception : null;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "HttpResponseErrorMsg{" +
                "timeStamp=" + timeStamp +
                ", status=" + status +
                ", exception='" + exception + '\'' +
                ", message='" + message + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
