package com.adacho.util;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

public class YearMonthPartitioner extends Partitioner<YearMonth, IntWritable> {

    @Override
    public int getPartition(YearMonth key, IntWritable value, int numPartitions) {
        return Math.abs(key.getYear().hashCode() % numPartitions);
    }
}