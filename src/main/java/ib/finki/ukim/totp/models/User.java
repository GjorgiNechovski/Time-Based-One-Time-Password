package ib.finki.ukim.totp.models;

import ib.finki.ukim.totp.models.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String name;
    protected String surname;
    protected String password;
    protected byte[] passwordSalt;

    @Enumerated(EnumType.STRING)
    protected UserRole role;

    public User(String name, String surname, String password, byte[] passwordSalt) {
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.passwordSalt = passwordSalt;
        this.role = UserRole.Customer;
    }

    public String getFullName(){
        return name + " " + surname;
    }
}
