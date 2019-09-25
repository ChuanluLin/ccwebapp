package com.csye6225.demo.controller;

import com.csye6225.demo.pojo.User;
import com.csye6225.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;
  
    @RequestMapping(path = "/v1/user", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> create(@RequestBody String userJSON, HttpServletResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap userMap = mapper.readValue(userJSON, HashMap.class);

        User newUser = new User();

        //user exist
        String email = userMap.get("email_address").toString();
        User user_db = userRepository.findByEmail(email);
        String password = userMap.get("password").toString();
        String first_name = userMap.get("first_name").toString();
        String last_name = userMap.get("last_name").toString();
        if (user_db != null) {
            return new ResponseEntity<>("The email exists! Please try again", HttpStatus.BAD_REQUEST);
        } else if(first_name == null || last_name == null){
            return new ResponseEntity<>("Name is empty!", HttpStatus.BAD_REQUEST);
        } else if (!isEmail(email)) {
            return new ResponseEntity<>("Invalid email! Please try again!", HttpStatus.BAD_REQUEST);
        } else if (!isStrongPassword(password)) {
            return new ResponseEntity<>("Need a strong password! Please try again!", HttpStatus.BAD_REQUEST);
        } else {
            //password
            String pw_hash = BCrypt.hashpw(password, BCrypt.gensalt());
            newUser.setPassword(pw_hash);

            newUser.setEmail(email);
            newUser.setFirst_name(first_name);
            newUser.setLast_name(last_name);

            //time
            newUser.setAccount_created(getDatetime());
            newUser.setAccount_updated(getDatetime());

            userRepository.save(newUser);
            String newUserJSON = mapper.writeValueAsString(newUser);

            return new ResponseEntity<>(newUserJSON, HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/v1/user/self", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> update(@RequestBody String userJSON, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName());
        ObjectMapper mapper = new ObjectMapper();
        HashMap userMap = mapper.readValue(userJSON, HashMap.class);
        String password = userMap.get("password").toString();
        String first_name = userMap.get("first_name").toString();
        String last_name = userMap.get("last_name").toString();
            //password
        if(password.equals("") || first_name.equals("") || last_name.equals("")) {
            return new ResponseEntity<>("Type all content.", HttpStatus.BAD_REQUEST);
        } else{
                if (!isStrongPassword(password)) {
                    return new ResponseEntity<>("Need a strong password! Please try again!", HttpStatus.BAD_REQUEST);
                } else {
                    String pw_hash = BCrypt.hashpw(password, BCrypt.gensalt());
                    user.setPassword(pw_hash);
                }
                //name
                user.setFirst_name(userMap.get("first_name").toString());
                user.setLast_name(userMap.get("last_name").toString());
                //time
                user.setAccount_updated(getDatetime());
                userRepository.save(user);
                String newUserJSON = mapper.writeValueAsString(user);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(path = "/v1/user/self", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> GET(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName());

        ObjectMapper mapper = new ObjectMapper();
        String userJSON = mapper.writeValueAsString(user);
        return new ResponseEntity<>(userJSON,HttpStatus.OK) ;
    }



    public String getDatetime() {
        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String dateString = format.format(currentTime);
        return dateString;
    }

    public boolean isEmail(String email) {
        return email.matches("[a-zA-Z0-9_]+@[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+");
    }

    public boolean isStrongPassword(String password) {
        return password.matches("^(?=.*\\d)(?=.*[a-zA-Z])(?=.*[\\W])[\\da-zA-Z\\W]{8,}$");
    }
}


