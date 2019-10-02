package com.csye6225.demo.repository;

import com.csye6225.demo.pojo.NutritionInformation;
import com.csye6225.demo.pojo.Recipie;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class RecipieRepositoryTest {

    @Autowired
    private RecipieRepository recipieRepository;

    @Test
    public void findById() {
        String title = "Kung Pao Chicken";
        Recipie recipie_create = new Recipie();
        recipie_create.setAuthor_id("8a8080376d61df6b016d61e06d260000");
        recipie_create.setTitle(title);
        recipie_create.setCreated_ts("2019-09-25T17:29:45.908Z");
        recipie_create.setCusine("Chinese");
        NutritionInformation ni = new NutritionInformation();
        ni.setCalories(100);
        recipie_create.setNutrition_information(ni);
        recipie_create.setUpdated_ts("2019-09-25T17:29:45.908Z");
        recipie_create = recipieRepository.save(recipie_create);
        String id = recipie_create.getId();

        Recipie recipie_query = recipieRepository.findById(id);
        Assert.assertTrue(recipie_query != null && title.equals(recipie_query.getTitle()));
    }
}