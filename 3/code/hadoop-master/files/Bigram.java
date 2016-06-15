import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.google.common.collect.Iterators;

import java.io.*;
import java.util.*;

public class Bigram 
{
  public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>
  {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException 
	{
      StringTokenizer itr = new StringTokenizer(value.toString());
	  String prev = null;

      while (itr.hasMoreTokens()) 
	  {
		String curr = itr.nextToken();
		if (prev != null) 
		{
			word.set(prev + " " + curr);
			context.write(word, one);
	  	}
	  	prev = curr;
	  }
    }
  }

  public static class IntSumReducer extends Reducer<Text,IntWritable,Text,IntWritable> 
  {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException 
	{
      int sum = 0;
      for (IntWritable val : values) 
	  {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static class Tuple<str extends Writable, count extends Writable> implements Writable
  {
	  public str t;
	  public count i;

	  public Tuple()
	  {
      }

	  public void add(str tt, count ii)
	  {
	     t = tt;
		 i = ii;
	  }

	  public void readFields(DataInput in) throws IOException 
	  {
		String keyClassName = in.readUTF();
		String valueClassName = in.readUTF();

		try 
		{
		  Class<str> keyClass = (Class<str>) Class.forName(keyClassName);
		  t = (str) keyClass.newInstance();
		  Class<count> valueClass = (Class<count>) Class.forName(valueClassName);
		  i = (count) valueClass.newInstance();

		  t.readFields(in);
		  i.readFields(in);
		} 
		catch (Exception e) 
		{
		  throw new RuntimeException("Unable to create Tuples");
		}
	  }

	  public void write(DataOutput out) throws IOException 
	  {
		out.writeUTF(t.getClass().getCanonicalName());
		out.writeUTF(i.getClass().getCanonicalName());

		t.write(out);
		i.write(out);
	  }
  }

  public static void main(String[] args) throws Exception 
  {
    Configuration conf = new Configuration();

    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(Bigram.class);
    job.setMapperClass(TokenizerMapper.class);
	job.setNumReduceTasks(1);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
	Path outputpath = new Path(args[1]);
	FileOutputFormat.setOutputPath(job, outputpath);
	job.waitForCompletion(true);

	BufferedReader br = null;

	try 
    {
		String sCurrentLine;
		FileSystem fileSystem = outputpath.getFileSystem(conf);

		FileStatus[] listStatus = fileSystem.listStatus(outputpath, new PathFilter() 
        {
		  public boolean accept(Path path) 
		  {
			return !path.getName().startsWith("_");
		  }
		});

		br = new BufferedReader(new InputStreamReader(fileSystem.open(listStatus[0].getPath())));

		List<Tuple<Text, IntWritable>> bigrams = new ArrayList<Tuple<Text, IntWritable>>();

		while ((sCurrentLine = br.readLine()) != null) 
		{
			String[] tokens = sCurrentLine.split("\\s+");

			Text bi = new Text();
			IntWritable coun = new IntWritable();
			Tuple t = new Tuple();

			bi.set(tokens[0] + " " + tokens[1]);
			coun.set(Integer.parseInt(tokens[2]));

			t.add(bi,coun);
			bigrams.add(t);
		}

		Collections.sort(bigrams, new Comparator<Tuple<Text, IntWritable>>()
		{
		  	public int compare(Tuple<Text, IntWritable> e1, Tuple<Text, IntWritable> e2) 
		  	{
				int res = e2.i.compareTo(e1.i);

		    	if (res == 0) 
				{
		     		return e1.t.compareTo(e2.t);
		    	}

		    	return res;
		  	}
		});
	
		//Calculate the number of bigrams
		int sum = 0;

		for(Tuple<Text, IntWritable> j : bigrams) 
		{
		  	sum += j.i.get();
		}
		System.out.println("###########################");
		System.out.println("Total Number of Bigrams = " + bigrams.size());
		System.out.println("Most Common Bigram = " + bigrams.get(0).t + " with count = " + bigrams.get(0).i);

		//Get the bigrams that make up the top 10%
		int sum10 = sum/10;
		System.out.println("\nPrinting bigrams that make up 10 percent of the count of all the bigrams (i.e. 10 percent of " + sum + " = " + sum/10 + ")");
		System.out.println("These are the bigrams needed: ");
		sum = 0;
		for(Tuple<Text, IntWritable> j : bigrams) 
		{
		  sum += j.i.get();

		  if(sum > sum10)
		    break;

		  System.out.println(j.t + "\t" + j.i);		
		}
		System.out.println("###########################");
	} 
	catch (IOException e) 
	{
		e.printStackTrace();
	} 
	finally 
	{
		try 
		{
			if (br != null)
				br.close();
		} 
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
	}

  }
}
