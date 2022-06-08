package lk.ijse.dep8.tasks.dao.exception;

public class DataAccessException extends RuntimeException{
    public DataAccessException() {
    }

    public DataAccessException(String message) {
        super(message);
    }
}
