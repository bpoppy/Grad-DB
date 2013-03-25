package edu.rutgers.cs541;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InstanceTester {

	private Connection conn = null;
	private Statement stmt;

	private HashMap<String, TableConstraints> tableConstraints = new HashMap<String, TableConstraints>();

	private void initializeTable(String schemaFile) {
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

		stmt = null;

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
	}

	private void determineConstraints() {
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
				TableConstraints ts = new TableConstraints(tableName);
				System.out.println("adding table: " + tableName);

				// query for the columns of the current table
				ResultSet rsCol = stmt
						.executeQuery("SELECT column_name, data_type, is_nullable "
								+ "FROM information_schema.columns "
								+ "WHERE table_schema = 'PUBLIC' "
								+ "  AND table_name = '"
								+ tableName
								+ "'"
								+ "ORDER BY ordinal_position");
				while (rsCol.next()) {
					String columnName = rsCol.getString(1);
					int dataType = rsCol.getInt(2);
					boolean isNullable = rsCol.getBoolean(3);
					ts.addColumn(columnName, dataType, isNullable);
					System.out.println("added column: " + columnName + " "
							+ dataType + " " + isNullable);
				}
				tableConstraints.put(tableName, ts);

				rsCol.close();
			}

		} catch (SQLException e) {
			System.err.println("Unable to generate instance");
			e.printStackTrace();
		}
	}

	public void testEquality() {
		// now let's check if the queries are identical
		boolean isDiff = false;
		try {

			String groupedQuery1 = getGroupedQuery(stmt, EntryPoint.query1);
			String groupedQuery2 = getGroupedQuery(stmt, EntryPoint.query2);

			// check if there are rows returned in query1 that are
			// not returned in query2
			ResultSet rsChk = stmt.executeQuery("SELECT ("
					+ "SELECT COUNT(*) AS diff12 FROM (" + groupedQuery1
					+ " EXCEPT " + groupedQuery2 + "))" + " + "
					+ "(SELECT COUNT(*) AS diff21 FROM (" + groupedQuery2
					+ " EXCEPT " + groupedQuery1 + "))");
			if (rsChk.next()) {
				int diffRows = rsChk.getInt(1);
				isDiff = diffRows > 0;
			}

		} catch (SQLException e) {
			System.err.println("Unable to perform check for query differences");
			e.printStackTrace();
		}

		// if the queries are different, save the instance to the out folder
		if (isDiff) {
			EntryPoint.writeInstance(stmt);
		}
	}

	public InstanceTester(String schemaFile) {
		initializeTable(schemaFile);
		determineConstraints();
		populateTables();
		testEquality();

	}

	private void populateTables() {
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

				System.out.println("inserting into " + tableName);
				for (int numInserts = EntryPoint.random.nextInt(100); numInserts > 0; numInserts--) {
					// for each table in the schema,
					// we will generate an INSERT statement to create a tuple
					StringBuilder insertSb = new StringBuilder();
					insertSb.append("INSERT INTO ");
					insertSb.append(tableName);
					insertSb.append(" VALUES (");

					// query for the columns of the current table
					ResultSet rsCol = stmt
							.executeQuery("SELECT column_name, data_type, is_nullable "
									+ "FROM information_schema.columns "
									+ "WHERE table_schema = 'PUBLIC' "
									+ "  AND table_name = '"
									+ tableName
									+ "'"
									+ "ORDER BY ordinal_position");
					int colNum = 1;
					while (rsCol.next()) {
						String columnName = rsCol.getString(1);
						System.out.println(columnName + "  type:" + tableConstraints.get(tableName).getColumnConstraints(columnName).columnType);

						if (colNum++ != 1) {
							insertSb.append(", ");
						}

						// generate a value appropriate for the column's type
						insertSb.append(tableConstraints.get(tableName)
								.getColumnConstraints(columnName)
								.getValueString());
					}
					insertSb.append(")");
					rsCol.close();

					// execute the INSERT statement to add a tuple to the table
					stmt.executeUpdate(insertSb.toString());
				}
			}

		} catch (SQLException e) {
			System.err.println("Unable to generate instance");
			e.printStackTrace();
		}
	}

	/**
	 * Augment the query by grouping all columns in the Result Set and adding a
	 * COUNT(*) field
	 * 
	 * @param stmt
	 *            - an active Statement to use for executing queries
	 * @param query
	 *            - the SQL query to be augmented
	 */
	private static String getGroupedQuery(Statement stmt, String query) {
		String rv = null;
		try {
			// execute the query and get the ResultSet's meta-data
			ResultSet rsQ = stmt.executeQuery(query);
			ResultSetMetaData rsMetaData = rsQ.getMetaData();

			// get a list of column labels so that we can group on all columns
			// backticks are used in case of labels that might be illegal when
			// reused in a query (e.g. COUNT(*))
			int columnCount = rsMetaData.getColumnCount();
			StringBuilder columnSeq = new StringBuilder();
			columnSeq.append('`').append(rsMetaData.getColumnLabel(1))
					.append('`');
			for (int i = 2; i <= columnCount; i++) {
				columnSeq.append(',');
				columnSeq.append('`').append(rsMetaData.getColumnLabel(i))
						.append('`');
			}
			rsQ.close();

			rv = "SELECT " + columnSeq + ", COUNT(*) " + "FROM (" + query
					+ ") " + "GROUP BY " + columnSeq;
		} catch (SQLException e) {
			System.err.println("Unable to perform check for query differences");
			e.printStackTrace();
		}
		return rv;
	}

}
