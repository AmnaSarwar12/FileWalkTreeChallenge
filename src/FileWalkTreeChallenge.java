import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class FileWalkTreeChallenge {
	public static void main(String[] args) {
		Path straightPath = Path.of(".");
		FileVisitor<Path> statsVisitor = new statsVisitors(Integer.MAX_VALUE);
		try {
			Files.walkFileTree(straightPath, statsVisitor);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


	}
	private static class statsVisitors implements FileVisitor<Path>{
		private Path initialPath = null;
		private Map<Path,Map<String, Long>> folderSize = new LinkedHashMap<>();
		private int initailCount;

		private int printLevel;

		private static final String File_Size = "filesize";
		private static final String DIR_CNT= "dirCount";
		private static final String FILE_CNT = "fileCount";
		public statsVisitors(int printLevel) {
			this.printLevel = printLevel;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			Objects.requireNonNull(file);
			Objects.requireNonNull(attrs);
			//folderSize.merge(file.getParent(), 0L, (o, n) -> o+= attrs.size() );
			var ParentMap = folderSize.get(file.getParent());
			if(ParentMap != null){
				long fileSize = attrs.size();
				ParentMap.merge(File_Size, fileSize, (o,n) -> o += n);
				ParentMap.merge(FILE_CNT, 1L, Math::addExact);
			}

			return FileVisitResult.CONTINUE;
			//return super.visitFile(file, attrs);
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			Objects.requireNonNull(file);

			if(exc != null){
				System.out.println(exc.getClass().getSimpleName() + " " + file);

			}
			return FileVisitResult.CONTINUE;
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
				folderSize.put(dir, new HashMap<>());
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
					if(level < printLevel){
						long size = value.getOrDefault(File_Size, 0L);
						System.out.printf("%s[%s] - %,d bytes, %d files, %d folder %n","\t".repeat(level)
						, key.getFileName(), size, value.getOrDefault(FILE_CNT, 0L),
								value.getOrDefault(DIR_CNT, 0L));
					}
				});
			}else{
				var parentMap = folderSize.get(dir.getParent());
				var childMap = folderSize.get(dir);
				long folderCount = childMap.getOrDefault(DIR_CNT, 0L);
				long fileSize = childMap.getOrDefault(File_Size, 0L);
				long fileCount = childMap.getOrDefault(FILE_CNT, 0L);
				parentMap.merge(DIR_CNT, folderCount + 1, (o,n)-> o += n);
				parentMap.merge(File_Size, fileSize, Math::addExact);
				parentMap.merge(FILE_CNT, fileCount, Math::addExact);
			}
//			if (exc != null)
//				throw exc;
			return FileVisitResult.CONTINUE;
		}
	}
	
}