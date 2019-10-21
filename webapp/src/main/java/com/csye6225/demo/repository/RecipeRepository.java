package com.csye6225.demo.repository;

import com.csye6225.demo.pojo.Recipe;
import org.springframework.data.repository.CrudRepository;

public interface RecipeRepository extends CrudRepository<Recipe, Integer> {
    public Recipe findById(String id);
}
