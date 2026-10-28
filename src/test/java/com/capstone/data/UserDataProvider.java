// ABOUTME: TestNG DataProvider that supplies varied User objects for data-driven tests.
// ABOUTME: Each row represents a distinct user creation scenario (gender, status combinations).
package com.capstone.data;

import com.capstone.models.User;
import com.capstone.utils.TestDataUtil;
import org.testng.annotations.DataProvider;

public class UserDataProvider {

    @DataProvider(name = "createUserData")
    public static Object[][] createUserData() {
        return new Object[][]{
                {new User(TestDataUtil.randomName(), TestDataUtil.randomEmail(), "male", "active")},
                {new User(TestDataUtil.randomName(), TestDataUtil.randomEmail(), "female", "active")},
                {new User(TestDataUtil.randomName(), TestDataUtil.randomEmail(), "male", "inactive")},
                {new User(TestDataUtil.randomName(), TestDataUtil.randomEmail(), "female", "inactive")},
        };
    }
}
