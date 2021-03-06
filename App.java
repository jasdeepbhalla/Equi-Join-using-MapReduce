// Author: Jasdeep Singh

//package dds.assignment4;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * main!
 * 
 */
public class App {

	public static String table1Name = "";
	public static String table2Name = "";

	public static class Map extends Mapper<Text, Text, Text, Text> {

		private Text joinColumn = new Text();
		private Text valueLine = new Text();

		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {

			String line = key.toString();
			String[] lineArr = key.toString().split(",");

			if (line != null) {
				if (table1Name.isEmpty())
					table1Name = lineArr[0];
				else {
					if (!table1Name.equals(lineArr[0])) {
						table2Name = lineArr[0];
					}
				}
			}

			joinColumn.set(lineArr[1]);
			valueLine.set(line);
			context.write(joinColumn, valueLine);

		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		public Text joinResult = new Text();

		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			ArrayList<String> table1 = new ArrayList<String>();
			ArrayList<String> table2 = new ArrayList<String>();

			for (Text i : values) {
				String[] s = i.toString().split(",");
				if (s != null && s[0].trim().equals(table1Name)) {
					table1.add(i.toString());
				} else if (s != null && s[0].trim().equals(table2Name)) {
					table2.add(i.toString());
				}

			}

			String strData = "";
			for (int i = 0; i < table1.size(); i++) {

				for (int j = 0; j < table2.size(); j++) {

					strData = table1.get(i) + ", " + table2.get(j);
					joinResult.set(strData);
					context.write(null, joinResult);
					joinResult.clear();
				}
			}

		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "equiJoin");
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}

}
