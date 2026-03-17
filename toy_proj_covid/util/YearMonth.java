package com.adacho.util;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableUtils;

public class YearMonth implements WritableComparable<YearMonth> {
    private String year;
    private Integer month;

    public YearMonth() {
    }

    public YearMonth(String year, Integer month) {
        this.year = year;
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        WritableUtils.writeString(out, year);
        out.writeInt(month);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        year = WritableUtils.readString(in);
        month = in.readInt();
    }

    @Override
    public int compareTo(YearMonth o) {
        int result = year.compareTo(o.year);
        if(result == 0) {
            return month.compareTo(o.month);
        }
        return 0;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(year).append(",").append(month).toString();
    }
}