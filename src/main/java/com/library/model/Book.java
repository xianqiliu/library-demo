package com.library.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String author;

    private Integer amount;

    public Book() {
    }

    public Book(String title, String author, Integer amount) {
        this.title = title;
        this.author = author;
        this.amount = amount;
    }
}