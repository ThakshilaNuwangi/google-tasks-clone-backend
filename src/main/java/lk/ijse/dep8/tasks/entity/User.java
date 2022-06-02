package lk.ijse.dep8.tasks.entity;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String email;
    private String password;
    private String fullName;
    private String profilePic;

    public User() {
    }

    public User(String id, String email, String password, String fullName, String profilePic) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.profilePic = profilePic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", fullName='" + fullName + '\'' +
                ", profilePic='" + profilePic + '\'' +
                '}';
    }
}
