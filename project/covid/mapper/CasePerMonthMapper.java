package com.adacho.mapper;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class CasePerMonthMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private final IntWritable outVal = new IntWritable(1);
    private Text outKey = new Text();

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        Covid covid = new Covid();
        covid.parseText(value);

       outKey.set(covid.getYear() + "," + covid.getMonth());
       context.write(outKey, outVal);
    }
}