package com.adacho.driver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import com.adacho.mapper.CaseYearMonthMapper;
import com.adacho.reducer.YearMonthReducer;
import com.adacho.util.YearMonthPartitioner;
import com.adacho.util.YearMonthGroupComparator;
import com.adacho.util.YearMonthComparator;

public class CaseYearMonth {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        if(args.length != 2) {
            System.out.println("Usage: CaseYearMonth <input-dir> <output-dir>");
            System.exit(1);
        }

        Job job = Job.getInstance(conf, "CaseYearMonth");

        job.setJarByClass(CaseYearMonth.class);
        job.setMapperClass(CaseYearMonthMapper.class);
        job.setReducerClass(YearMonthReducer.class);

        job.setPartitionerClass(YearMonthPartitioner.class);
        job.setGroupingComparatorClass(YearMonthGroupComparator.class);
        job.setSortComparatorClass(YearMonthComparator.class);

        job.setMapOutputKeyClass(YearMonth.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(YearMonth.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}