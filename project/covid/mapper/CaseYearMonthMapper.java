package com.adacho.mapper;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.adacho.util.Covid;
import com.adacho.util.YearMonth;

public class CaseYearMonthMapper extends Mapper<LongWritable, Text, YearMonth, IntWritable> {
    private final IntWritable outVal = new IntWritable(1);
    private YearMonth outKey = new YearMonth();

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, YearMonth, IntWritable>.Context context) throws IOException, InterruptedException {
        Covid covid = new Covid();
        covid.parseText(value);

        outKey.set(Integer.toString(covid.getYear()));
        outKey.setMonth(covid.getMonth());

        context.write(outKey, outVal);
    }
}