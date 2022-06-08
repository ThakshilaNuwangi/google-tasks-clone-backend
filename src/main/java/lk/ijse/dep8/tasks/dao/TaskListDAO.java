package lk.ijse.dep8.tasks.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskListDAO {
    private int id;
    private String name;
    private String userId;
}
