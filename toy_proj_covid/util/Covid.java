package com.adacho.util;

import org.apache.hadoop.io.Text;

public class Covid {

    private int year;
    private int month;
    private int day;

    private AgeGroup age = null;

    public void parseText(Text line) {

        try {
            String[] columns = line.toString().split(",");

            String[] caseDate = columns[0].split("/");

            year = Integer.parseInt(caseDate[0]);
            month = Integer.parseInt(caseDate[1]);
            day = Integer.parseInt(caseDate[2]);

            age = AgeGroup.getAgebyCommentary(columns[6]);
            gender = Gender.getGender(columns[5]);

        } catch (Exception e) {
            // ignore malformed line
        }
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public AgeGroup getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }
}