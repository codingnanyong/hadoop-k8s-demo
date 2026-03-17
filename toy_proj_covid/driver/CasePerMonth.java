package com.adacho.driver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.adacho.mapper.CasePerMonthMapper;
import com.adacho.reducer.CasePerMonthReducer;

public class CasePerMonth {

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();

        if (args.length != 2) {
            System.err.println("Usage: CasePerMonth <input-dir> <output-dir>");
            System.exit(1);
        }

        Job job = Job.getInstance(conf, "CasePerMonth");

        job.setJarByClass(CasePerMonth.class);
        job.setMapperClass(CasePerMonthMapper.class);
        job.setReducerClass(CasePerMonthReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}