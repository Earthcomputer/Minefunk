package net.earthcomputer.minefunk.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The AST visitor used to generate Minecraft commands
 * 
 * @author Earthcomputer
 */
public class CommandListVisitor extends IndexVisitor {

	@Override
	public Object visit(ASTFunction node, Object data) {
		if ((ASTUtil.getModifiers(node) & Modifiers.INLINE) == 0) {
			String funcId = ((Data) data).index.getFunctionId(node);
			List<String> commands = new ArrayList<>();
			StatementParser.toCommandList(ASTUtil.getBody(node), ((Data) data).index, commands,
					((Data) data).exceptions);
			((Data) data).commandLists.put(funcId, commands);
		}
		return data;
	}

	public static class Data implements IIndexVisitorData {
		private Index index;
		private Map<String, List<String>> commandLists;
		private List<ParseException> exceptions;

		public Data(Index index, Map<String, List<String>> commandLists, List<ParseException> exceptions) {
			this.index = index;
			this.commandLists = commandLists;
			this.exceptions = exceptions;
		}

		@Override
		public Index getIndex() {
			return index;
		}

		@Override
		public List<ParseException> getExceptions() {
			return exceptions;
		}
	}

}
