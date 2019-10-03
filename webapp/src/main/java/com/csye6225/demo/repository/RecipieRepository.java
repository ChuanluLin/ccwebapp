package com.csye6225.demo.repository;

import com.csye6225.demo.pojo.Recipie;
import org.springframework.data.repository.CrudRepository;

public interface RecipieRepository extends CrudRepository<Recipie, Integer> {
    public Recipie findById(String id);
}
