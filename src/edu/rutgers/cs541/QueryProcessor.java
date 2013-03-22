package edu.rutgers.cs541;

import java.util.HashSet;

/**
 * 
 * The Query Processor class which is meant to go through the queries and
 * extract all the values which could be important
 * 
 * @author Brian
 * 
 */

public class QueryProcessor {
	HashSet<String> tokenSet;
	HashSet<String> stringSet;

	public void processQuery(String query) {

	}

	private int precedingSlashes(String query, int idx) {
		for (int i = 1; i <= idx; i++) {
			if (query.charAt(idx - i) != '\\')
				return i - 1;
		}
		return idx - 1;
	}

	private boolean concattedString(String query, int idx) {
		int i = idx + 1;

		while (query.charAt(i) == ' ')
			i++;

		if (query.charAt(i) == '\'' || query.charAt(i) == '\"')
			return true;
		return false;
	}

	private int concattedStringStartIdx(String query, int idx) {
		int i = idx + 1;

		while (query.charAt(i) == ' ')
			i++;

		return i;
	}

	private void extractStrings(String query) {
		int startIdx = 0, endIdx;

		while (true) {
			int firstSingle = query.indexOf('\'', startIdx);
			int firstDouble = query.indexOf('\"', startIdx);
			char quoteChar;

			if (firstDouble < 0 && firstSingle < 0)
				return;

			if (firstSingle < 0) {
				startIdx = firstDouble;
			} else if (firstDouble < 0) {
				startIdx = firstSingle;
			} else {
				startIdx = Math.min(firstSingle, firstDouble);
			}

			quoteChar = query.charAt(startIdx);

			endIdx = query.indexOf(quoteChar, startIdx + 1);

			while (true) {
				while (precedingSlashes(query, endIdx) % 2 == 1
						|| query.charAt(endIdx + 1) == quoteChar) {
					
					//make sure the quote isn't escaped
					while (precedingSlashes(query, endIdx) % 2 == 1)
						endIdx++;
					//make sure it isn't a literal quote ex: '' , ""
					while (query.charAt(endIdx + 1) == quoteChar)
						endIdx += 2;
				}

				//If there is a string concatenated to the end of this one ex: 'hello' "world"
				if (concattedString(query, endIdx)) {
					endIdx = concattedStringStartIdx(query, endIdx);
					quoteChar = query.charAt(endIdx);
					endIdx = query.indexOf(quoteChar, endIdx + 1);
				} else {
					break;
				}
			}

			stringSet.add(query.substring(startIdx + 1, endIdx));
			query = query.substring(0, startIdx) + query.substring(endIdx + 1);
		}
	}
}
