package eu.larkc.plugin.hadoop.filterplugin;

import io.files.readers.NTriplesReader;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.OutputLogFilter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterJob extends Configured implements Tool {

	private static Logger log = LoggerFactory.getLogger(FilterJob.class);
	
	//Parameters
	private int numReduceTasks = 1;
	private int numMapTasks = 1;

	public int run(String[] args) throws Exception {
		parseArgs(args);

		filter(args[0], args[1], args[2]);

		return 0;
	}

	private Job createNewJob(String name) throws IOException {
		Configuration conf = new Configuration(this.getConf());
	    conf.setInt("maptasks", numMapTasks);
		
		Job job = new Job(conf);
		job.setJarByClass(FilterJob.class);
		job.setJobName(name);
		FileInputFormat.setInputPathFilter(job, OutputLogFilter.class);
		job.setNumReduceTasks(numReduceTasks);
		SequenceFileOutputFormat.setCompressOutput(job, true);
	    SequenceFileOutputFormat.setOutputCompressionType(job, CompressionType.BLOCK);
		
		return job;
	}
	
	public void parseArgs(String[] args) {
		
		for(int i=0;i<args.length; ++i) {
			
			if (args[i].equalsIgnoreCase("--maptasks")) {
				numMapTasks = Integer.valueOf(args[++i]);
			}
			
			if (args[i].equalsIgnoreCase("--reducetasks")) {
				numReduceTasks = Integer.valueOf(args[++i]);
			}
		}
	}
	
	public void filter(String input, String output, String statementPatterns) throws Exception {
		Job job = createNewJob("StatementPattern Filter");
		
		String[] sp=statementPatterns.split("\n");
		
		job.getConfiguration().setInt(FilterMapper.FILTER_MAPPER_NO_PATTERNS, sp.length);
		for (int i=0; i<sp.length; i++) {
			job.getConfiguration().set(FilterMapper.FILTER_MAPPER_PATTERN+i, sp[i]);
		}
		
	    //Input
	    FileInputFormat.addInputPath(job, new Path(input));
	    job.setInputFormatClass(NTriplesReader.class);
	    
	    //Job
	    job.setMapperClass(FilterMapper.class);
	    job.setMapOutputKeyClass(NullWritable.class);
	    job.setMapOutputValueClass(Text.class);

	    //Output
	    job.setOutputKeyClass(NullWritable.class);
	    job.setOutputValueClass(Text.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	    TextOutputFormat.setCompressOutput(job, true);
	    TextOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
	    TextOutputFormat.setOutputPath(job, new Path(output));

	    
	    long time = System.currentTimeMillis();
	    job.waitForCompletion(true);
	    log.info("Job finished in " + (System.currentTimeMillis() - time));
	}


	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: FilterJob [input dir] [output dir] [patterns (e.g. _:1 rdf:type._:2\n_:1 rdfs:subclassof _:2 to get all type and subclassof triples)] [options]");
			System.exit(0);
		}

		long time = System.currentTimeMillis();
		int res = ToolRunner.run(new Configuration(), new FilterJob(), args);
		log.info("Filter time: " + (System.currentTimeMillis() - time));
		System.exit(res);
	  }	
}