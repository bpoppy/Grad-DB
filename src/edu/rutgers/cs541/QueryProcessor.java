package edu.rutgers.cs541;

import java.sql.Types;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * The Query Processor class which is meant to go through the queries and
 * extract all the values which could be important.
 * 
 * @author Brian
 * 
 */



public class QueryProcessor {
	
	private static TreeSet<Double> doubleSet = new TreeSet<Double>();
	private static TreeSet<Integer> intSet = new TreeSet<Integer>();
	private static TreeSet<String> stringSet = new TreeSet<String>();
	
	public static Object constraintLock = new Object();

	
	/**
	 * Process the query and extract all values we care about.
	 * 
	 * @param query
	 *            the query
	 */
	public static void processQuery(String query) {
		query = extractStrings(query);
		query = replaceOperators(query);
		extractNumberValues(query);
	}
	
	
	/**
	 * 
	 * @param type SQL value type
	 * @return The set of values that are stored of the specified type
	 */
	public static Set getValues(int type){
		switch (type){
		case Types.DOUBLE:{
			return doubleSet;
		}

		case Types.VARCHAR:{
			return stringSet;
		}

		case Types.INTEGER:{
			return intSet;
		}
		
		default:{
			
			throw new RuntimeException("Invalid Type" + type);
		}
		}
	}

	/**
	 * Find all the int and double values and put them in the sets.
	 * 
	 * @param query
	 */
	private static void extractNumberValues(String query) {
		String[] tokens = query.split("\\s+");
		for (String tok : tokens) {
			try {
				double parsedValue;
				parsedValue = Double.parseDouble(tok);
				doubleSet.add(parsedValue);
				if (parsedValue == (int) parsedValue) {
					intSet.add((int) parsedValue);
				}
			} catch (Exception ex) {
			}

			try {
				intSet.add(Integer.parseInt(tok));
			} catch (Exception ex) {
			}

			if (tok.indexOf("0x") == 0)
				try {
					intSet.add(Integer.parseInt(tok.substring(2), 16));
					doubleSet.add(Double.valueOf(Integer.parseInt(
							tok.substring(2), 16)));
				} catch (Exception ex) {
				}
		}
	}

	/**
	 * 
	 * @param query
	 * @return modified query with all the characters in the array replaced with
	 *         spaces. Helps with pulling out key words but we might not want to
	 *         remove parens and the like if we want to evaluate expressions.
	 */
	private static String replaceOperators(String query) {
		Character[] charSet = { '(', ')', '+', '/', '=', '[', ']', '*', '-' };
		for (Character ch : charSet) {
			while (query.indexOf(ch) >= 0) {
				query = query.substring(0, query.indexOf(ch)) + ' '
						+ query.substring(query.indexOf(ch) + 1);
			}
		}
		return query;
	}

	/**
	 * 
	 * @param query
	 *            the query
	 * @return modified query qith parens replaced with spaces
	 */
	private static String removeParens(String query) {
		while (query.indexOf('(') >= 0)
			query = query.substring(0, query.indexOf('(')) + ' '
					+ query.substring(query.indexOf('(') + 1);

		while (query.indexOf(')') >= 0) {
			query = query.substring(0, query.indexOf(')')) + ' '
					+ query.substring(query.indexOf(')') + 1);
		}

		return query;
	}

	/**
	 * 
	 * @param query
	 *            the query
	 * @param idx
	 *            the index of the prospective end quote
	 * @return how many backslashes precede the quote
	 */
	private static int precedingSlashes(String query, int idx) {
		for (int i = 1; i <= idx; i++) {
			if (query.charAt(idx - i) != '\\')
				return i - 1;
		}
		return idx - 1;
	}

	/**
	 * 
	 * @param query
	 *            the query
	 * @param idx
	 *            the end index of the first string literal
	 * @return whether there is a second string literal concatenated to this one
	 */
	private static boolean concattedString(String query, int idx) {
		int i;

		for (i = idx + 1; query.charAt(i) == ' '; i++)
			;

		if (query.charAt(i) == '\'' || query.charAt(i) == '\"')
			return true;
		return false;
	}

	/**
	 * 
	 * @param query
	 *            the query
	 * @param idx
	 *            the end index of the first string literal
	 * @return the index of the next string's starting quote
	 */
	private static int concattedStringStartIdx(String query, int idx) {
		int i;

		for (i = idx + 1; query.charAt(i) == ' '; i++)
			;

		return i;
	}

	/**
	 * 
	 * @param query
	 *            the query
	 * @return a modified query with all string literals removed
	 */
	private static String extractStrings(String query) {
		int startIdx = 0, endIdx;

		while (true) {
			int firstSingle = query.indexOf('\'', startIdx);
			int firstDouble = query.indexOf('\"', startIdx);

			// The character that we are trying to match that way we can have
			// stuff like '"' or "'"
			char quoteChar;

			// if and only if there are no more quotes, we are done
			if (firstDouble < 0 && firstSingle < 0)
				return query;

			if (firstSingle < 0) {
				startIdx = firstDouble;
			} else if (firstDouble < 0) {
				startIdx = firstSingle;
			} else {
				startIdx = Math.min(firstSingle, firstDouble);
			}

			// See what we're trying to match
			quoteChar = query.charAt (startIdx);

			endIdx = query.indexOf(quoteChar, startIdx + 1);

			while (true) {
				while (precedingSlashes(query, endIdx) % 2 == 1
						|| query.charAt(endIdx + 1) == quoteChar) {

					// make sure the quote isn't escaped
					while (precedingSlashes(query, endIdx) % 2 == 1) {
						endIdx = query.indexOf(quoteChar, endIdx + 1);
					}
					// make sure it isn't a literal quote ex: '' , ""
					while (query.charAt(endIdx + 1) == quoteChar)
						endIdx = query.indexOf(quoteChar, endIdx + 2);
				}

				// If there is a string concatenated to the end of this one ex:
				// 'hello' "world"
				if (concattedString(query, endIdx)) {
					endIdx = concattedStringStartIdx(query, endIdx);
					quoteChar = query.charAt(endIdx);
					endIdx = query.indexOf(quoteChar, endIdx + 1);
				} else {
					// We only break out when the
					break;
				}
			}

			// add the string to the set and remove it from the query
			stringSet.add(query.substring(startIdx, endIdx + 1));
			query = query.substring(0, startIdx) + query.substring(endIdx + 1);
		}
	}

	public static void main(String[] args) {
		QueryProcessor qp = new QueryProcessor();
		qp.processQuery("");
		System.out.println("Doubles: "
				+ Arrays.toString(qp.doubleSet.toArray()));
		System.out.println("Ints: " + Arrays.toString(qp.intSet.toArray()));
		System.out.println("Strings: "
				+ Arrays.toString(qp.stringSet.toArray()));

	}
}
