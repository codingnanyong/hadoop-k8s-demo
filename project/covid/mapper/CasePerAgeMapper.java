package com.adacho.mapper;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.adacho.util.Covid;

public class CasePerAgeMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private final IntWritable outVal = new IntWritable(1);
    private Text outKey = new Text();

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        Covid covid = new Covid();
        covid.parseText(value);

        outKey.set(covid.getAge().getCommentary());
        context.write(outKey, outVal);
    }
}