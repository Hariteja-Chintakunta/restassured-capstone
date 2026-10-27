package com.capstone.utils;

import java.util.UUID;

public class TestDataUtil {

    public static String randomName() {
        return "QA User " + UUID.randomUUID().toString().substring(0, 6);
    }

    public static String randomEmail() {
        return "qa_" + UUID.randomUUID().toString().substring(0, 10) + "@example.com";
    }

    public static String userJson(String name, String email, String gender, String status) {
        return String.format(
                "{\"name\":\"%s\",\"email\":\"%s\",\"gender\":\"%s\",\"status\":\"%s\"}",
                name, email, gender, status);
    }
}