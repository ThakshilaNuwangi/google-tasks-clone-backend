package lk.ijse.dep8.tasks.entity;

import lk.ijse.dep8.tasks.dao.TaskDAO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private int id;
    private String title;
    private String details;
    private int position;
    private TaskDAO.Status status;
    private int taskListId;

    public enum Status{
        completed, needsAction
    }
}
