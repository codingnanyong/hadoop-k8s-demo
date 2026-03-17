package com.adacho.mapper;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.adacho.util.Covid;

public class CaseMultiMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
   private final IntWritable outVal = new IntWritable(1);
   private Text outKey = new Text();

   @Override
   protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        Covid covid = new Covid();
        covid.parseText(value);

        outKey.set(covid.getYear() + "," + covid.getMonth());
        if(covid.getGender() == Gender.MALE) {
            context.getCounter(CovidCounter.GENDER_MALE).increment(1);
        } else if(covid.getGender() == Gender.FEMALE) {
            context.getCounter(CovidCounter.GENDER_FEMALE).increment(1);
        } else {
            context.getCounter(CovidCounter.GENDER_OTHERS).increment(1);
        }
        context.write(outKey, outVal);

        if(covid.getAge() != AgeGroup.AGENA) {
            outKey.set("A/" + covid.getAge().getCommentary());
            context.write(outKey, outVal);
        } else {
            context.getCounter(CovidCounter.NOT_AVAILABLE_AGE).increment(1);
        }
   }
}