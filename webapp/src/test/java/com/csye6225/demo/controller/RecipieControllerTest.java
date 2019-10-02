package com.csye6225.demo.controller;

import com.csye6225.demo.pojo.Recipie;
import com.csye6225.demo.pojo.User;
import com.csye6225.demo.repository.UserRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RecipieControllerTest {
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private User test_user;
    private static Recipie createdRecipie;
    private static boolean initialized = false;

    @Before
    public void setup(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        if (!initialized) {
            // remove potential User in database
            User user = userRepository.findByEmail("test@email.com");
            if (user != null) {
                userRepository.delete(user);
            }
            //generate test user
            test_user = new User();
            test_user.setEmail("test@email.com");
            test_user.setPassword("qwer123!");
            test_user.setFirst_name("Chuanlu");
            test_user.setLast_name("Lin");
            userRepository.save(test_user);
            // finish initialization
            initialized = true;
        }
    }

    @Test
    public void createRecipie() throws Exception {
        Authentication authToken = new TestingAuthenticationToken("test@email.com", null);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\n" +
                "  \"cook_time_in_min\": 15,\n" +
                "  \"prep_time_in_min\": 15,\n" +
                "  \"title\": \"Kung Pao Chicken\",\n" +
                "  \"cusine\": \"Italian\",\n" +
                "  \"servings\": 2,\n" +
                "  \"ingredients\": [\n" +
                "    \"4 ounces linguine pasta\",\n" +
                "    \"2 boneless, skinless chicken breast halves, sliced into thin strips\",\n" +
                "    \"2 teaspoons Cajun seasoning\",\n" +
                "    \"2 tablespoons butter\"\n" +
                "  ],\n" +
                "  \"steps\": [\n" +
                "    {\n" +
                "      \"position\": 1,\n" +
                "      \"items\": \"do not over cooked\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"position\": 2,\n" +
                "      \"items\": \"finish\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"nutrition_information\": {\n" +
                "    \"calories\": 100,\n" +
                "    \"cholesterol_in_mg\": 4,\n" +
                "    \"sodium_in_mg\": 100,\n" +
                "    \"carbohydrates_in_grams\": 53.7,\n" +
                "    \"protein_in_grams\": 53.7\n" +
                "  }\n" +
                "}";

        ResultActions resultActions =
            mockMvc.perform(MockMvcRequestBuilders.post("/v1/recipie/")
                .principal(authToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                //the root element of the query，for example, $.length() represents the whole returned document
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value("13"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.author_id").value(test_user.getId()))
                .andDo(MockMvcResultHandlers.print()); // print out the http response info

        // get response json
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        // parse json to object
        createdRecipie = objectMapper.readValue(contentAsString, Recipie.class);
    }

    @Test
    public void recipieGET() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/recipie/"+createdRecipie.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                //the root element of the query，for example, $.length() represents the whole returned document
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value("13"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Kung Pao Chicken"))
                .andDo(MockMvcResultHandlers.print()); // print out the http response info
    }

    @Test
    public void recipieUpdate() throws Exception {
        Authentication authToken = new TestingAuthenticationToken("test@email.com", null);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        String json = "{\n" +
                "  \"cook_time_in_min\": 15,\n" +
                "  \"prep_time_in_min\": 15,\n" +
                "  \"title\": \"Kung Pao Chicken\",\n" +
                "  \"cusine\": \"Chinese\",\n" +
                "  \"servings\": 2,\n" +
                "  \"ingredients\": [\n" +
                "    \"4 ounces linguine pasta\",\n" +
                "    \"2 boneless, skinless chicken breast halves, sliced into thin strips\",\n" +
                "    \"2 teaspoons Cajun seasoning\",\n" +
                "    \"2 tablespoons butter\"\n" +
                "  ],\n" +
                "  \"steps\": [\n" +
                "    {\n" +
                "      \"position\": 1,\n" +
                "      \"items\": \"do not over cooked\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"position\": 2,\n" +
                "      \"items\": \"finish\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"nutrition_information\": {\n" +
                "    \"calories\": 100,\n" +
                "    \"cholesterol_in_mg\": 4,\n" +
                "    \"sodium_in_mg\": 100,\n" +
                "    \"carbohydrates_in_grams\": 53.7,\n" +
                "    \"protein_in_grams\": 53.7\n" +
                "  }\n" +
                "}";

        ResultActions resultActions =
                mockMvc.perform(MockMvcRequestBuilders.put("/v1/recipie/"+createdRecipie.getId())
                        .principal(authToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        //the root element of the query，for example, $.length() represents the whole returned document
                        .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value("13"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.cusine").value("Chinese"))
                        .andDo(MockMvcResultHandlers.print()); // print out the http response info
    }

    @Test
    public void recipieZDelete() throws Exception {
        Authentication authToken = new TestingAuthenticationToken("test@email.com", null);
        SecurityContextHolder.getContext().setAuthentication(authToken);
        System.out.println("principal:"+ SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/recipie/"+createdRecipie.getId())
                .principal(authToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print()); // print out the http response info
    }
}