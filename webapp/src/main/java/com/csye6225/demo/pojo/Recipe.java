package com.csye6225.demo.pojo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.StreamingHttpOutputMessage;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="recipe")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
public class Recipe {
    @Column(name = "image")
    private Image image;

    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name="id", unique = true, nullable = false, length = 32)
    private String id;

    @Column(name = "created_ts", nullable = false)
    private String created_ts;

    @Column(name = "updated_ts", nullable = false)
    private String updated_ts;

    @Column(name = "author_id", nullable = false)
    private String author_id;

    @Column(name = "cook_time_in_min", nullable = false)
    private int cook_time_in_min;

    @Column(name = "prep_time_in_min", nullable = false)
    private int prep_time_in_min;

    @Column(name = "total_time_in_min", nullable = false)
    private int total_time_in_min;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "cusine", nullable = false)
    private String cusine;

//    @Range(min = 1, max = 5, message = "Range should between 1 and 5.")
    @Column(name = "servings", nullable = false)
    private int servings;

    @ElementCollection(fetch=FetchType.LAZY,
            targetClass=String.class)
   // @CollectionTable(name="ingredients")
    @Column(name = "ingredients", nullable = false)
    private Set<String> ingredients = new HashSet<String>();

    @ElementCollection(fetch=FetchType.LAZY,
            targetClass=OrderedList.class)
    @CollectionTable(name="orderedlist")
    @OrderColumn(name="steps", nullable = false)
    private List<OrderedList> steps = new ArrayList<OrderedList>();

    @Column(name = "nutrition_information", nullable = false)
    private NutritionInformation nutrition_information;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated_ts() {
        return created_ts;
    }

    public void setCreated_ts(String created_ts) {
        this.created_ts = created_ts;
    }

    public String getUpdated_ts() {
        return updated_ts;
    }

    public void setUpdated_ts(String updated_ts) {
        this.updated_ts = updated_ts;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public int getCook_time_in_min() {
        return cook_time_in_min;
    }

    public void setCook_time_in_min(int cook_time_in_min) {
        this.cook_time_in_min = cook_time_in_min;
    }

    public int getPrep_time_in_min() {
        return prep_time_in_min;
    }

    public void setPrep_time_in_min(int prep_time_in_min) {
        this.prep_time_in_min = prep_time_in_min;
    }

    public int getTotal_time_in_min() {
        return total_time_in_min;
    }

    public void setTotal_time_in_min(int total_time_in_min) {
        this.total_time_in_min = total_time_in_min;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCusine() {
        return cusine;
    }

    public void setCusine(String cusine) {
        this.cusine = cusine;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public Set<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Set<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<OrderedList> getSteps() {
        return steps;
    }

    public void setSteps(List<OrderedList> steps) {
        this.steps = steps;
    }

    public NutritionInformation getNutrition_information() {
        return nutrition_information;
    }

    public void setNutrition_information(NutritionInformation nutrition_information) {
        this.nutrition_information = nutrition_information;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
