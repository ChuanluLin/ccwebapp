package com.csye6225.demo.controller;

import com.csye6225.demo.pojo.NutritionInformation;
import com.csye6225.demo.pojo.OrderedList;
import com.csye6225.demo.pojo.Recipie;
import com.csye6225.demo.pojo.User;
import com.csye6225.demo.repository.RecipieRepository;
import com.csye6225.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
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
            return new ResponseEntity<>("The email exists! Please try again", HttpStatus.BAD_REQUEST);
        } else if(first_name.equals("") || last_name.equals("")){
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
        if (userMap.containsKey("email_address") | userMap.containsKey("account_created") | userMap.containsKey("account_updated")) {
            return new ResponseEntity<>("Could not change some information.", HttpStatus.BAD_REQUEST);
        }
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

    @Autowired
    private RecipieRepository recipieRepository;

    @RequestMapping(path = "/v1/recipie/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> createRecipie(@RequestBody String recipieJSON, HttpServletResponse response) throws IOException, JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JSONObject recipieObj = new JSONObject(recipieJSON);
        Recipie newRecipie = new Recipie();

        User user = userRepository.findByEmail(auth.getName());
        String userid = user.getId();
        newRecipie.setAuthor_id(userid);
        int cook_time_in_min = (int) recipieObj.getInt("cook_time_in_min");
        int prep_time_in_min = (int) recipieObj.getInt("prep_time_in_min");
        if(!(cook_time_in_min % 5 == 0) || !(prep_time_in_min % 5 == 0) ){
            return new ResponseEntity<>("Cook or prep time should multiple of 5!", HttpStatus.BAD_REQUEST);
        }
        String title = recipieObj.getString("title");
        String cusine = recipieObj.getString("cusine");
        int servings = recipieObj.getInt("servings");
        if(!(servings >= 1 && servings <= 5)){
            return new ResponseEntity<>("Servings should be from 1 to 5!", HttpStatus.BAD_REQUEST);
        }
        //Ingredients
        Set<String> ingredients = new HashSet<String>();
        JSONArray ingArray  = recipieObj.getJSONArray("ingredients");
        int len = ingArray.length();
        for(int i = 0; i < len; i++){
            ingredients.add(ingArray.getString(i));
        }
        //Steps
        List<OrderedList> steps = new ArrayList<OrderedList>();
        OrderedList order = new OrderedList();
        int position = recipieObj.getJSONArray("steps").getJSONObject(0).getInt("position");
        if(position < 1 ){
            return new ResponseEntity<>("Position no less than 1!", HttpStatus.BAD_REQUEST);
        }
        String items = recipieObj.getJSONArray("steps").getJSONObject(0).getString("items");
        order.setPosition(position);
        order.setItems(items);
        steps.add(order);
        //Nutrition
        NutritionInformation nutrition_information = new NutritionInformation();
        int calories = recipieObj.getJSONObject("nutrition_information").getInt("calories");
        Number cholesterol_in_mg = recipieObj.getJSONObject("nutrition_information").getDouble("cholesterol_in_mg");
        int sodium_in_mg = recipieObj.getJSONObject("nutrition_information").getInt("sodium_in_mg");
        Number carbohydrates_in_grams = recipieObj.getJSONObject("nutrition_information").getDouble("carbohydrates_in_grams");
        Number protein_in_grams = recipieObj.getJSONObject("nutrition_information").getDouble("protein_in_grams");
        nutrition_information.setCalories(calories);
        nutrition_information.setCholesterol_in_mg(cholesterol_in_mg);
        nutrition_information.setSodium_in_mg(sodium_in_mg);
        nutrition_information.setCarbohydrates_in_grams(carbohydrates_in_grams);
        nutrition_information.setProtein_in_grams(protein_in_grams);

        newRecipie.setCook_time_in_min(cook_time_in_min);
        newRecipie.setPrep_time_in_min(prep_time_in_min);
        newRecipie.setTitle(title);
        newRecipie.setCusine(cusine);
        newRecipie.setServings(servings);
        newRecipie.setIngredients(ingredients);
        newRecipie.setSteps(steps);
        newRecipie.setNutrition_information(nutrition_information);
        newRecipie.setCreated_ts(getDatetime());
        newRecipie.setUpdated_ts(getDatetime());
        newRecipie.setTotal_time_in_min(cook_time_in_min+prep_time_in_min);

        recipieRepository.save(newRecipie);
        ObjectMapper mapper = new ObjectMapper();
        String newRecipieJSON = mapper.writeValueAsString(newRecipie);
        return new ResponseEntity<>(newRecipieJSON, HttpStatus.CREATED);
    }

    @RequestMapping(path = "/v1/recipie/{id}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> recipieGET(@PathVariable String id) throws IOException {

        Recipie recipie = recipieRepository.findById(id);
        ObjectMapper mapper = new ObjectMapper();
        String userJSON = mapper.writeValueAsString(recipie);
        return new ResponseEntity<>(userJSON,HttpStatus.OK) ;
    }
}


