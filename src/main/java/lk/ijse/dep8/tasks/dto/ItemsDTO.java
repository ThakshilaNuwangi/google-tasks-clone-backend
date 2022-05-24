package lk.ijse.dep8.tasks.dto;

import java.io.Serializable;
import java.util.List;

public class ItemsDTO implements Serializable {
    private List<TaskDTO> items;

    public ItemsDTO() {
    }

    public ItemsDTO(List<TaskDTO> items) {
        this.items = items;
    }

    public List<TaskDTO> getItems() {
        return items;
    }

    public void setItems(List<TaskDTO> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ItemsDTO{" +
                "items=" + items +
                '}';
    }
}
