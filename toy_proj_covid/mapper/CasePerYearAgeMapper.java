package com.adacho.mapper;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.adacho.util.Covid;
import com.adacho.util.AgeGroup;

public class CasePerYearAgeMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final IntWritable outValue = new IntWritable(1);
    private Text outKey = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        // 1. 한 줄 데이터를 Covid 객체로 파싱
        Covid covid = new Covid();
        covid.parseText(value);

        // 2. NA 연령 제외
        if (covid.getAge() != null && covid.getAge() != AgeGroup.AGENA) {

            // 3. 출력 Key 형식: "년도,age_group"
            // 예: 2020,20 - 29 Years
            outKey.set(covid.getYear() + "," + covid.getAge().getCommentary());

            // 4. (년도,연령) 1건 발생
            context.write(outKey, outValue);
        }
    }
}