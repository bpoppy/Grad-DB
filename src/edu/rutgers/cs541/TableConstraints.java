package edu.rutgers.cs541;

import java.sql.Types;
import java.util.HashMap;

/**
 * Constraints placed on a given table
 * 
 * @author Brian
 * 
 */
public class TableConstraints {
	private String name;

	private HashMap<Integer, boolean[]> valuesInTable = new HashMap<Integer, boolean[]>(); // Which ranges of values
													// are
	// allowed in the table
	private HashMap<Integer, Boolean> randomValuesInTable = new HashMap<Integer, Boolean>(); // Whether random
															// values are
	// allowed for given type

	private HashMap<String, ColumnConstraints> columnConstraints = new HashMap<String, ColumnConstraints>();

	private InstanceTester tester;
	
	public TableConstraints(String name, InstanceTester tester) {
		this.name = name;
		this.tester = tester;
		determineValuesAllowed(Types.DOUBLE);
		determineValuesAllowed(Types.INTEGER);
		determineValuesAllowed(Types.VARCHAR);
	}


	public ColumnConstraints getColumnConstraints(String name) {
		return columnConstraints.get(name);
	}

	private void determineValuesAllowed(int dataType) {
		boolean randomAllowed = EntryPoint.random.nextBoolean();
		int valuesSize = tester.getValues(dataType).size();
		boolean[] bs = new boolean[0];

		if (valuesSize > 0) {
			bs = new boolean[2 * valuesSize - 1]; // Have a boolean value for
													// each value and each
													// exclusive range between
													// them
			

			for (int i = 0; i < bs.length; i++) {
				bs[i] = EntryPoint.random.nextBoolean();
			}

		}
		if (InstanceTester.cardinality(bs) == 0)
			randomAllowed = true;
		valuesInTable.put(dataType, bs);
		randomValuesInTable.put(dataType, randomAllowed);
	}

	public void addColumn(String columnName, int columnType, boolean nullable, int maxSize) {
		columnConstraints.put(
				columnName,
				new ColumnConstraints(columnName, columnType, valuesInTable
						.get(columnType), randomValuesInTable.get(columnType),
						nullable, maxSize, tester));
	}

}
