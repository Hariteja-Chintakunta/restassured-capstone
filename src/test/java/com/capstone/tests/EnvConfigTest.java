package com.capstone.tests;

import com.capstone.utils.PropertyReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EnvConfigTest {

    @Test
    public void baseUrlIsReadFromPropertyFile() {
        String baseUrl = PropertyReader.get("baseUrl");
        System.out.println("Environment : " + PropertyReader.getEnv());
        System.out.println("Base URL    : " + baseUrl);
        Assert.assertTrue(baseUrl.startsWith("https://"));
    }

    @Test
    public void environmentNameMatchesSelectedEnv() {
        Assert.assertEquals(PropertyReader.get("envName"), PropertyReader.getEnv());
    }
}