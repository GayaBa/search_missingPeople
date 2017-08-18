package com.example.gaya.searchpeople;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class DBclass extends TableServiceEntity {
    public DBclass(String username) {
        this.partitionKey = username;
        this.rowKey = "";
    }

    public DBclass() {
    }

    private String password;
    private String email;
    private String phoneNumber;
    private String name;
    private String surname;
    private String age;
    private String comment;

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}