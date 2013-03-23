package edu.rutgers.cs541;

import java.util.Arrays;
import java.util.HashSet;
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
	public TreeSet<Double> doubleSet = new TreeSet<Double>();
	public TreeSet<Integer> intSet = new TreeSet<Integer>();
	public TreeSet<String> stringSet = new TreeSet<String>();

	/**
	 * Process the query and extract all values we care about.
	 * 
	 * @param query
	 *            the query
	 */
	public void processQuery(String query) {
		query = extractStrings(query);
		query = replaceOperators(query);
		extractNumberValues(query);
	}

	private void extractNumberValues(String query) {
		String[] tokens = query.split("\\s+");
		System.out.println(Arrays.toString(tokens));
		for (String tok : tokens) {
			try {
				double parsedValue;
				parsedValue = Double.parseDouble(tok);
				doubleSet.add(parsedValue);
				if(parsedValue == (int)parsedValue){
					intSet.add((int)parsedValue);					
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

	private String replaceOperators(String query) {
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
	private String removeParens(String query) {
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
	private int precedingSlashes(String query, int idx) {
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
	private boolean concattedString(String query, int idx) {
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
	private int concattedStringStartIdx(String query, int idx) {
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
	private String extractStrings(String query) {
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
			quoteChar = query.charAt(startIdx);

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
		qp.processQuery("SELECT id FROM T2 WHERE id > 0x5FA5 OR id < 17 AND NOT tag = \"tag:\\\"1\\\"\" AND name IN (SELECT * FROM T1 WHERE name LIKE \"Brian\" ' Poppy' OR AVG(score) + 20.0 > 5.5)");
		System.out.println("Doubles: "
				+ Arrays.toString(qp.doubleSet.toArray()));
		System.out.println("Ints: " + Arrays.toString(qp.intSet.toArray()));
		System.out.println("Strings: "
				+ Arrays.toString(qp.stringSet.toArray()));

	}
}
