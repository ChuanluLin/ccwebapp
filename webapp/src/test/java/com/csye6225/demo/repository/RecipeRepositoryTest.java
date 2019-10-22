package com.csye6225.demo.repository;

import com.csye6225.demo.pojo.NutritionInformation;
import com.csye6225.demo.pojo.Recipe;
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
public class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Test
    public void findById() {
        String title = "Kung Pao Chicken";
        Recipe recipe_create = new Recipe();
        recipe_create.setAuthor_id("8a8080376d61df6b016d61e06d260000");
        recipe_create.setTitle(title);
        recipe_create.setCreated_ts("2019-09-25T17:29:45.908Z");
        recipe_create.setCusine("Chinese");
        NutritionInformation ni = new NutritionInformation();
        ni.setCalories(100);
        recipe_create.setNutrition_information(ni);
        recipe_create.setUpdated_ts("2019-09-25T17:29:45.908Z");
        recipe_create = recipeRepository.save(recipe_create);
        String id = recipe_create.getId();

        Recipe recipe_query = recipeRepository.findById(id);
        Assert.assertTrue(recipe_query != null && title.equals(recipe_query.getTitle()));
    }
}