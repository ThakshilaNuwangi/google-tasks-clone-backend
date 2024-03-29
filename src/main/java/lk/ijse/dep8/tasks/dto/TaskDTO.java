package lk.ijse.dep8.tasks.dto;

import jakarta.json.bind.annotation.JsonbTransient;

import java.io.Serializable;

public class TaskDTO implements Serializable {
    private Integer id;
    private String title;
    private Integer position;
    private String notes;
    private Status status = Status.NEEDS_ACTION;
    @JsonbTransient
    private int taskListId;


    public TaskDTO(Integer id, String title, Integer position, String notes, String status, Integer taskListId) {
        this.id = id;
        this.title = title;
        this.position = position;
        this.notes = notes;
        this.setStatus(status);
        this.taskListId = taskListId;
    }

    public TaskDTO() {
    }

    public TaskDTO(Integer id, String title, Integer position, String notes, String status) {
        this.id = id;
        this.title = title;
        this.position = position;
        this.notes = notes;
        this.setStatus(status);
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

    public String getStatus() {
        return status.toString();
    }

    public void setStatus(String status) {
        this.status = status.equals("completed") ? Status.COMPLETED : Status.NEEDS_ACTION;
    }

    public int getTaskListId() {
        return taskListId;
    }

    public void setTaskListId(int taskListId) {
        this.taskListId = taskListId;
    }

    public enum Status {
        NEEDS_ACTION("needsAction"), COMPLETED("completed");
        private String state;

        Status(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return state;
        }
    }
}
