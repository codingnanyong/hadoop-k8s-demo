package com.adacho.reducer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class CaseMultiReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable outVal = new IntWritable();
    private Text OutputKey = new Text();
    private MultipleOutputs<Text, IntWritable> mOutput;

    @Override
    protected void setup(Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        mOutput = new MultipleOutputs<Text, IntWritable>(context);
    }

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        String[] tokens = key.toString().split("/");
        OutputKey.set(tokens[1]);

        int sum = 0;
        for(IntWritable value : values) {
            sum += value.get();
        }
        outVal.set(sum);

        if(tokens[0].equals("M")) {
            mOutput.write("month", OutputKey, outVal);
        } else {
            mOutput.write("age", OutputKey, outVal);
        }

        context.write(key, outVal);
    }

    @Override
    protected void cleanup(Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        mOutput.close();
    }
}