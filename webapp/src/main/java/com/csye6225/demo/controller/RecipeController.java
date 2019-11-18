package com.csye6225.demo.controller;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.csye6225.demo.exception.DataValidationException;
import com.csye6225.demo.exception.RequestLimit;
import com.csye6225.demo.pojo.*;
import com.csye6225.demo.repository.RecipeRepository;
import com.csye6225.demo.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timgroup.statsd.StatsDClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
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
public class RecipeController {
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    private final StatsDClient statsd;

    @Value("${aws.access.key}")
    private String AWS_ACCESS_KEY;
    @Value("${aws.secret.key}")
    private String AWS_SECRET_KEY;
    @Value("${aws.bucketname}")
    private String bucketName;

    @Autowired
    public RecipeController(StatsDClient statsd) {
        this.statsd = statsd;
    }

    @PostMapping(path = "/v1/recipe/", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> createRecipe(@RequestBody String recipeJSON, HttpServletResponse response) throws IOException, JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JSONObject recipeObj = new JSONObject(recipeJSON);
        Recipe newRecipe = new Recipe();

        User user = userRepository.findByEmail(auth.getName());
        String userid = user.getId();
        newRecipe.setAuthor_id(userid);
        int cook_time_in_min;
        int prep_time_in_min;
        int servings;
        int calories;
        Number cholesterol_in_mg;
        int sodium_in_mg;
        Number carbohydrates_in_grams;
        Number protein_in_grams;
        try {
            cook_time_in_min = (int) recipeObj.getInt("cook_time_in_min");
            prep_time_in_min = (int) recipeObj.getInt("prep_time_in_min");
            servings = recipeObj.getInt("servings");
            //Nutrition
            calories = recipeObj.getJSONObject("nutrition_information").getInt("calories");
            cholesterol_in_mg = recipeObj.getJSONObject("nutrition_information").getDouble("cholesterol_in_mg");
            sodium_in_mg = recipeObj.getJSONObject("nutrition_information").getInt("sodium_in_mg");
            carbohydrates_in_grams = recipeObj.getJSONObject("nutrition_information").getDouble("carbohydrates_in_grams");
            protein_in_grams = recipeObj.getJSONObject("nutrition_information").getDouble("protein_in_grams");
        } catch (Exception e) {
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Format error!");
        }
        //cook time multiple of 5
        if (!(cook_time_in_min % 5 == 0) || !(prep_time_in_min % 5 == 0)) {
//            return new ResponseEntity<>("Cook or prep time should multiple of 5!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Cook or prep time should multiple of 5!");
        }
        // 1 <= servings <=5
        if (!(servings >= 1 && servings <= 5)) {
//            return new ResponseEntity<>("Servings should be from 1 to 5!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Servings should be from 1 to 5!");
        }

        String title = recipeObj.getString("title");
        String cuisine = recipeObj.getString("cuisine");
        //Ingredients: Set, avoid saving duplicate items
        Set<String> ingredients = new HashSet<String>();
        JSONArray ingArray = recipeObj.getJSONArray("ingredients");
        int len = ingArray.length();
        for (int i = 0; i < len; i++) {
            ingredients.add(ingArray.getString(i));
        }
        //Steps
        List<OrderedList> steps = new ArrayList<OrderedList>();
        int size = recipeObj.getJSONArray("steps").length();
        for (int i = 0; i < size; i++) {
            int position;
            try {
                position = recipeObj.getJSONArray("steps").getJSONObject(i).getInt("position");
            } catch (Exception e) {
                throw new DataValidationException(getDatetime(), 400, "Bad Request", "Format error!");
            }
            if (position < 1) {
//                return new ResponseEntity<>("Position no less than 1!", HttpStatus.BAD_REQUEST);
                throw new DataValidationException(getDatetime(), 400, "Bad Request", "Position cannot be less than 1!");
            }
            String items = recipeObj.getJSONArray("steps").getJSONObject(i).getString("items");
            OrderedList order = new OrderedList();
            order.setPosition(position);
            order.setItems(items);
            steps.add(order);
        }
        //Nutrition
        NutritionInformation nutrition_information = new NutritionInformation();
        nutrition_information.setCalories(calories);
        nutrition_information.setCholesterol_in_mg(cholesterol_in_mg);
        nutrition_information.setSodium_in_mg(sodium_in_mg);
        nutrition_information.setCarbohydrates_in_grams(carbohydrates_in_grams);
        nutrition_information.setProtein_in_grams(protein_in_grams);
        //Image
//        Image image = new Image();
//        image.setFilename("");
//        image.setUrl("");
//        String uuid = UUID.randomUUID().toString().replaceAll("-","");
//        image.setImageid(uuid);

        newRecipe.setCook_time_in_min(cook_time_in_min);
        newRecipe.setPrep_time_in_min(prep_time_in_min);
        newRecipe.setTotal_time_in_min(cook_time_in_min + prep_time_in_min);
        newRecipe.setTitle(title);
        newRecipe.setCusine(cuisine);
        newRecipe.setIngredients(ingredients);
        newRecipe.setServings(servings);
        newRecipe.setSteps(steps);
        newRecipe.setNutrition_information(nutrition_information);
//        newRecipe.setImage(image);
        newRecipe.setCreated_ts(getDatetime());
        newRecipe.setUpdated_ts(getDatetime());

        recipeRepository.save(newRecipe);
        ObjectMapper mapper = new ObjectMapper();
        String newRecipeJSON = mapper.writeValueAsString(newRecipe);
        return new ResponseEntity<>(newRecipeJSON, HttpStatus.CREATED);
    }

    @PutMapping(path = "/v1/recipe/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> recipeUpdate(@PathVariable("id") String id, @RequestBody String recipeJSON, HttpServletResponse response) throws IOException, JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Recipe newRecipe = recipeRepository.findById(id);
        if (newRecipe == null) {
//            return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found");
        }

        String authorId = newRecipe.getAuthor_id();
        User user = userRepository.findByEmail(auth.getName());
        String userId = user.getId();
        if (!userId.equals(authorId)) {
//            return new ResponseEntity<>("Cannot change other's recipe.", HttpStatus.UNAUTHORIZED);
            throw new DataValidationException(getDatetime(), 401, "Unauthorized", "Cannot change other's recipe.");
        }

        JSONObject recipeObj = new JSONObject(recipeJSON);
        int cook_time_in_min;
        int prep_time_in_min;
        int servings;
        int calories;
        Number cholesterol_in_mg;
        int sodium_in_mg;
        Number carbohydrates_in_grams;
        Number protein_in_grams;
        try {
            cook_time_in_min = (int) recipeObj.getInt("cook_time_in_min");
            prep_time_in_min = (int) recipeObj.getInt("prep_time_in_min");
            servings = recipeObj.getInt("servings");
            //Nutrition
            calories = recipeObj.getJSONObject("nutrition_information").getInt("calories");
            cholesterol_in_mg = recipeObj.getJSONObject("nutrition_information").getDouble("cholesterol_in_mg");
            sodium_in_mg = recipeObj.getJSONObject("nutrition_information").getInt("sodium_in_mg");
            carbohydrates_in_grams = recipeObj.getJSONObject("nutrition_information").getDouble("carbohydrates_in_grams");
            protein_in_grams = recipeObj.getJSONObject("nutrition_information").getDouble("protein_in_grams");
        } catch (Exception e) {
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Format error!");
        }

        //cook time multiple of 5
        if (!(cook_time_in_min % 5 == 0) || !(prep_time_in_min % 5 == 0)) {
//            return new ResponseEntity<>("Cook or prep time should multiple of 5!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Cook or prep time should multiple of 5!");
        }
        // 1 <= servings <=5
        if (!(servings >= 1 && servings <= 5)) {
//            return new ResponseEntity<>("Servings should be from 1 to 5!", HttpStatus.BAD_REQUEST);
            throw new DataValidationException(getDatetime(), 400, "Bad Request", "Servings should be from 1 to 5!");
        }

        String title = recipeObj.getString("title");
        String cusine = recipeObj.getString("cusine");
        //Ingredients: Set, avoid saving duplicate items
        Set<String> ingredients = new HashSet<String>();
        JSONArray ingArray = recipeObj.getJSONArray("ingredients");
        int len = ingArray.length();
        for (int i = 0; i < len; i++) {
            ingredients.add(ingArray.getString(i));
        }
        //Steps
        List<OrderedList> steps = new ArrayList<OrderedList>();
        int size = recipeObj.getJSONArray("steps").length();
        for (int i = 0; i < size; i++) {
            int position;
            try {
                position = recipeObj.getJSONArray("steps").getJSONObject(i).getInt("position");
            } catch (Exception e) {
                throw new DataValidationException(getDatetime(), 400, "Bad Request", "Format error!");
            }
            if (position < 1) {
//                return new ResponseEntity<>("Position no less than 1!", HttpStatus.BAD_REQUEST);
                throw new DataValidationException(getDatetime(), 400, "Bad Request", "Position cannot be less than 1!");
            }
            String items = recipeObj.getJSONArray("steps").getJSONObject(i).getString("items");
            OrderedList order = new OrderedList();
            order.setPosition(position);
            order.setItems(items);
            steps.add(order);
        }
        //Nutrition
        NutritionInformation nutrition_information = new NutritionInformation();
        nutrition_information.setCalories(calories);
        nutrition_information.setCholesterol_in_mg(cholesterol_in_mg);
        nutrition_information.setSodium_in_mg(sodium_in_mg);
        nutrition_information.setCarbohydrates_in_grams(carbohydrates_in_grams);
        nutrition_information.setProtein_in_grams(protein_in_grams);

        newRecipe.setCook_time_in_min(cook_time_in_min);
        newRecipe.setPrep_time_in_min(prep_time_in_min);
        newRecipe.setTotal_time_in_min(cook_time_in_min + prep_time_in_min);
        newRecipe.setTitle(title);
        newRecipe.setCusine(cusine);
        newRecipe.setIngredients(ingredients);
        newRecipe.setServings(servings);
        newRecipe.setSteps(steps);
        newRecipe.setNutrition_information(nutrition_information);
        newRecipe.setUpdated_ts(getDatetime());

        recipeRepository.save(newRecipe);
        ObjectMapper mapper = new ObjectMapper();
        String newRecipeJSON = mapper.writeValueAsString(newRecipe);
        return new ResponseEntity<>(newRecipeJSON, HttpStatus.OK);
    }


    @DeleteMapping(path = "/v1/recipe/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> recipeDelete(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Recipe recipe = recipeRepository.findById(id);

        if (recipe == null) {
//            return new ResponseEntity<>("Recipe Not Found", HttpStatus.NOT_FOUND);
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found");
        }

        String authorId = recipe.getAuthor_id();
        User user = userRepository.findByEmail(auth.getName());
        String userId = user.getId();
        if (!userId.equals(authorId)) {
//            return new ResponseEntity<>("Cannot delete other's recipe.", HttpStatus.UNAUTHORIZED);
            throw new DataValidationException(getDatetime(), 401, "Unauthorized", "Cannot delete other's recipe.");
        }

        recipeRepository.delete(recipe);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @GetMapping(path = "/v1/recipe/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> recipeGET(@PathVariable("id") String id) throws IOException {
        Recipe recipe = recipeRepository.findById(id);
        if (recipe == null) {
//            return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found");
        }
        ObjectMapper mapper = new ObjectMapper();
        String recipeJSON = mapper.writeValueAsString(recipe);
        return new ResponseEntity<>(recipeJSON, HttpStatus.OK);
    }


    @GetMapping(path = "/v1/recipes", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> newestrecipeGET() throws IOException {
        List<Recipe> recipeList = recipeRepository.findInOrders();
        int len = recipeList.size();
        Recipe newRecipe = recipeList.get(len - 1);
        if (newRecipe == null) {
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found");
        }
        ObjectMapper mapper = new ObjectMapper();
        String recipeJSON = mapper.writeValueAsString(newRecipe);
        return new ResponseEntity<>(recipeJSON, HttpStatus.OK);
    }

    @RequestLimit
    @PostMapping(path = "/v1/myrecipes", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getMyRecipes() throws IOException, JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Recipe newRecipe = new Recipe();

        User user = userRepository.findByEmail(auth.getName());
        String userid = user.getId();
        List<Recipe> recipeList = recipeRepository.findByAuthor_id(userid);
        if (recipeList.size() <= 0) {
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found");
        }

        String recipes = "";
        for (Recipe r : recipeList) {
            ObjectMapper mapper = new ObjectMapper();
            String newRecipeJSON = mapper.writeValueAsString(r);
            recipes += newRecipeJSON;
        }


        AWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
        AmazonSNSClient snsClient = new AmazonSNSClient(awsCredentials);
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));

        // Create an Amazon SNS topic.
        CreateTopicRequest createTopicRequest = new CreateTopicRequest("email_request");
        CreateTopicResult createTopicResponse = snsClient.createTopic(createTopicRequest);
        // Print the topic ARN.
        System.out.println("TopicArn:" + createTopicResponse.getTopicArn());
        // Print the request ID for the CreateTopicRequest action.
        System.out.println("CreateTopicRequest: " + snsClient.getCachedResponseMetadata(createTopicRequest));

        // Publish a message to an Amazon SNS topic.
        String msg = "If you receive this message, publishing a message to an Amazon SNS topic works.";
        PublishRequest publishRequest = new PublishRequest(createTopicResponse.getTopicArn(), msg);
        PublishResult publishResponse = snsClient.publish(publishRequest);

        // Print the MessageId of the message.
        System.out.println("MessageId: " + publishResponse.getMessageId());


        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }


    public String getDatetime() {
        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String dateString = format.format(currentTime);
        return dateString;
    }
}
