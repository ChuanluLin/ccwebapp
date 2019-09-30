package com.csye6225.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserControllerTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp (){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void create() throws Exception {
        // generate input JSON
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> createJsonMap = new HashMap<>();
        createJsonMap.put("email_address", "test@email.com");
        createJsonMap.put("password", "123abcD@");
        createJsonMap.put("first_name", "Chuanlu");
        createJsonMap.put("last_name", "Lin");
        String json = objectMapper.writeValueAsString(createJsonMap);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                //the root element of the query，for example, $.length() represents the whole returned document
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value("6"))
                .andDo(MockMvcResultHandlers.print()); // print out the http response info
    }

    @Test
    public void get() throws Exception {
        Authentication authToken = new TestingAuthenticationToken("test@email.com", null);
        SecurityContextHolder.getContext().setAuthentication(authToken);
        System.out.println("principal:"+ SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/user/self").principal(authToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                //the root element of the query，for example, $.length() represents the whole returned document
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value("6"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last_name").value("Lin"))
                .andDo(MockMvcResultHandlers.print()); // print out the http response info
    }

    @Test
    public void update() throws Exception {
        // generate input JSON
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> createJsonMap = new HashMap<>();
        createJsonMap.put("password", "123abcD@");
        createJsonMap.put("first_name", "Lulu");
        createJsonMap.put("last_name", "Lin");
        String json = objectMapper.writeValueAsString(createJsonMap);

        Authentication authToken = new TestingAuthenticationToken("test@email.com", null);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/user/self").principal(authToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                //the root element of the query，for example, $.length() represents the whole returned document
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value("6"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.first_name").value("Lulu"))
                .andDo(MockMvcResultHandlers.print()); // print out the http response info
    }

}
