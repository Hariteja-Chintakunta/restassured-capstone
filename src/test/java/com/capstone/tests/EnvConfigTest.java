package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.server.LocalApiServer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EnvConfigTest extends ApiBase {

    @Test
    public void localApiBaseUrlUsesLoopbackInterface() {
        Assert.assertTrue(LocalApiServer.baseUri().startsWith("http://127.0.0.1:"));
    }
}