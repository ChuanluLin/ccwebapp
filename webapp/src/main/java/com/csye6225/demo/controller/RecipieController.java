package com.csye6225.demo.controller;

import com.csye6225.demo.exception.DataValidationException;
import com.csye6225.demo.pojo.NutritionInformation;
import com.csye6225.demo.pojo.OrderedList;
import com.csye6225.demo.pojo.Recipie;
import com.csye6225.demo.pojo.User;
import com.csye6225.demo.repository.RecipieRepository;
import com.csye6225.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class RecipieController {
    @Autowired
    private RecipieRepository recipieRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping(path = "/v1/recipie/", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> createRecipie(@RequestBody String recipieJSON, HttpServletResponse response) throws IOException, JSONException{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JSONObject recipieObj = new JSONObject(recipieJSON);
        Recipie newRecipie = new Recipie();

        User user = userRepository.findByEmail(auth.getName());
        String userid = user.getId();
        newRecipie.setAuthor_id(userid);
        int cook_time_in_min = (int) recipieObj.getInt("cook_time_in_min");
        int prep_time_in_min = (int) recipieObj.getInt("prep_time_in_min");
        //cook time multiple of 5
        if(!(cook_time_in_min % 5 == 0) || !(prep_time_in_min % 5 == 0) ){
//            return new ResponseEntity<>("Cook or prep time should multiple of 5!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Cook or prep time should multiple of 5!", "/v1/recipie/");
        }
        String title = recipieObj.getString("title");
        String cusine = recipieObj.getString("cusine");
        int servings = recipieObj.getInt("servings");
        // 1 <= servings <=5
        if(!(servings >= 1 && servings <= 5)){
//            return new ResponseEntity<>("Servings should be from 1 to 5!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Servings should be from 1 to 5!", "/v1/recipie/");
        }
        //Ingredients: Set, avoid saving duplicate items
        Set<String> ingredients = new HashSet<String>();
        JSONArray ingArray  = recipieObj.getJSONArray("ingredients");
        int len = ingArray.length();
        for(int i = 0; i < len; i++){
            ingredients.add(ingArray.getString(i));
        }
        //Steps
        List<OrderedList> steps = new ArrayList<OrderedList>();
        int size = recipieObj.getJSONArray("steps").length();
        for (int i = 0; i < size; i++) {
            int position = recipieObj.getJSONArray("steps").getJSONObject(i).getInt("position");
            if (position < 1) {
//                return new ResponseEntity<>("Position no less than 1!", HttpStatus.BAD_REQUEST);
                throw new DataValidationException(getDatetime(), 400, "Bad Request", "Position cannot be less than 1!", "/v1/recipie/");
            }
            String items = recipieObj.getJSONArray("steps").getJSONObject(i).getString("items");
            OrderedList order = new OrderedList();
            order.setPosition(position);
            order.setItems(items);
            steps.add(order);
        }
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

    @PutMapping(path = "/v1/recipie/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> recipieUpdate(@PathVariable("id") String id, @RequestBody String recipieJSON, HttpServletResponse response) throws IOException, JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Recipie newRecipie = recipieRepository.findById(id);
        if(newRecipie == null){
//            return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found", "/v1/recipie/"+id);
        }

        String authorId = newRecipie.getAuthor_id();
        User user = userRepository.findByEmail(auth.getName());
        String userId = user.getId();
        if(!userId.equals(authorId)){
//            return new ResponseEntity<>("Cannot change other's recipe.", HttpStatus.UNAUTHORIZED);
            throw new DataValidationException(getDatetime(), 401, "Unauthorized", "Cannot change other's recipe.", "/v1/recipie/"+id);
        }

        JSONObject recipieObj = new JSONObject(recipieJSON);
        int cook_time_in_min = (int) recipieObj.getInt("cook_time_in_min");
        int prep_time_in_min = (int) recipieObj.getInt("prep_time_in_min");
        if(!(cook_time_in_min % 5 == 0) || !(prep_time_in_min % 5 == 0) ){
//            return new ResponseEntity<>("Cook or prep time should multiple of 5!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Cook or prep time should multiple of 5!", "/v1/recipie/"+id);
        }
        String title = recipieObj.getString("title");
        String cusine = recipieObj.getString("cusine");
        int servings = recipieObj.getInt("servings");
        if(!(servings >= 1 && servings <= 5)){
//            return new ResponseEntity<>("Servings should be from 1 to 5!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Servings should be from 1 to 5!", "/v1/recipie/"+id);
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
        int size = recipieObj.getJSONArray("steps").length();
        for (int i = 0; i < size; i++) {
            int position = recipieObj.getJSONArray("steps").getJSONObject(i).getInt("position");
            if (position < 1) {
//                return new ResponseEntity<>("Position no less than 1!", HttpStatus.BAD_REQUEST);
                throw new DataValidationException(getDatetime(), 400, "Bad Request", "Position no less than 1!", "/v1/recipie/"+id);
            }
            String items = recipieObj.getJSONArray("steps").getJSONObject(i).getString("items");
            OrderedList order = new OrderedList();
            order.setPosition(position);
            order.setItems(items);
            steps.add(order);
        }
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
        newRecipie.setUpdated_ts(getDatetime());
        newRecipie.setTotal_time_in_min(cook_time_in_min+prep_time_in_min);

        recipieRepository.save(newRecipie);
        ObjectMapper mapper = new ObjectMapper();
        String newRecipieJSON = mapper.writeValueAsString(newRecipie);
        return new ResponseEntity<>(newRecipieJSON, HttpStatus.OK);
    }

    @DeleteMapping(path = "/v1/recipie/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> recipieDelete(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Recipie recipie = recipieRepository.findById(id);

        if(recipie == null){
//            return new ResponseEntity<>("Recipe Not Found", HttpStatus.NOT_FOUND);
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found", "/v1/recipie/"+id);
        }

        String authorId = recipie.getAuthor_id();
        User user = userRepository.findByEmail(auth.getName());
        String userId = user.getId();
        if(!userId.equals(authorId)){
//            return new ResponseEntity<>("Cannot delete other's recipe.", HttpStatus.UNAUTHORIZED);
            throw new DataValidationException(getDatetime(), 401, "Unauthorized", "Cannot delete other's recipe.", "/v1/recipie/"+id);
        }

        recipieRepository.delete(recipie);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(path = "/v1/recipie/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> recipieGET(@PathVariable("id") String id) throws IOException {
        Recipie recipie = recipieRepository.findById(id);
        if(recipie == null){
//            return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found", "/v1/recipie/"+id);
        }
        ObjectMapper mapper = new ObjectMapper();
        String userJSON = mapper.writeValueAsString(recipie);
        return new ResponseEntity<>(userJSON,HttpStatus.OK) ;
    }

    public String getDatetime() {
        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String dateString = format.format(currentTime);
        return dateString;
    }

}
