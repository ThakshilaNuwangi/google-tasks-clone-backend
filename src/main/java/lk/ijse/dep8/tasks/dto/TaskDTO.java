package lk.ijse.dep8.tasks.dto;

import java.io.Serializable;

public class TaskDTO implements Serializable {
    private Integer id;
    private String title;
    private Integer position;
    private String notes;
    private Status status;

    public TaskDTO() {
    }

    public TaskDTO(Integer id, String title, Integer position, String notes, Status status) {
        this.id = id;
        this.title = title;
        this.position = position;
        this.notes = notes;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status{
        NEEDS_ACTION("needsAction"), COMPLETED("completed");
        private String state;

        Status(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return "Status{" +
                    "state='" + state + '\'' +
                    '}';
        }
    }
}
