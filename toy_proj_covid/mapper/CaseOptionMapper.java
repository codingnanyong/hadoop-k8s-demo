package com.adacho.mapper;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.adacho.util.Covid;

public class CaseOptionMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private final IntWritable outVal = new IntWritable(1);
    private Text outKey = new Text();
    private String jobType;

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        jobType = context.getConfiguration().get("job.type");
    }

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        Covid covid = new Covid();
        covid.parseText(value);

        if(jobType.equals("month")) {
            outKey.set(covid.getYear() + "," + covid.getMonth());
        } else if(jobType.equals("age")) {
            outKey.set(covid.getAge().getCommentary());
        }

        context.write(outKey, outVal);
    }
}