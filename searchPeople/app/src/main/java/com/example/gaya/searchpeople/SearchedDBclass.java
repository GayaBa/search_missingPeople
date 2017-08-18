package com.example.gaya.searchpeople;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class SearchedDBclass extends TableServiceEntity {
    public SearchedDBclass(String username, String id) {
        this.partitionKey = id;
        this.rowKey = username;
    }

    public SearchedDBclass() {
    }

    private String name;
    private String surname;
    private String age;
    private String comment;
    private int id;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return this.surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAge() {
        return this.age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}