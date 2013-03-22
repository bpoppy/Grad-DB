package edu.rutgers.cs541;

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
	public HashSet<String> stringSet = new HashSet<String>();

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
	
	private void extractNumberValues(String query){
		String[] tokens = query.split("\\s+");
		for (String tok : tokens){
			try{
				doubleSet.add(Double.valueOf(tok));								
			} catch(Exception ex){}

			try{
				intSet.add(Integer.valueOf(tok));								
			} catch(Exception ex){}
		}
	}

	private String replaceOperators(String query) {
		Character[] charSet = { '(', ')', '+', '/', '=', '[', ']', '*', '-' };
		for (Character ch : charSet)
			while (query.indexOf(ch) >= 0)
				query = query.substring(0, query.indexOf(ch)) + ' '
						+ query.substring(query.indexOf(ch) + 1);

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

		while (query.indexOf(')') >= 0)
			query = query.substring(0, query.indexOf(')')) + ' '
					+ query.substring(query.indexOf(')') + 1);

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
		int i = idx + 1;

		while (query.charAt(i) == ' ')
			i++;

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
		int i = idx + 1;

		while (query.charAt(i) == ' ')
			i++;

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
					while (precedingSlashes(query, endIdx) % 2 == 1)
						endIdx++;
					// make sure it isn't a literal quote ex: '' , ""
					while (query.charAt(endIdx + 1) == quoteChar)
						endIdx += 2;
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
			stringSet.add(query.substring(startIdx + 1, endIdx));
			query = query.substring(0, startIdx) + query.substring(endIdx + 1);
		}
	}
}
