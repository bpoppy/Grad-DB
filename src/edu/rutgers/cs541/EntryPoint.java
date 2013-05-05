// TODO BEFORE COKE
package edu.rutgers.cs541;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


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
	public static  AtomicInteger examplesTested = new AtomicInteger();
	private static String outputDirectory;
	
	private static String schemaFile;
	
	private static Runnable[] runners = new Runnable[4];
	
	public static int numColumns = 0;
	
	

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
		
		initializeTable(schemaFile);
		
		
		for(int i = 0; i < EntryPoint.runners.length; i++) {
			EntryPoint.runners[i] = new TestThread(schemaFile);
		}
		for(int i = 0; i < EntryPoint.runners.length; i++) {
			EntryPoint.runners[i].run();
		}
		
	}
	
	private static void initializeTable(String schemaFile) {
		Connection conn;
		// load the H2 Driver
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load H2 driver");
			e.printStackTrace();
			// croak
			System.exit(1);
		}

		// credentials do not really matter
		// since the database will be private
		String dbUser = "dummy";
		String dbPassword = "password";

		Statement stmt = null;

		// This is the URL to create an H2 private In-Memory DB
		String dbUrl = "jdbc:h2:mem:";

		try {
			// create a connection to the H2 database
			// since the DB does not already exist, it will be created
			// automatically
			// http://www.h2database.com/html/features.html#in_memory_databases
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

			// create a statement to execute queries
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Unable to initialize H2 database");
			e.printStackTrace();
		}

		// H2 has a nice command allowing us to run a series of
		// queries from file.
		// http://www.h2database.com/html/grammar.html#runscript
		// We will use it here to run the user-supplied schema script.
		try {
			stmt.execute("RUNSCRIPT FROM '" + schemaFile + "'");
		} catch (SQLException e) {
			System.err.println("Unable to load the schema script \""
					+ schemaFile + "\"");
			e.printStackTrace();
		}


		// We will now query the system views (i.e. the information_schema)
		// to see what is in the user provided schema.
		try {
			// see what tables are in the schema
			// (note that the user schema is called PUBLIC by default)
			ResultSet rsTab = stmt.executeQuery("SELECT table_name "
					+ "FROM information_schema.tables "
					+ "WHERE table_schema = 'PUBLIC'");

			List<String> tableNames = new ArrayList<String>();
			while (rsTab.next()) {
				// note that column indexing starts from 1
				tableNames.add(rsTab.getString(1));
			}
			rsTab.close();

			for (String tableName : tableNames) {

				// query for the columns of the current table
				ResultSet rsCol = stmt
						.executeQuery("SELECT column_name, data_type, is_nullable, character_maximum_length "
								+ "FROM information_schema.columns "
								+ "WHERE table_schema = 'PUBLIC' "
								+ "  AND table_name = '"
								+ tableName
								+ "'"
								+ "ORDER BY ordinal_position");
				while (rsCol.next()) {
					numColumns ++;
				}

				rsCol.close();
			}

		} catch (SQLException e) {
			System.err.println("Unable to generate instance");
			e.printStackTrace();
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

	public static synchronized void writeInstance(Statement stmt) {

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
