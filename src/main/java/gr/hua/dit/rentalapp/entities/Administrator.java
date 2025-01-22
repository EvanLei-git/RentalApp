package gr.hua.dit.rentalapp.entities;


import jakarta.persistence.*;



@Entity
@Table(name = "administrators")
public class Administrator extends User {

    public Administrator() {
        super();
    }

    public Administrator(String username, String email, String password) {
        super(username, email, password);
    }

}