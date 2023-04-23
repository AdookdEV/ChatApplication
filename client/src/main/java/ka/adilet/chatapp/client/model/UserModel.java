package ka.adilet.chatapp.client.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

public class UserModel {
    private Long id;
    private String name;
    private String surname;
    @JsonSetter("phone_number")
    private String phoneNumber;
    private String password;

    public UserModel() {
        id = 0L;
        name = "Some user";
        surname = "Some surname";
        phoneNumber = "XXX";
    }

    public UserModel(Long id, String name, String surname, String phoneNumber, String password) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    @JsonGetter("id")
    public Long getId() {
        return id;
    }
    @JsonGetter("name")
    public String getName() {
        return name;
    }
    @JsonGetter("surname")
    public String getSurname() {
        return surname;
    }
    @JsonGetter("phone_number")
    public String getPhoneNumber() {
        return phoneNumber;
    }
    @JsonGetter("password")
    public String getPassword() {return  password;}

    @JsonSetter("id")
    public void setId(Long id) {
        this.id = id;
    }
    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
    }
    @JsonSetter("surname")
    public void setSurname(String surname) {
        this.surname = surname;
    }
    @JsonSetter("phone_number")
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    @JsonSetter("password")
    public void setPassword() {this.password = password;}
}
