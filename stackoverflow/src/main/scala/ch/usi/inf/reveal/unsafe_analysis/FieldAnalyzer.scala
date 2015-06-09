package ch.usi.inf.reveal.unsafe_analysis

import scala.io._
import java.io._
import ch.usi.inf.reveal.parsing.artifact.ArtifactSerializer
import ch.usi.inf.reveal.parsing.units.TextUnit
import ch.usi.inf.reveal.parsing.model.java._

object FieldAnalyzer extends App{

  
  def getJsonIds(jsonFolder: String): List[Int] = {
    val folder = new File(jsonFolder)
    val jsonFiles = folder.list().par.filter{ _.endsWith(".json") }
    jsonFiles.map { e => e.replaceAll("\\.json","").toInt }.toList
  }
  
  
  val jsonDir = new File(args(0))
  val ids = getJsonIds(args(0))
  val serializer = new ArtifactSerializer()
  val unsafeFields = Source.fromFile(args(2)).getLines().toList
  val writer = new PrintWriter(args(3))
  
  
  println("Loading artifacts...")
  val rawArtifacts = ids.par.map { id => 
    val jsonFile = new File(args(0),s"$id.json")
    serializer.deserializeFromFile(jsonFile) 
  }
  
  println(s"Found ${rawArtifacts.size} json files.")
  print("Searching for Unsafe members usage...")
  val members = rawArtifacts.flatMap{ artifact =>
    val nodes = artifact.units.flatMap { unit => unit match {
      case textUnit: TextUnit =>  Seq(textUnit.astNode, textUnit.codeFragments) 
      case _ => Seq(unit.astNode)
      }
    }
    
    val visitors = nodes.par.map { _.accept(CandidatesVisitor()) }
    val merged = visitors.reduce((a,b) => a merge b)
    val identifiers = merged.ids.map{_.name}.toList
    val literals = merged.stringLiterals.map{ _.valueRep }.toList
    (literals ++ identifiers).filter { e => unsafeFields.contains(e) }
    
  }
  
  val grouped = members.groupBy { x => x }.seq
  
  println("done.")
  println("Saving...")
  grouped.foreach{ case(name, list) =>  writer.println(s"$name,${list.size}") }
  writer.close()
  print("done.")
}