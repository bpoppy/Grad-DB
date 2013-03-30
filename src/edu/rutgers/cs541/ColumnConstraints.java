package edu.rutgers.cs541;

import java.sql.Types;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;
import java.util.TreeSet;

public class ColumnConstraints {

	String name;
	BitSet constraints;
	int columnType;
	boolean randomAllowed;
	boolean nullable;
	private int maxSize;
	private InstanceTester tester;
	

	public ColumnConstraints(String columnName, int columnType,
			BitSet tableConstraints, Boolean randomAllowed, boolean nullable, int maxSize, InstanceTester tester) {
		this.name = columnName;
		this.columnType = columnType;
		this.maxSize = maxSize;
		this.tester = tester;
		
		constraints = new BitSet(tableConstraints.length());
		for (int i = 0; i < constraints.size(); i++) {
			constraints.set(i, EntryPoint.random.nextBoolean());
		}
		constraints.and(tableConstraints);
		
		//TODO remove this and fix ranges of strings
		if(columnType == Types.VARCHAR){
			BitSet temp = new BitSet(constraints.length());
			temp.set(0, (constraints.length() + 1) / 2);
			constraints.and(temp);
		}

		this.randomAllowed = randomAllowed && EntryPoint.random.nextBoolean();

		if (constraints.cardinality() == 0)
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
					|| constraints.cardinality() == 0) {
				return String.valueOf(EntryPoint.random.nextDouble());
			}

			int count = EntryPoint.random.nextInt(constraints.cardinality()) - 1;
			int idx;
			for (idx = constraints.nextSetBit(0); idx >= 0 && count > 0; idx = constraints
					.nextSetBit(idx + 1), count--)
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
					|| constraints.cardinality() == 0) {
				System.out.println("random value selected");
				return String.valueOf(EntryPoint.random.nextInt());
			}

			int count = EntryPoint.random.nextInt(constraints.cardinality()) - 1;
			int idx;
			for (idx = constraints.nextSetBit(0); idx >= 0 && count > 0; idx = constraints
					.nextSetBit(idx + 1), count--)
				;

			assert count == 0;

			Object[] valuesArray = (Object[]) values.toArray();

			if (idx < valuesArray.length) {
				return String.valueOf(valuesArray[idx]);
			}

			System.out.println(constraints.toString());
			System.out.println("size "+ constraints + " " + constraints.size() + "  " + constraints.length());
			
			
			System.out.println("cardinality " + constraints.cardinality());
			System.out.println(Arrays.toString(valuesArray));
			System.out.println(valuesArray[idx]);

			idx -= valuesArray.length;
			return String
					.valueOf(((Integer)valuesArray[idx] + (Integer)valuesArray[idx + 1]) / 2);

		}

		case Types.VARCHAR: {

			if ((randomAllowed && EntryPoint.random.nextInt(10) == 0)
					|| constraints.cardinality() == 0) {
				return randomString(maxSize);
			}

			int count = EntryPoint.random.nextInt(constraints.cardinality()) - 1;
			int idx;
			System.out.println(constraints.cardinality());
			for (idx = constraints.nextSetBit(0); idx >= 0 && count > 0; idx = constraints
					.nextSetBit(idx + 1), count--)
				;

			assert count == 0;

			System.out.println(idx + " " + values.getClass() + "  " + values.toString() );
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
