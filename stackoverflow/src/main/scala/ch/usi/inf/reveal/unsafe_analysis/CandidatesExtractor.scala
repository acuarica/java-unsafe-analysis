package ch.usi.inf.reveal.unsafe_analysis

import scala.collection.GenSeq
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.util.Failure
import scala.util.Try
import ch.usi.inf.reveal.parsing.artifact.StackOverflowArtifact
import java.io.File
import ch.usi.inf.reveal.parsing.artifact.ArtifactSerializer
import ch.usi.inf.reveal.parsing.units.CodeTypesMetaInformation
import java.io.PrintWriter

object CandidatesExtractor extends App {
  println("* Step 1: Extracts candidate questions. ")
    
  if (args.size < 2) {
    println("Usage error (wrong number of arguments). ")
    println("Usage: CandidatesExtractor <artifact-folder> <candidates-file>")
    println("Example: CandidatesExtractor ./data/json ./results/candidates.csv")
    println("<artifact-folder> is the folder containing serialized stack overflow posts (e.g., ./data/json)")
    println("<candidates-file> is the csv file to be written with the candidate posts for next steps. ")
    
    System.exit(-1)
  }
  
  val folder = args(0)
  val serializer = new ArtifactSerializer()
  
  def getJsonIds(jsonFolder: String): List[Int] = {
    val folder = new File(jsonFolder)
    val jsonFiles = folder.list().par.filter{ _.endsWith(".json") }
    jsonFiles.map { e => e.replaceAll("\\.json","").toInt }.toList
  }
    
  def deserializeArtifacts(ids: List[Int]) = ids.flatMap { id => 
    Try { 
      val jsonFile = new java.io.File(folder,s"$id.json")
      serializer.deserializeFromFile(jsonFile) 
    }.toOption
  }
  
  def filterArtifacts(artifacts: List[StackOverflowArtifact]): List[Result] = {
     val analyzer = new CandidatesAnalyzer
     val analysis = artifacts.map { artifact => Try { analyzer.analyze(artifact) } }
     
     val successes = analysis.filter { _.isSuccess }.filter{ t => (t.get.hasUnsafeType || t.get.hasUnsafeMethod) }.map { _.get }
     val failures = analysis.filter{_.isFailure}
     println(s"analysis failed on ${failures.size} artifacts.")
     successes
   }
  
  print("* Getting ids...")
  val ids = getJsonIds(folder)
  println(s"${ids.size} files found on folder.")
  print("* Deserializing artifacts...")
  val artifacts = deserializeArtifacts(ids)
  
  val arts = artifacts.find { artifact => artifact.question.id == 17825178 }
  
  arts.foreach { artifact =>
    val metainfos = artifact.question.units.flatMap { unit => unit.type2metaInformation[CodeTypesMetaInformation] }
    
    metainfos.foreach { metainfo =>
      metainfo.qualifiedTypes.foreach{println}
    }  
  }
  
  println(s" ${artifacts.size} artifacts loaded.")
  print("* Filtering artifacts containing unsafe...")
  val result = filterArtifacts(artifacts)
  println(s"* ${result.size} potential artifacts containing methods or unsafe type mentions.")
  
  val foundIds = result.map{_.artifact.question.id}
  val toKeep = result.filter { res => res.artifact.units.exists { unit => unit.rawText.toLowerCase() contains "unsafe" } }
  
  println(s"* ${toKeep.size} artifacts also containing 'unsafe' word.")
 
  print(s"* Writing output (${args(1)})...")
  val writer = new PrintWriter(args(1))
  toKeep.foreach{ result => writer.println(result.artifact.id) }
  writer.close()
  println("Done.")
}



