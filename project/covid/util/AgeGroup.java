package com.adacho.util;

public enum AgeGroup {

    AGE0TO9("0 - 9 Years"),
    AGE10TO19("10 - 19 Years"),
    AGE20TO29("20 - 29 Years"),
    AGE30TO39("30 - 39 Years"),
    AGE40TO49("40 - 49 Years"),
    AGE50TO59("50 - 59 Years"),
    AGE60TO69("60 - 69 Years"),
    AGE70TO79("70 - 79 Years"),
    AGE80PLUS("80 + Years"),
    AGENA("NA");

    private String commentary;

    private AgeGroup(String commentary) {
        this.commentary = commentary;
    }

    public String getCommentary() {
        return commentary;
    }

    public static AgeGroup getAgebyCommentary(String commentary) {
        for (AgeGroup age : AgeGroup.values()) {
            if (age.getCommentary().equals(commentary)) {
                return age;
            }
        }
        return AgeGroup.AGENA;
    }
}