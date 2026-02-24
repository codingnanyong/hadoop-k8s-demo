package com.adacho.reducer;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class CasePerMonthReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private IntWritable outVal = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values,
                          Context context)
            throws IOException, InterruptedException {

        int sum = 0;

        for (IntWritable value : values) {
            sum += value.get();
        }

        outVal.set(sum);
        context.write(key, outVal);
    }
}