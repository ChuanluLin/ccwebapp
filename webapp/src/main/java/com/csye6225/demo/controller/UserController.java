package com.csye6225.demo.controller;

import com.csye6225.demo.pojo.User;
import com.csye6225.demo.repository.UserRespository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

//    @ExceptionHandler
//    public void illegalRegister(IllegalArgumentException e, HttpServletResponse response) throws IOException {
//        response.sendError(HttpStatus.BAD_REQUEST.value(),"The email is exist!");
//    }


    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public User create(@RequestBody Map<String, String> payload, HttpServletResponse response) throws IOException {
//        for(User u: UserRespository.findAll()){
//            if(u.getEmail().equals(payload.get("email_address"))){
////                throw new illegalRegister();
//                response.sendError(HttpStatus.BAD_REQUEST.value(),"The email is exist!");
//            }
//        }

        User user = new User();

        //user exist
        User user_db = UserRespository.findByEmail(payload.get("email_address"));
        if(user_db != null)
            response.sendError(HttpStatus.BAD_REQUEST.value(),"The email is exist!");

        //strong password

        //password
        String pw_hash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(pw_hash);




        user.setEmail(payload.get("email_address"));
        user.setFirst_name(payload.get("first_name"));
        user.setLast_name(payload.get("last_name"));

        //time
        //need a filter for these 2 time
        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        String dateString = format.format(currentTime);
        user.setAccount_created(dateString);
        user.setAccount_updated(dateString);

        //need a filter to hide password




        return user;

    }


}


