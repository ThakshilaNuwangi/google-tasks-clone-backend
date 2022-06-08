package lk.ijse.dep8.tasks.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDAO {
    private int id;
    private String title;
    private String details;
    private int position;
    private Status status;
    private int taskListId;

    public enum Status{
        completed, needsAction
    }
}
