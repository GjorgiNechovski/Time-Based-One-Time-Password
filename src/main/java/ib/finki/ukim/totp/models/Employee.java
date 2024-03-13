package ib.finki.ukim.totp.models;

import ib.finki.ukim.totp.models.enums.UserRole;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Employee extends User {
    private String username;

    public Employee(String name, String surname, String password, byte[] password_salt, String username) {
        super(name, surname, password, password_salt);
        this.username = username;
        this.role = UserRole.Employee;
    }
}
