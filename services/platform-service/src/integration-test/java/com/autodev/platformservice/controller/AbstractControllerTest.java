package com.autodev.platformservice.controller;

import com.autodev.platformservice.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings("java:S6813")
public class AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    protected MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUpSecurity() {
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("test-uuid-123");
        securityUtilsMock.when(SecurityUtils::getCurrentUserEmail).thenReturn("test@test.com");

    }

    @AfterEach
    public void closeSecurityMock() {
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }
}
