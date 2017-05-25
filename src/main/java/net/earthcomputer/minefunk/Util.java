package net.earthcomputer.minefunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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
	
	public static ParseException createParseException(String message, Token token) {
		ParseException e = new ParseException(message);
		e.currentToken = token;
		return e;
	}

}
