package com.adacho.driver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.conf.GenericOptionsParser;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import com.adacho.mapper.CaseOptionMapper;
import com.adacho.reducer.CasePerMonthReducer;

public class CaseOption extends Configured implements Tool {
    public static void main(String[] args) throws Exception {
        int result = ToolRunner.run(new CaseOption(), new CaseOption(),args);
    }
    @Override
    public int run(String[] args) throws Exception {
       String[] remainArgs = new GenericOptionsParser(getConf(), args).getRemainingArgs();

       if(remainArgs.length != 2) {
        System.out.println("Usage: CaseOption <input-dir> <output-dir>");
        System.exit(1);
       }

       Job job = Job.getInstance(getConf(), "CaseOption");

       job.setJarByClass(CaseOption.class);
       job.setMapperClass(CaseOptionMapper.class);
       job.setReducerClass(CasePerMonthReducer.class);

       job.setInputFormatClass(TextInputFormat.class);
       job.setOutputFormatClass(TextOutputFormat.class);

       job.setInputFormatClass(TextInputFormat.class);
       job.setOutputFormatClass(TextOutputFormat.class);

       job.setOutputKeyClass(Text.class);
       job.setOutputValueClass(IntWritable.class);

       FileInputFormat.addInputPath(job, new Path(remainArgs[0]));
       FileOutputFormat.setOutputPath(job, new Path(remainArgs[1]));

       return job.waitForCompletion(true) ? 0 : 1;
    }
}