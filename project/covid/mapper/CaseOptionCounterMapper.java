package com.adacho.mapper;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.adacho.util.Covid;
import com.adacho.util.Gender;
import com.adacho.util.CovidCounter;

public class CaseOptionCounterMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private String jobType;
    private final IntWritable outVal = new IntWritable(1);
    private Text outKey = new Text();

     @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        Covid covid = new Covid();
        covid.parseText(value);

        if(jobType.equals("month")) {
            outKey.set(covid.getYear() + "," + covid.getMonth());
            context.write(outKey, outVal);
            if(covid.getGender() == Gender.MALE) {
                context.getCounter(CovidCounter.GENDER_MALE).increment(1);
            } else if(covid.getGender() == Gender.FEMALE) {
                context.getCounter(CovidCounter.GENDER_FEMALE).increment(1);
            } else {
                context.getCounter(CovidCounter.GENDER_OTHERS).increment(1);
            }
        } else if(jobType.equals("age")) {
           if(covid.getAge() != AgeGroup.AGENA) {
            outKey.set(covid.getAge().getCommentary());
            context.write(outKey, outVal);
           }
           else {
            context.getCounter(CovidCounter.NOT_AVAILABLE_AGE).increment(1);
           }
        }
    }

    @Override
    protected void setup(Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        jobType = context.getConfiguration().get("job.type");
    }
}