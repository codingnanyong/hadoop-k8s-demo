package com.adacho.reducer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.adacho.util.YearMonth;

public class YearMonthReducer extends Reducer<YearMonth, IntWritable, Text, IntWritable> {
    private IntWritable outVal = new IntWritable();
    private YearMonth outKey = new YearMonth();

    @Override
    protected void reduce(YearMonth key, Iterable<IntWritable> values, Reducer<YearMonth, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        int sum = 0;

        Integer currentMonth = key.getMonth();

        for(IntWritable value : values) {
            if(currentMonth != key.getMonth()) {
                outVal.set(sum);
                outKey.setYear(key.getYear());
                outKey.setMonth(currentMonth);
                context.write(outKey, outVal);
                sum = 0;
            }
            sum += value.get();
            currentMonth = key.getMonth();
        }
        if(currentMonth == key.getMonth()) {
            outVal.set(sum);
            outKey.setYear(key.getYear());
            outKey.setMonth(key.getMonth());
            context.write(outKey, outVal);
        }
    }
}