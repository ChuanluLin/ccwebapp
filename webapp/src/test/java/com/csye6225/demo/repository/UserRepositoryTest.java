package com.csye6225.demo.repository;

import com.csye6225.demo.pojo.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByEmail() {
        // remove potential User in database
        User user = userRepository.findByEmail("test@email.com");
        if (user != null) {
            userRepository.delete(user);
        }
        String email = "test@email.com";
        String first_name = "John";
        User user_create = new User();
        user_create.setEmail(email);
        user_create.setFirst_name(first_name);
        user_create.setLast_name("Doe");
        user_create.setPassword("Qwer123!");
        userRepository.save(user_create);

        User user_query = userRepository.findByEmail(email);
        Assert.assertTrue(user_query != null && first_name.equals(user_query.getFirst_name()));
    }
}