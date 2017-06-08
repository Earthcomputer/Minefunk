package net.earthcomputer.minefunk;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A class which contains utilities for analyzing call graphs
 * 
 * @author Earthcomputer
 */
public class CallGraphAnalyzer {

	private CallGraphAnalyzer() {
	}

	/**
	 * Finds groups of strongly connected components in a call graph. If a group
	 * of strongly connected components contains more than one node, then a
	 * cycle is present.
	 * 
	 * @author Earthcomputer
	 *
	 * @param <T>
	 *            - the type of a call graph node
	 */
	// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
	public static class StronglyConnectedComponentsFinder<T> {

		private Map<T, Set<T>> graph;
		private int index;
		private Map<T, Integer> indexes = new HashMap<>();
		private Map<T, Integer> lowlinks = new HashMap<>();
		private Set<T> onStack = new HashSet<>();
		private Deque<T> stack = new ArrayDeque<>();
		private Result<T> result;

		public StronglyConnectedComponentsFinder(Map<T, Set<T>> graph) {
			this.graph = graph;
		}

		public Result<T> findStronglyConnectedComponents() {
			indexes.clear();
			lowlinks.clear();
			onStack.clear();
			result = new Result<>();

			index = 0;
			stack.clear();
			graph.keySet().forEach(vertex -> {
				if (!indexes.containsKey(vertex)) {
					strongConnect(vertex);
				}
			});

			return result;
		}

		private void strongConnect(T vertex) {
			indexes.put(vertex, index);
			lowlinks.put(vertex, index);
			index++;
			stack.push(vertex);
			onStack.add(vertex);

			graph.get(vertex).forEach(nextVertex -> {
				if (!indexes.containsKey(nextVertex)) {
					strongConnect(nextVertex);
					int nextVertexLowlink = lowlinks.get(nextVertex);
					if (nextVertexLowlink < lowlinks.get(vertex)) {
						lowlinks.put(vertex, nextVertexLowlink);
					}
				} else if (onStack.contains(nextVertex)) {
					int nextVertexIndex = indexes.get(nextVertex);
					if (nextVertexIndex < lowlinks.get(vertex)) {
						lowlinks.put(vertex, nextVertexIndex);
					}
				}
			});

			// Boxed integers will compare identity if not unboxed
			if (lowlinks.get(vertex).intValue() == indexes.get(vertex).intValue()) {
				Set<T> group = new HashSet<>();
				T node;
				do {
					node = stack.pop();
					onStack.remove(node);
					group.add(node);
					result.groups.put(node, group);
				} while (!node.equals(vertex));
			}
		}

		/**
		 * A container class containing the result of this algorithm
		 * 
		 * @author Earthcomputer
		 *
		 * @param <T>
		 *            - the type of a call graph node
		 */
		public static class Result<T> {
			private Map<T, Set<T>> groups = new HashMap<>();

			private Result() {
			}

			public Set<T> getConnectedComponents() {
				return groups.keySet().stream().filter(this::isConnectedComponent).collect(Collectors.toSet());
			}

			public boolean isConnectedComponent(T node) {
				return groups.get(node).size() > 1;
			}

			public Set<Set<T>> getConnectedGroups() {
				return groups.values().stream().distinct().collect(Collectors.toSet());
			}
		}

	}

}
