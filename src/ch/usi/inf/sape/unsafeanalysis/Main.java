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
////	public static class Analyse {
////		public static void main(String[] args) throws Exception {
////			final MavenIndex index = build(Analyse.class);
////
////			List<UnsafeEntry> allMatches = new LinkedList<UnsafeEntry>();
////
////			int i = 0;
////			for (MavenArtifact a : index) {
////				i++;
////
////				String path = a.getPath();
////
////				try {
////					List<UnsafeEntry> matches = UnsafeAnalysis.searchJarFile(
////							"db/" + path, a);
////
////					allMatches.addAll(matches);
////				} catch (NoSuchFileException e) {
////					log.log("File not found %s (%dth)", path, i);
////				} catch (ZipException e) {
////					log.log("Zip exception for %s (%dth): %s", path, i,
////							e.getMessage());
////				} catch (Exception e) {
////					log.log("Exception for %s (%dth): %s", path, i,
////							e.getMessage());
////				}
////			}
////
////			String localPathCsv = "db/unsafe-maven.csv";
////
////			try (PrintStream out = new PrintStream(localPathCsv)) {
////				UnsafeAnalysis.printMatchesCsv(out, allMatches);
////			}
////		}
////	}
//
//	public static class AnalyseJdk8 {
//		public static void main(String[] args) throws Exception {
//
//			ArrayList<String> list = new ArrayList<String>();
//			try (BufferedReader br = new BufferedReader(new FileReader(
//					"jdk8/list.txt"))) {
//				String line;
//				while ((line = br.readLine()) != null) {
//					list.add(line);
//				}
//			}
//
//			List<UnsafeEntry> allMatches = new LinkedList<UnsafeEntry>();
//
//			int i = 0;
//			for (String file : list) {
//				MavenArtifact a = new MavenArtifact("jdk8", "jdk8", file
//						+ "@1.8", 1, "jar", "", "");
//
//				i++;
//
//				String path = a.getPath();
//
//				try {
//					List<UnsafeEntry> matches = UnsafeAnalysis.searchJarFile(
//							"jdk8/" + file, a);
//
//					allMatches.addAll(matches);
//				} catch (NoSuchFileException e) {
//					log.log("File not found %s (%dth)", path, i);
//				} catch (ZipException e) {
//					log.log("Zip exception for %s (%dth): %s", path, i,
//							e.getMessage());
//				} catch (Exception e) {
//					log.log("Exception for %s (%dth): %s", path, i,
//							e.getMessage());
//				}
//			}
//
//			String localPathCsv = "jdk8/unsafe-maven-jdk8.csv";
//
//			try (PrintStream out = new PrintStream(localPathCsv)) {
//				UnsafeAnalysis.printMatchesCsv(out, allMatches);
//			}
//		}
//	}
//
//	public static class Graph {
//		public static void main(String[] args) throws Exception {
//			final MavenIndex index = build(Graph.class);
//
//			String localPathCsv = "db/depgraph.csv";
//			try (PrintStream out = new PrintStream(localPathCsv)) {
//
//				out.println("groupId, artifactId, depGroupId, depArtifactId, depVersion, depScope");
//
//				for (final MavenArtifact a : index) {
//					String path = a.getPomPath();
//					try {
//						SAXParserFactory spf = SAXParserFactory.newInstance();
//
//						File f = new File("db/" + path);
//						spf.newSAXParser().parse(f, new DefaultHandler() {
//							private Dependency dep;
//							private String value;
//
//							@Override
//							public void startElement(String uri,
//									String localName, String qName,
//									Attributes attributes) throws SAXException {
//								if (qName.equals("dependency")) {
//									dep = new Dependency();
//								}
//							}
//
//							@Override
//							public void characters(char[] ch, int start,
//									int length) throws SAXException {
//								value = new String(ch, start, length);
//							}
//
//							@Override
//							public void endElement(String uri,
//									String localName, String qName)
//									throws SAXException {
//								if (qName.equals("dependency")) {
//									out.format("%s, %s, %s, %s, %s, %s\n",
//											a.groupId, a.artifactId,
//											dep.groupId, dep.artifactId,
//											dep.version, dep.scope);
//
//									a.dependencies.add(dep);
//									dep = null;
//								} else if (dep != null
//										&& qName.equals("groupId")) {
//									dep.groupId = value;
//								} else if (dep != null
//										&& qName.equals("artifactId")) {
//									dep.artifactId = value;
//								} else if (dep != null
//										&& qName.equals("version")) {
//									dep.version = value;
//								} else if (dep != null && qName.equals("scope")) {
//									dep.scope = value;
//								}
//							}
//						});
//					} catch (FileNotFoundException e) {
//						logger.info("POM not found %s", path);
//					} catch (Exception e) {
//						logger.log("Exception (%s) in %s: %s", e.getClass(), path,
//								e.getMessage());
//					}
//				}
//			}
//		}
//	}

	
}
