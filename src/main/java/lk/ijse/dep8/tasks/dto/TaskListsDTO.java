package lk.ijse.dep8.tasks.dto;

import java.util.List;

public class TaskListsDTO {
    List<TaskListDTO> items;

    public TaskListsDTO() {
    }

    public TaskListsDTO(List<TaskListDTO> items) {
        this.items = items;
    }

    public List<TaskListDTO> getItems() {
        return items;
    }

    public void setItems(List<TaskListDTO> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "TaskListsDTO{" +
                "items=" + items +
                '}';
    }
}
