package io.github.ossnass.jpa99;

import javax.persistence.*;

@Entity
@Table(name = "Person")
public class Person {

    private Integer id;
    private String name;

    @Id
    @Basic
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public Person setId(Integer id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "name", length = 50, nullable = false)
    public String getName() {
        return name;
    }

    public Person setName(String name) {
        this.name = name;
        return this;
    }
}
