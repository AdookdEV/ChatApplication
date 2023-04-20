package ka.adilet.chatapp.client.model;

import com.fasterxml.jackson.annotation.JsonGetter;
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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    @JsonGetter("phone_number")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }
}
