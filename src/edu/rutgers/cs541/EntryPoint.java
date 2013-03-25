package edu.rutgers.cs541;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 * This is a sample class intended to demonstrate how to parse command-line
 * options and how to start an H2 In-Memory Database
 * 
 * You are free to change the contents of this file completely (and add any
 * additional classes), but your main() method must be in
 * edu.rutgers.cs541.EntryPoint
 * 
 * @author yaros
 */
public class EntryPoint {
 
	
	public static Random random = new Random();

	public static String query1, query2;

	private static int solutionsFound = 0;
	private static String outputDirectory;

	private static String schemaFile;

	/**
	 * This is the main method, where execution begins
	 * 
	 * @param args
	 *            - command line arguments
	 */
	public static void main(String[] args) {

		if (args.length != 4) {
			String usage = "required arguments are <schema_file> "
					+ "<query1> <query2> <instance_output_directory>";
			System.err.println(usage);
			// croak
			System.exit(1);
		}

		schemaFile = args[0];

		// read the queries from file
		query1 = readFileOrDie(args[1]);
		query2 = readFileOrDie(args[2]);

		outputDirectory = args[3];

		QueryProcessor.processQuery(query1);
		QueryProcessor.processQuery(query2);
		
		
		int count = 0;
		
		while(true){
			System.out.println("run " + count++);
			new InstanceTester(schemaFile);
		}
	}

	/**
	 * Read the contents of a file into a string Terminate program if unable to
	 * do so
	 * 
	 * @param fileName
	 *            - name of file to be read
	 */
	private static String readFileOrDie(String fileName) {
		// using fast way to read a file into a string:
		// http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file/326440#326440

		String rv = null;

		FileInputStream stream = null;
		try {
			stream = new FileInputStream(new File(fileName));
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			rv = Charset.defaultCharset().decode(bb).toString();
		} catch (IOException e) {
			System.err.println("Unable to open file \"" + fileName + "\"");
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}

		if (rv == null) {
			// must not have been able to read file, so croak
			System.exit(1);
		}

		return rv;
	}

	public static void writeInstance(Statement stmt) {

		// use the user-supplied directory (last command line argument)
		String outPath = new File(outputDirectory, (++solutionsFound) + ".sql")
				.getPath();
		try {
			// Use another handy H2 command to save the instance
			stmt.execute("SCRIPT NOSETTINGS TO '" + outPath + "'");
		} catch (SQLException e) {
			System.err.println("Unable save to load the schema script \""
					+ schemaFile + "\"");
			e.printStackTrace();
		}
	}
}
