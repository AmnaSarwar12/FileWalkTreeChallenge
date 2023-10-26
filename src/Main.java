import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
	public static void main(String[] args) {
		Path straightPath = Path.of(".");
		FileVisitor<Path> statsVisitor = new statsVisitors();
		try {
			Files.walkFileTree(straightPath, statsVisitor);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


	}
	private static class statsVisitors extends SimpleFileVisitor<Path>{
		private Path initialPath = null;
		private Map<Path,Long> folderSize = new LinkedHashMap<>();
		private int initailCount;


		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			Objects.requireNonNull(file);
			Objects.requireNonNull(attrs);
			folderSize.merge(file.getParent(), 0L, (o, n) -> o+= attrs.size() );
			return FileVisitResult.CONTINUE;
			//return super.visitFile(file, attrs);
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			Objects.requireNonNull(dir);
			Objects.requireNonNull(attrs);
			if(initialPath == null){
				initialPath = dir;
				initailCount = dir.getNameCount();
			}else{
				int relativeLevel = dir.getNameCount() - initailCount;
				if(relativeLevel == 1){
					folderSize.clear();
				}
				folderSize.put(dir, 0L);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			Objects.requireNonNull(dir);
			if(dir.equals(initialPath)){
				return FileVisitResult.TERMINATE;
			}
			int relativeLevel = dir.getNameCount() - initailCount;
			if(relativeLevel == 1){
				folderSize.forEach((key, value) -> {
					int level = key.getNameCount() - initailCount - 1;
					System.out.printf("%s[%s] - %,d bytes %n", "\t".repeat(level), key.getFileName(), value);
				});
			}else{
				long folderSizes = folderSize.get(dir);
				folderSize.merge(dir.getParent(), 0L, (o,n)-> o += folderSizes);
			}
//			if (exc != null)
//				throw exc;
			return FileVisitResult.CONTINUE;
		}
	}
	
}