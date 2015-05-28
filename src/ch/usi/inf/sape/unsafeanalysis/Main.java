package ch.usi.inf.sape.unsafeanalysis;


public class Main {

////	private static final Log log = new Log(System.err);
////
////	private static void showSummary(MavenIndex index) {
////
////		log.log("uniqueArtifactsCount: %,d", index.uniqueArtifactsCount);
////		log.log("totalSize: %,d MB", index.totalSize / (1024 * 1024));
////		log.log("lastVersionJarsSize: %,d MB", index.lastVersionJarsSize
////				/ (1024 * 1024));
////		log.log("mmindate: %s", index.mmindate);
////		log.log("mmindate: %s", index.mmaxdate);
////		log.log("mmindate: %s", index.imindate);
////		log.log("mmindate: %s", index.imaxdate);
////		log.log("Root groups list (%d): %s", index.rootGroupsList.size(),
////				index.rootGroupsList);
////		log.log("Field set (%d): %s", index.fieldSet.size(), index.fieldSet);
////		log.log("Extension set (%d): %s", index.extSet.size(), index.extSet);
////	}
////
////	private static MavenIndex build(Class<?> mainClass) throws Exception {
////		log.log("Running class %s", mainClass);
////
////		String indexPath = MavenDBProperties.get().indexPath();
////		IndexSearcher searcher = new IndexSearcher(indexPath);
////
////		log.log("Maven Index contains %,d documents", searcher.maxDoc());
////
////		MavenIndex index = MavenIndexBuilder.build(searcher);
////		showSummary(index);
////
////		return index;
////	}
////
////	public static class Build {
////		public static void main(String args[]) throws Exception {
////			MavenIndex index = build(Build.class);
////
////			log.log("Serializing index... ");
////
////			String fileName = MavenDBProperties.get().artifactsPath();
////			new File(new File(fileName).getParent()).mkdirs();
////			try (PrintStream out = new PrintStream(fileName)) {
////				index.print(out);
////			}
////
////			log.log("DONE");
////		}
////	}
////
////
//


	
}
