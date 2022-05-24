package lk.ijse.dep8.tasks.dto;

import java.io.Serializable;

public class ResourceDTO implements Serializable {

    private ItemsDTO resource;

    public ResourceDTO() {
    }

    public ResourceDTO(ItemsDTO resource) {
        this.resource = resource;
    }

    public ItemsDTO getResource() {
        return resource;
    }

    public void setResource(ItemsDTO resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "ResourceDTO{" +
                "resource=" + resource +
                '}';
    }
}
