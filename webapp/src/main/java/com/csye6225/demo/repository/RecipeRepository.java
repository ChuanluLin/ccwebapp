package com.csye6225.demo.repository;

import com.csye6225.demo.pojo.Recipe;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecipeRepository extends CrudRepository<Recipe, Integer> {
    public Recipe findById(String id);

    @Query("select u from Recipe u order by u.created_ts")
    List<Recipe> findInOrders();

    public List<Recipe> findByAuthor_id(String author_id);
}
