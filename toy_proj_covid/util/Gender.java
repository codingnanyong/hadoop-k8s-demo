package com.adacho.util;

public enum Gender {
    MALE, FEMALE, OTHERS;

    private Static Gender getGender(String gender) {
        if (gender.equals("MALE")) {
            return Gender.MALE;
        } else if (gender.equals("FEMALE")) {
            return Gender.FEMALE;
        } else {
            return Gender.OTHERS;
        }
    }
}