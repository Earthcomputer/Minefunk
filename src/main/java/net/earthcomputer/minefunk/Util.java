package net.earthcomputer.minefunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import net.earthcomputer.minefunk.parser.ASTNodeValue;
import net.earthcomputer.minefunk.parser.ASTUtil;
import net.earthcomputer.minefunk.parser.MinefunkParserConstants;
import net.earthcomputer.minefunk.parser.Node;
import net.earthcomputer.minefunk.parser.ParseException;
import net.earthcomputer.minefunk.parser.Token;

/**
 * General utilities
 * 
 * @author Earthcomputer
 *
 */
public class Util {

	/**
	 * Converts a deque to a new array list.
	 * 
	 * @param deque
	 *            - the deque to convert
	 * @return The converted list
	 * @see #dequeToList(Deque, List)
	 */
	public static <E> ArrayList<E> dequeToList(Deque<E> deque) {
		return dequeToList(deque, new ArrayList<>(deque.size()));
	}

	/**
	 * Converts a deque to the given list. The head (top) of the deque will be
	 * the first element in the list.
	 * 
	 * @param deque
	 *            - the deque to convert
	 * @param list
	 *            - the list to add elements to
	 * @return The input list with elements having been added
	 */
	public static <E, T extends List<E>> T dequeToList(Deque<E> deque, T list) {
		deque.descendingIterator().forEachRemaining(list::add);
		return list;
	}

	/**
	 * Converts a list to a new array deque.
	 * 
	 * @param list
	 *            - the list to convert
	 * @return The converted deque
	 * @see #listToDeque(List, Deque)
	 */
	public static <E> ArrayDeque<E> listToDeque(List<E> list) {
		return listToDeque(list, new ArrayDeque<>(list.size()));
	}

	/**
	 * Converts a list to the given deque. The first element in the list will be
	 * the head (top) of the deque.
	 * 
	 * @param list
	 *            - the list to convert
	 * @param deque
	 *            - the deque to add elements to
	 * @return The input deque with elements having been added
	 */
	public static <E, T extends Deque<E>> T listToDeque(List<E> list, T deque) {
		list.forEach(deque::addLast);
		return deque;
	}

	/**
	 * Creates a syntax error on the given node with the given message
	 * 
	 * @param message
	 *            - the message
	 * @param node
	 *            - the AST node
	 * @return The newly created parse exception
	 */
	public static ParseException createParseException(String message, Node node) {
		return createParseException(message, createToken(node));
	}

	/**
	 * Creates a syntax error on the given token with the given message. Note
	 * the token doesn't need an image, it can just be used to describe a range
	 * in the source code.
	 * 
	 * @param message
	 *            - the message
	 * @param token
	 *            - the token (range in the source code)
	 * @return The newly created parse exception
	 */
	public static ParseException createParseException(String message, Token token) {
		ParseException e = new ParseException(message);
		e.currentToken = token;
		return e;
	}

	/**
	 * Creates a token, seeing as there is no easy constructor in the token
	 * class
	 * 
	 * @param image
	 *            - the token's image
	 * @param startLine
	 *            - the token's begin line
	 * @param startColumn
	 *            - the token's begin column
	 * @param endLine
	 *            - the token's end line
	 * @param endColumn
	 *            - the token's end column
	 * @return The newly created token
	 */
	public static Token createToken(String image, int startLine, int startColumn, int endLine, int endColumn) {
		Token tok = new Token(MinefunkParserConstants.EOF, image);
		tok.beginLine = startLine;
		tok.beginColumn = startColumn;
		tok.endLine = endLine;
		tok.endColumn = endColumn;
		return tok;
	}

	/**
	 * Creates a token (range in the source code) containing the given node.
	 * This token will not have an image
	 * 
	 * @param node
	 *            - the AST node
	 * @return The newly created token
	 */
	public static Token createToken(Node node) {
		ASTNodeValue value = ASTUtil.getNodeValue(node);
		Token tok = new Token(MinefunkParserConstants.EOF);
		tok.beginLine = value.getStartLine();
		tok.beginColumn = value.getStartColumn();
		tok.endLine = value.getEndLine();
		tok.endColumn = value.getEndColumn();
		return tok;
	}

	/**
	 * Finds a whitespace character with the same width as <tt>c</tt>.
	 * Specifically, if <tt>c</tt> is a whitespace character it returns
	 * <tt>c</tt>, and otherwise a space (' ').
	 * 
	 * @param c
	 *            - the possibly non-whitespace character
	 * @return The whitespace character
	 */
	public static char charToWhitespace(char c) {
		if (Character.isWhitespace(c)) {
			return c;
		} else {
			return ' ';
		}
	}

	/**
	 * Calculates the date of Easter in the given year. <a href=
	 * "https://en.wikipedia.org/wiki/Computus#Anonymous_Gregorian_algorithm">This</a>
	 * algorithm is used.
	 * 
	 * @param y
	 *            - the year
	 * @return A two-element array containing the 0-indexed month followed by
	 *         the 1-indexed day of the month
	 */
	public static int[] calculateEaster(int y) {
		int a = y % 19;
		int b = y / 100;
		int c = y % 100;
		int d = b >> 2; // b / 4
		int e = b & 3; // b % 4
		int g = ((b << 3) + 13) / 25; // (8 * b + 13) / 25
		int h = (19 * a + b - d - g + 15) / 30;
		int j = c >> 2; // c / 4
		int k = c & 3; // c % 4
		int m = (a + 11 * h) / 319;
		// (2 * e + 2 * j - k - h + m + 32) % 7
		int r = ((e << 1) + (j << 1) - k - h + m + 32) % 7;
		int n = (h - m + r + 90) / 25;
		int p = h - m + r + n + 19 & 31; // (h - m + r + n + 19) % 32
		return new int[] { n - 1, p };
	}

}
