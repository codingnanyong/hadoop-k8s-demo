package com.adacho.driver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import com.adacho.mapper.CaseOptionCounterMapper;
import com.adacho.reducer.CasePerMonthReducer;

public class CaseOptionCounter extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        int result = ToolRunner.run(new CaseOptionCounter(), new CaseOptionCounter(),args);
        System.out.println("Result Code" + result);
    }

    @Override
    public int run(String[] args) throws Exception {
        String[] remainArgs = new GenericOptionsParser(getConf(), args).getRemainingArgs();

        if(remainArgs.length != 2) {
            System.out.println("Usage: CaseOptionCounter <input-dir> <output-dir>");
            System.exit(1);
        }

        Job job = Job.getInstance(getConf(), "CaseOptionCounter");
        job.setJarByClass(CaseOptionCounter.class);
        job.setMapperClass(CaseOptionCounterMapper.class);
        job.setReducerClass(CasePerMonthReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(remainArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(remainArgs[1]));

        return 0;
    }
}