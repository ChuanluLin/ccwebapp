package com.csye6225.demo.controller;

import com.csye6225.demo.pojo.User;
import com.csye6225.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

//    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
//    public void illegalRegisterException(HttpServletResponse response) throws IOException {
//        response.sendError(HttpStatus.BAD_REQUEST.value(),"The email is exist! Please try again!");
//    }


    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public ResponseEntity<String> create(@RequestBody String userJSON, HttpServletResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap userMap = mapper.readValue(userJSON, HashMap.class);

        User newUser = new User();

        //user exist
        String email = userMap.get("email_address").toString();
        User user_db = userRepository.findByEmail(email);
        String password = userMap.get("password").toString();
        if (user_db != null) {
//            response.sendError(HttpStatus.BAD_REQUEST.value(), "The email is exist!");
            return new ResponseEntity<>("The email exists! Please try again", HttpStatus.BAD_REQUEST);
        }  else {
            //password
            String pw_hash = BCrypt.hashpw(password, BCrypt.gensalt());
            newUser.setPassword(pw_hash);

            newUser.setEmail(userMap.get("email_address").toString());
            newUser.setFirst_name(userMap.get("first_name").toString());
            newUser.setLast_name(userMap.get("last_name").toString());

            //time
            newUser.setAccount_created(getDatetime());
            newUser.setAccount_updated(getDatetime());

            userRepository.save(newUser);
            String newUserJSON = mapper.writeValueAsString(newUser);

            return new ResponseEntity<>(newUserJSON, HttpStatus.CREATED);
        }
    }

    public String getDatetime() {
        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String dateString = format.format(currentTime);
        return dateString;
    }




}


