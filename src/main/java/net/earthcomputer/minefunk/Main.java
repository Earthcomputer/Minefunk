package net.earthcomputer.minefunk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import net.earthcomputer.minefunk.parser.ASTProcessor;
import net.earthcomputer.minefunk.parser.ASTRoot;
import net.earthcomputer.minefunk.parser.Index;
import net.earthcomputer.minefunk.parser.MinefunkParser;
import net.earthcomputer.minefunk.parser.ParseException;

public class Main {

	private static void printUsage() {
		System.out.println("java -jar minefunk.jar <source-files>");
	}

	public static void main(String[] args) throws ParseException, IOException {
		if (args.length == 0) {
			printUsage();
			return;
		}
		FileMatcher fileMatcher = new FileMatcher(new File(".").getAbsoluteFile().toPath(), args[0]);
		List<File> inputFiles = new ArrayList<>();
		Files.walkFileTree(new File(".").getAbsoluteFile().toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (fileMatcher.accept(file.toFile())) {
					inputFiles.add(file.toFile());
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
				System.err.println("Failed to read file " + file);
				e.printStackTrace();
				return FileVisitResult.CONTINUE;
			}
		});

		List<ASTRoot> asts = new ArrayList<>(inputFiles.size());
		Index index = new Index();
		List<ParseException> exceptions = new ArrayList<>();

		for (File inputFile : inputFiles) {
			InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
			ASTRoot root = new MinefunkParser(in).parse();
			in.close();
			asts.add(root);

			ASTProcessor.preIndexCheck(root, exceptions);
			if (handleExceptions(exceptions)) {
				return;
			}

			ASTProcessor.index(root, index, exceptions);
			if (handleExceptions(exceptions)) {
				return;
			}

			index.resolvePendingFunctions(exceptions);
			if (handleExceptions(exceptions)) {
				return;
			}
		}

		for (ASTRoot root : asts) {
			ASTProcessor.postIndexCheck(root, index, exceptions);
			if (handleExceptions(exceptions)) {
				return;
			}
		}

		Map<String, List<String>> commandLists = new HashMap<>();
		for (ASTRoot root : asts) {
			ASTProcessor.generateCommandLists(root, index, commandLists, exceptions);
			if (handleExceptions(exceptions)) {
				return;
			}
		}

		commandLists.forEach((funcId, commands) -> {
			System.out.println(funcId);
			commands.forEach(command -> {
				System.out.println("  " + command);
			});
		});
	}

	private static boolean handleExceptions(List<ParseException> exceptions) {
		if (!exceptions.isEmpty()) {
			exceptions.forEach(ParseException::printStackTrace);
			return true;
		} else {
			return false;
		}
	}

}
