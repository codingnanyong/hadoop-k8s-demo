package com.adacho.util;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class YearMonthGroupComparator extends WritableComparator {
    
    protected YearMonthGroupComparator() {
        super(YearMonth.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        YearMonth key1 = (YearMonth) a;
        YearMonth key2 = (YearMonth) b;
        
        return key1.getYear().compareTo(key2.getYear());
    }
}