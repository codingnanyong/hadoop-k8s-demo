package com.adacho.driver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

public class CaseMulti extends Configured implements Tool {
    public static void main(String[] args) throws Exception {
        int result = ToolRunner.run(new Configuration(), new CaseMulti(), args);
    }

    @Override
    public int run(String[] args) throws Exception {
        String[] remainArgs = new GenericOptionsParser(getConf(), args).getRemainingArgs();
        if(remainArgs.length != 2) {
            System.out.println("Usage: CaseMulti <input-dir> <output-dir>");
            System.exit(1);
        }

        Job job = Job.getInstance(getConf(), "CaseMulti");

        job.setJarByClass(CaseMulti.class);
        job.setMapperClass(CaseMultiMapper.class);
        job.setReducerClass(CaseMultiReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(remainArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(remainArgs[1]));

        MultipleOutputs.addNamedOutput(job, "month", TextOutputFormat.class, Text.class, IntWritable.class);
        MultipleOutputs.addNamedOutput(job, "age", TextOutputFormat.class, Text.class, IntWritable.class);

        return 0;
    }
}