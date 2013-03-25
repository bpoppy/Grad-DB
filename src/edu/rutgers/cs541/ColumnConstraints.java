package edu.rutgers.cs541;

import java.sql.Types;
import java.util.BitSet;
import java.util.Set;

public class ColumnConstraints {

	String name;
	BitSet constraints;
	int columnType;
	boolean randomAllowed;
	boolean nullable;

	public ColumnConstraints(String columnName, int columnType,
			BitSet tableConstraints, Boolean randomAllowed, boolean nullable) {
		this.name = columnName;
		this.columnType = columnType;
		
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

		Set values = QueryProcessor.getValues(columnType);

		switch (columnType) {
		case Types.DOUBLE: {

			if ((randomAllowed && EntryPoint.random.nextInt(10) == 0)
					|| constraints.cardinality() == 0) {
				return String.valueOf(EntryPoint.random.nextDouble());
			}

			int count = EntryPoint.random.nextInt(constraints.cardinality());
			int idx;
			for (idx = constraints.nextSetBit(0); idx >= 0 && count > 0; idx = constraints
					.nextSetBit(idx + 1), count--)
				;

			assert count == 0;

			Double[] valuesArray = (Double[]) values.toArray();

			if (idx < valuesArray.length) {
				return String.valueOf(valuesArray[idx]);
			}

			idx -= valuesArray.length;
			return String
					.valueOf((valuesArray[idx] + valuesArray[idx + 1]) / 2);

		}

		case Types.INTEGER: {

			if ((randomAllowed && EntryPoint.random.nextInt(10) == 0)
					|| constraints.cardinality() == 0) {
				return String.valueOf(EntryPoint.random.nextInt());
			}

			int count = EntryPoint.random.nextInt(constraints.cardinality());
			int idx;
			for (idx = constraints.nextSetBit(0); idx >= 0 && count > 0; idx = constraints
					.nextSetBit(idx + 1), count--)
				;

			assert count == 0;

			Integer[] valuesArray = (Integer[]) values.toArray();

			if (idx < valuesArray.length) {
				return String.valueOf(valuesArray[idx]);
			}

			idx -= valuesArray.length;
			return String
					.valueOf((valuesArray[idx] + valuesArray[idx + 1]) / 2);

		}

		case Types.VARCHAR: {

			if ((randomAllowed && EntryPoint.random.nextInt(10) == 0)
					|| constraints.cardinality() == 0) {
				return randomString();
			}

			int count = EntryPoint.random.nextInt(constraints.cardinality());
			int idx;
			for (idx = constraints.nextSetBit(0); idx >= 0 && count > 0; idx = constraints
					.nextSetBit(idx + 1), count--)
				;

			assert count == 0;

			Integer[] valuesArray = (Integer[]) values.toArray();

			if (idx < valuesArray.length) {
				return String.valueOf(valuesArray[idx]);
			}
		}
		
		default:{
			throw new RuntimeException("Invalid Type");
		}

		}

	}
	
	
	private static String randomString() {
		StringBuilder sb = new StringBuilder();
		for (int i = EntryPoint.random.nextInt(10); i > 0; i--) {
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
