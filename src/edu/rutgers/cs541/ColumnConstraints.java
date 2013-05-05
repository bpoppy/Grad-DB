package edu.rutgers.cs541;

import java.sql.Types;
import java.util.Set;

public class ColumnConstraints {

	String name;
	boolean[] constraints;
	int columnType;
	boolean randomAllowed;
	boolean nullable;
	private int maxSize;
	private InstanceTester tester;
	

	public ColumnConstraints(String columnName, int columnType,
			boolean[] tableConstraints, Boolean randomAllowed, boolean nullable, int maxSize, InstanceTester tester) {
		this.name = columnName;
		this.columnType = columnType;
		this.maxSize = maxSize;
		this.tester = tester;
		
		constraints = new boolean[tableConstraints.length];
		for (int i = 0; i < constraints.length; i++) {
			constraints[i] = EntryPoint.random.nextBoolean() && tableConstraints[i];
		}
		
		//TODO remove this and fix ranges of strings
		if(columnType == Types.VARCHAR){
			for(int i = (constraints.length + 1) / 2; i < constraints.length; i++){
				constraints[i] = false;
			}
		}

		this.randomAllowed = randomAllowed && EntryPoint.random.nextBoolean();

		
		if (InstanceTester.cardinality(constraints) == 0)
			this.randomAllowed = true;

		this.nullable = nullable && EntryPoint.random.nextBoolean();
	}

	public String getValueString() {

		if (nullable && EntryPoint.random.nextInt(10) == 0) {
			return "NULL";
		}

		Set values = tester.getValues(columnType);

		switch (columnType) {
		case Types.DOUBLE: {

			if ((randomAllowed && EntryPoint.random.nextInt(10) == 0)
					|| InstanceTester.cardinality(constraints) == 0) {
				return String.valueOf(EntryPoint.random.nextDouble());
			}

			int count = EntryPoint.random.nextInt(InstanceTester.cardinality(constraints)) - 1;
			int idx;
			for (idx = InstanceTester.nextSetBit(constraints, 0); idx >= 0 && count > 0; idx = InstanceTester.nextSetBit(constraints
					, idx + 1), count--)
				;

			assert count == 0;

			Object[] valuesArray = (Object[]) values.toArray();

			if (idx < valuesArray.length) {
				
				return String.valueOf(valuesArray[idx]);
			}

			idx -= valuesArray.length;
			return String
					.valueOf(((Double)valuesArray[idx] + (Double)valuesArray[idx + 1]) / 2);

		}

		case Types.INTEGER: {
			
			if ((randomAllowed && EntryPoint.random.nextInt(10) == 0)
					|| InstanceTester.cardinality(constraints) == 0) {
				return String.valueOf(EntryPoint.random.nextInt());
			}

			int count = EntryPoint.random.nextInt(InstanceTester.cardinality(constraints)) - 1;
			int idx;
			for (idx = InstanceTester.nextSetBit(constraints, 0); idx >= 0 && count > 0; idx = InstanceTester.nextSetBit(constraints
					, idx + 1), count--)
				;

			assert count == 0;

			Object[] valuesArray = (Object[]) values.toArray();

			if (idx < valuesArray.length) {
				return String.valueOf(valuesArray[idx]);
			}

			
			

			idx -= valuesArray.length;
			return String
					.valueOf(((Integer)valuesArray[idx] + (Integer)valuesArray[idx + 1]) / 2);

		}

		case Types.VARCHAR: {

			if ((randomAllowed && EntryPoint.random.nextInt(4) == 0)
					|| InstanceTester.cardinality(constraints) == 0) {
				return randomString(maxSize);
			}

			int count = EntryPoint.random.nextInt(InstanceTester.cardinality(constraints)) - 1;
			int idx;
			for (idx = InstanceTester.nextSetBit(constraints, 0); idx >= 0 && count > 0; idx = InstanceTester.nextSetBit(constraints
					, idx + 1), count--)
				;

			assert count == 0;

			Object[] valuesArray = (Object[]) values.toArray();

			if (idx < valuesArray.length) {
				String retValue = String.valueOf(valuesArray[idx]).substring(0, Math.min(maxSize, String.valueOf(valuesArray[idx]).length()) - 1) + "\'";
				
				return retValue;
			} else{
				return randomString(maxSize);
			}
		}
		
		default:{
			throw new RuntimeException("Invalid Type");
		}

		}

	}
	
	
	
	public static String randomString(int maxSize) {
		StringBuilder sb = new StringBuilder();
		for (int i = EntryPoint.random.nextInt(maxSize) + 1; i > 0; i--) {
			int type = EntryPoint.random.nextInt(3);
			switch (type) {
			case 0: {
				sb.append((char)(EntryPoint.random.nextInt('z' - 'a') + 'a'));
			}
				break;
			case 1: {
				sb.append((char)(EntryPoint.random.nextInt('Z' - 'A') + 'A'));
			}
				break;
			case 2: {
				sb.append((char)(EntryPoint.random.nextInt('9' - '0') + '0'));
			}
				break;
			}
		}
		return "'" + sb.toString() + "'";
	}
}
