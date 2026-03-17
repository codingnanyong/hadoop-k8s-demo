package com.adacho.util;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class YearMonthComparator extends WritableComparator {
    protected YearMonthComparator() {
        super(YearMonth.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        YearMonth key1 = (YearMonth) a;
        YearMonth key2 = (YearMonth) b;

        int result = key1.getYear().compareTo(key2.getYear());
        if(result != 0) {
            return result;
        }
        return key1.getMonth() == key2.getMonth() ? 0 : (key1.getMonth() < key2.getMonth() ? -1 : 1);
    }
}