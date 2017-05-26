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

public class Util {

	public static <E> ArrayList<E> dequeToList(Deque<E> deque) {
		return dequeToList(deque, new ArrayList<>(deque.size()));
	}

	public static <E, T extends List<E>> T dequeToList(Deque<E> deque, T list) {
		deque.descendingIterator().forEachRemaining(list::add);
		return list;
	}

	public static <E> ArrayDeque<E> listToDeque(List<E> list) {
		return listToDeque(list, new ArrayDeque<>(list.size()));
	}

	public static <E, T extends Deque<E>> T listToDeque(List<E> list, T deque) {
		list.forEach(deque::addLast);
		return deque;
	}

	public static ParseException createParseException(String message, Node node) {
		return createParseException(message, createToken(node));
	}
	
	public static ParseException createParseException(String message, Token token) {
		ParseException e = new ParseException(message);
		e.currentToken = token;
		return e;
	}

	public static Token createToken(String image, int startLine, int startColumn, int endLine, int endColumn) {
		Token tok = new Token(MinefunkParserConstants.EOF, image);
		tok.beginLine = startLine;
		tok.beginColumn = startColumn;
		tok.endLine = endLine;
		tok.endColumn = endColumn;
		return tok;
	}

	public static Token createToken(Node node) {
		ASTNodeValue value = ASTUtil.getNodeValue(node);
		Token tok = new Token(MinefunkParserConstants.EOF);
		tok.beginLine = value.getStartLine();
		tok.beginColumn = value.getStartColumn();
		tok.endLine = value.getEndLine();
		tok.endColumn = value.getEndColumn();
		return tok;
	}
	
	public static char charToWhitespace(char c) {
		if (Character.isWhitespace(c)) {
			return c;
		} else {
			return ' ';
		}
	}
	
}
