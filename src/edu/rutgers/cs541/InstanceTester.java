package edu.rutgers.cs541;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class InstanceTester {

	private Connection conn = null;
	private Statement stmt;

	public static AtomicInteger minRows = new AtomicInteger(Integer.MAX_VALUE);


	private int numRows = 0;

	private final HashMap<String, TableConstraints> tableConstraints = new HashMap<String, TableConstraints>();
	private final HashMap<String, Integer> tableSizes = new HashMap<String, Integer>();

	private TreeSet<Double> doubleSet = new TreeSet<Double>();
	private TreeSet<Integer> intSet = new TreeSet<Integer>();
	private TreeSet<String> stringSet = new TreeSet<String>();

	public InstanceTester(String schemaFile, int minimumDataPoints, int maxTuples) {
		this.getQueryProcessorValues();
		this.padConstraintValues(minimumDataPoints);
		this.initializeTable(schemaFile);
		this.determineConstraints();
		for(String tableName : this.tableConstraints.keySet()){
			int tableSize = EntryPoint.random.nextInt(maxTuples);
			maxTuples -= tableSize;
			this.tableSizes.put(tableName,  tableSize);
		}
		this.populateTables();
		this.testEquality();
	}

	public static int cardinality(boolean[] array) {
		int count = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i]) {
				count++;
			}
		}
		return count;
	}

	public static int nextSetBit(boolean[] array, int start) {
		for (int i = start; i < array.length; i++) {
			if (array[i] == true) {
				return i;
			}
		}
		return -1;
	}

	/**
	 *
	 * @param type
	 *            SQL value type
	 * @return The set of values that are stored of the specified type
	 */
	public Set getValues(int type) {
		switch (type) {
		case Types.DOUBLE: {
			return this.doubleSet;
		}

		case Types.VARCHAR: {
			return this.stringSet;
		}

		case Types.INTEGER: {
			return this.intSet;
		}

		default: {

			throw new RuntimeException("Invalid Type" + type);
		}
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized void getQueryProcessorValues() {
		this.doubleSet = (TreeSet<Double>) ((TreeSet<Double>) QueryProcessor
				.getValues(Types.DOUBLE)).clone();
		this.intSet = (TreeSet<Integer>) ((TreeSet<Integer>) QueryProcessor
				.getValues(Types.INTEGER)).clone();
		this.stringSet = (TreeSet<String>) ((TreeSet<String>) QueryProcessor
				.getValues(Types.VARCHAR)).clone();
	}

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

		this.stmt = null;

		// This is the URL to create an H2 private In-Memory DB
		String dbUrl = "jdbc:h2:mem:";

		try {
			// create a connection to the H2 database
			// since the DB does not already exist, it will be created
			// automatically
			// http://www.h2database.com/html/features.html#in_memory_databases
			this.conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

			// create a statement to execute queries
			this.stmt = this.conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Unable to initialize H2 database");
			e.printStackTrace();
		}

		// H2 has a nice command allowing us to run a series of
		// queries from file.
		// http://www.h2database.com/html/grammar.html#runscript
		// We will use it here to run the user-supplied schema script.
		try {
			this.stmt.execute("RUNSCRIPT FROM '" + schemaFile + "'");
		} catch (SQLException e) {
			System.err.println("Unable to load the schema script \""
					+ schemaFile + "\"");
			e.printStackTrace();
		}
	}

	private void padConstraintValues(int minConstraints) {
		while (this.doubleSet.size() < minConstraints) {
			this.doubleSet.add(EntryPoint.random.nextDouble());
		}

		while (this.intSet.size() < minConstraints) {
			this.intSet.add(EntryPoint.random.nextInt());
		}

		while (this.stringSet.size() < minConstraints) {
			this.stringSet.add(ColumnConstraints.randomString(100));
		}
	}

	private void determineConstraints() {

		// We will now query the system views (i.e. the information_schema)
		// to see what is in the user provided schema.
		try {
			// see what tables are in the schema
			// (note that the user schema is called PUBLIC by default)
			ResultSet rsTab = this.stmt.executeQuery("SELECT table_name "
					+ "FROM information_schema.tables "
					+ "WHERE table_schema = 'PUBLIC'");

			List<String> tableNames = new ArrayList<String>();
			while (rsTab.next()) {
				// note that column indexing starts from 1
				tableNames.add(rsTab.getString(1));
			}
			rsTab.close();

			for (String tableName : tableNames) {
				TableConstraints ts = new TableConstraints(tableName, this);

				// query for the columns of the current table
				ResultSet rsCol = this.stmt
						.executeQuery("SELECT column_name, data_type, is_nullable, character_maximum_length "
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
					int maxSize = rsCol.getInt(4);
					if (QueryProcessor.usefulColumns.contains(columnName.toLowerCase())) {
						ts.addColumn(columnName, dataType, isNullable, maxSize);
					}
				}
				this.tableConstraints.put(tableName, ts);

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

			String groupedQuery1 = getGroupedQuery(this.stmt, EntryPoint.query1);
			String groupedQuery2 = getGroupedQuery(this.stmt, EntryPoint.query2);

			// check if there are rows returned in query1 that are
			// not returned in query2
			ResultSet rsChk = this.stmt.executeQuery("SELECT ("
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
			System.exit(0);
		}

		EntryPoint.examplesTested.incrementAndGet();

		// if the queries are different, save the instance to the out folder
		if (isDiff && this.numRows < minRows.get()) {
			String[] result = new String[] {
				"" + (EntryPoint.solutionsFound.get() + 1),
				new Date().getTime() - EntryPoint.startTime + "",
				"" + EntryPoint.examplesTested.get(),
				"" + this.numRows,
			};
			EntryPoint.window.publishResult(result);
			EntryPoint.writeInstance(this.stmt);
			minRows.set(this.numRows);
		}
	}

	private void populateTables() {
		// We will now query the system views (i.e. the information_schema)
		// to see what is in the user provided schema.
		try {
			// see what tables are in the schema
			// (note that the user schema is called PUBLIC by default)
			ResultSet rsTab = this.stmt.executeQuery("SELECT table_name "
					+ "FROM information_schema.tables "
					+ "WHERE table_schema = 'PUBLIC'");

			List<String> tableNames = new ArrayList<String>();
			while (rsTab.next()) {
				// note that column indexing starts from 1
				tableNames.add(rsTab.getString(1));
			}
			rsTab.close();

			for (String tableName : tableNames) {

				int numInserts = this.tableSizes.get(tableName);
				this.numRows += numInserts;
				for (; numInserts > 0; numInserts--) {
					// for each table in the schema,
					// we will generate an INSERT statement to create a tuple
					StringBuilder insertSb = new StringBuilder();
					insertSb.append("INSERT INTO ");
					insertSb.append(tableName);
					insertSb.append(" VALUES (");

					// query for the columns of the current table
					ResultSet rsCol = this.stmt.executeQuery("SELECT column_name, data_type "
							+ "FROM information_schema.columns "
							+ "WHERE table_schema = 'PUBLIC' "
							+ "  AND table_name = '" + tableName + "'"
							+ "ORDER BY ordinal_position");
					int colNum = 1;
					while (rsCol.next()) {
						String columnName = rsCol.getString(1);
						int dataType = rsCol.getInt(2);


						if (colNum++ != 1) {
							insertSb.append(", ");
						}

						if (QueryProcessor.usefulColumns.contains(columnName.toLowerCase())) {

							// generate a value appropriate for the column's
							// type
							insertSb.append(this.tableConstraints.get(tableName)
									.getColumnConstraints(columnName)
									.getValueString());
						} else{
							insertSb.append(this.defaultValue(dataType));
						}
					}
					insertSb.append(")");
					rsCol.close();

					try {
						// execute the INSERT statement to add a tuple to the
						// table
						this.stmt.executeUpdate(insertSb.toString());
					} catch (Exception ex) {
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("Unable to generate instance");
			e.printStackTrace();
		}
	}

	public Object defaultValue(int dataType){
		switch(dataType){
		case Types.VARCHAR:{
			return "\"A\"";
		}
		case Types.INTEGER:{
			return "0";
		}
		case Types.DOUBLE:{
			return "0";
		}
		default:
			return "0";
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
