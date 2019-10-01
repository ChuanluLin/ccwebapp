package com.csye6225.demo.controller;
import com.csye6225.demo.exception.DataValidationException;
import com.csye6225.demo.pojo.User;
import com.csye6225.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;

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
//            return new ResponseEntity<>("The email exists! Please try again", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "The email exists! Please try again", "/v1/user");
        } else if(first_name.equals("") || last_name.equals("")){
//            return new ResponseEntity<>("Name is empty!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Name is empty!", "/v1/user");
        } else if (!isEmail(email)) {
//            return new ResponseEntity<>("Invalid email! Please try again!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Invalid email! Please try again!", "/v1/user");
        } else if (!isStrongPassword(password)) {
//            return new ResponseEntity<>("Need a strong password! Please try again!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Need a strong password! Please try again!", "/v1/user");
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
//        if (userMap.containsKey("email_address") | userMap.containsKey("account_created") | userMap.containsKey("account_updated")) {
//            return new ResponseEntity<>("Could not change some information.", HttpStatus.BAD_REQUEST);
//        }
        String password = userMap.get("password").toString();
        String first_name = userMap.get("first_name").toString();
        String last_name = userMap.get("last_name").toString();
            //password
        if(password.equals("") || first_name.equals("") || last_name.equals("")) {
//            return new ResponseEntity<>("Type all content.", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Type all content.", "/v1/user/self");
        } else{
                if (!isStrongPassword(password)) {
//                    return new ResponseEntity<>("Need a strong password! Please try again!", HttpStatus.BAD_REQUEST);
                    throw new DataValidationException(getDatetime(), 400, "Bad Request", "Need a strong password! Please try again!", "/v1/user/self");
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
                return new ResponseEntity<>(newUserJSON, HttpStatus.OK);
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


