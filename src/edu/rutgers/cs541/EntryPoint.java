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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a sample class intended to demonstrate how to 
 * parse command-line options and how to start an H2 In-Memory Database 
 * 
 * You are free to change the contents of this file completely
 * (and add any additional classes), but your main() method must be in
 * edu.rutgers.cs541.EntryPoint
 * 
 * @author yaros
 */
public class EntryPoint {

	/**
	 * This is the main method, where execution begins
	 * 
	 * @param args - command line arguments
	 */
	public static void main(String[] args) {
		
		if(args.length != 4) {
			String usage = "required arguments are <schema_file> "+
					"<query1> <query2> <instance_output_directory>";
			System.err.println(usage);
			//croak
			System.exit(1); 
		}

		//read the queries from file
		String query1 = readFileOrDie(args[1]);
		String query2 = readFileOrDie(args[2]);
		
		// load the H2 Driver
		try {
	        Class.forName("org.h2.Driver");
		} catch(ClassNotFoundException e) {
			System.err.println("Unable to load H2 driver");
			e.printStackTrace();
			//croak
			System.exit(1);
		}
        
    // This is the URL to create an H2 private In-Memory DB
    String dbUrl = "jdbc:h2:mem:";
    
    // credentials do not really matter 
    // since the database will be private
    String dbUser = "dummy";
    String dbPassword = "password";

    Connection conn=null;
    Statement stmt=null;
    
    try {
      //create a connection to the H2 database
      //since the DB does not already exist, it will be created automatically
      //http://www.h2database.com/html/features.html#in_memory_databases
    	conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			
			//create a statement to execute queries
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
			stmt.execute("RUNSCRIPT FROM '"+args[0]+"'");
		} catch (SQLException e) {
			System.err.println("Unable to load the schema script \""+args[0]+"\"");
			e.printStackTrace();
		}
			
		// We will now query the system views (i.e. the information_schema) 
		// to see what is in the user provided schema.
		try {
			// see what tables are in the schema 
			//  (note that the user schema is called PUBLIC by default) 
			ResultSet rsTab = stmt.executeQuery(
					"SELECT table_name "+
					"FROM information_schema.tables "+
					"WHERE table_schema = 'PUBLIC'");
			
			List<String> tableNames = new ArrayList<String>();
			while(rsTab.next()) {
				//note that column indexing starts from 1
				tableNames.add(rsTab.getString(1));
			}
			rsTab.close();
			
			for(String tableName : tableNames) {
				//for each table in the schema, 
				//  we will generate an INSERT statement to create a tuple
				StringBuilder insertSb = new StringBuilder();
				insertSb.append("INSERT INTO ");
				insertSb.append(tableName);
				insertSb.append(" VALUES (");
				
				// query for the columns of the current table
				ResultSet rsCol = stmt.executeQuery(
						"SELECT column_name, data_type, is_nullable "+
						"FROM information_schema.columns "+
						"WHERE table_schema = 'PUBLIC' "+
						"  AND table_name = '"+tableName+"'" +
						"ORDER BY ordinal_position");
				int colNum = 1;
				while(rsCol.next()) {
					String columnName = rsCol.getString(1);
					int dataType = rsCol.getInt(2);
					boolean isNullable = rsCol.getBoolean(3);
					
					if(colNum++ != 1) {
						insertSb.append(", ");
					}
					
					// generate a value appropriate for the column's type
					switch(dataType) {
					case Types.INTEGER:
						insertSb.append("0"); 
						break;
					case Types.DOUBLE:
						//mmm, pi
						insertSb.append("3.14159"); 
						break;
					case Types.VARCHAR:
						if(isNullable) {
							//in your code, you will want to put NULLs
							//in all NULLABLE columns, not just VARCHARs
							insertSb.append("NULL");
						} else {
							insertSb.append("'abc'");
						}
						break;
					default:
						System.err.println("Column \""+columnName
								+"\" of Table "+tableName
								+" has unsupported data type "+dataType);
						//croak;
						System.exit(1);
					}
				}
				insertSb.append(")");
				rsCol.close();
				
				//execute the INSERT statement to add a tuple to the table
				stmt.executeUpdate(insertSb.toString());
			}
			
		} catch (SQLException e) {
			System.err.println("Unable to generate instance");
			e.printStackTrace();
		}

		//now let's check if the queries are identical
		boolean isDiff = false;
		try {
	     // check if there are rows returned in query1 that are 
       //   not returned in query2 
      ResultSet rsChk = stmt.executeQuery(
          "SELECT ("+
              "SELECT COUNT(*) AS diff12 FROM ("+
              query1+" EXCEPT "+query2+"))" +
                  		" + "+
              "(SELECT COUNT(*) AS diff21 FROM ("+
              query2+" EXCEPT "+query1+"))");
      if(rsChk.next()) {
        int diffRows = rsChk.getInt(1);
        isDiff = diffRows > 0;
      }
			
		} catch (SQLException e) {
			System.err.println("Unable to perform check for query differences");
			e.printStackTrace();
		}

		//if the queries are different, save the instance to the out folder
		if(isDiff) {
			// use the user-supplied directory (last command line argument)
			String outPath = new File(args[3], "1.sql").getPath();
			try {
				//Use another handy H2 command to save the instance 
				stmt.execute("SCRIPT NOSETTINGS TO '"+outPath+"'");
			} catch (SQLException e) {
				System.err.println("Unable save to load the schema script \""+args[0]+"\"");
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * Read the contents of a file into a string
	 * Terminate program if unable to do so
	 * @param fileName - name of file to be read
	 */
	private static String readFileOrDie(String fileName) {
		//using fast way to read a file into a string: 
		//  http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file/326440#326440

		String rv = null;
		
		FileInputStream stream = null;
		try { 
			stream = new FileInputStream(new File(fileName));
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = 
					fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			rv =  Charset.defaultCharset().decode(bb).toString();
		} catch(IOException e) {
			System.err.println("Unable to open file \""+fileName+"\"");
			e.printStackTrace();
		} finally {
			if(stream != null) {
				try {
					stream.close();
					} catch (IOException e) {
					//do nothing
				}
			}
		}
		
		if(rv == null) {
			//must not have been able to read file, so croak
			System.exit(1);
		}
		
		return rv;
	}
}
