package com.csye6225.demo.repository;


import com.csye6225.demo.pojo.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    public User findByEmail(String email);
}