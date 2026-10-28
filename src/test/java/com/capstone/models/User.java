// ABOUTME: POJO representing a user entity for API request/response serialization.
// ABOUTME: Used with Gson to convert between Java objects and JSON payloads.
package com.capstone.models;

import com.google.gson.Gson;

public class User {

    private static final Gson GSON = new Gson();

    private String name;
    private String email;
    private String gender;
    private String status;

    public User(String name, String email, String gender, String status) {
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getStatus() {
        return status;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {
        return name + " (" + email + ") [" + gender + ", " + status + "]";
    }
}
