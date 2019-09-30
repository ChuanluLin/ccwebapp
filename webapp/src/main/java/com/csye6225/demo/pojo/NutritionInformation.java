package com.csye6225.demo.pojo;

import javax.persistence.Embeddable;

@Embeddable
public class NutritionInformation {
    private int calories;
    private Number cholesterol_in_mg;
    private int sodium_in_mg;
    private Number carbohydrates_in_grams;
    private Number protein_in_grams;

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public Number getCholesterol_in_mg() {
        return cholesterol_in_mg.floatValue();
    }

    public void setCholesterol_in_mg(Number cholesterol_in_mg) {
        this.cholesterol_in_mg = cholesterol_in_mg;
    }

    public int getSodium_in_mg() {
        return sodium_in_mg;
    }

    public void setSodium_in_mg(int sodium_in_mg) {
        this.sodium_in_mg = sodium_in_mg;
    }

    public Number getCarbohydrates_in_grams() {
        return carbohydrates_in_grams.floatValue();
    }

    public void setCarbohydrates_in_grams(Number carbohydrates_in_grams) {
        this.carbohydrates_in_grams = carbohydrates_in_grams;
    }

    public Number getProtein_in_grams() {
        return protein_in_grams.floatValue();
    }

    public void setProtein_in_grams(Number protein_in_grams) {
        this.protein_in_grams = protein_in_grams;
    }
}
