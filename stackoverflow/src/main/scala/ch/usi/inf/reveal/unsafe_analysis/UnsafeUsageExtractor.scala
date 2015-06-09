package ch.usi.inf.reveal.unsafe_analysis

import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger
import scala.io.Source
import ch.usi.inf.reveal.parsing.artifact.ArtifactSerializer
import ch.usi.inf.reveal.parsing.units.TextUnit
import scala.util.Try

object UnsafeUsageExtractor extends App {
  println("* Step 2: Unsafe usage extractor. ")
    
  if (args.size < 3) {
    println("Usage error (wrong number of arguments). ")
    println("Usage: UnsafeUsageExtractor <artifact-folder> <candidates-file> <destination-file>")
    println("Example: UnsafeUsageExtractor ./data/json ./results/candidates.csv ./results/intermediate.csv")
    println("<artifact-folder> is the folder containing serialized stack overflow posts.")
    println("<candidates-file> is the file obtained from step 1.")
    println("<destination-file> is the file to be written containing the intermediate usage results.")
    
    System.exit(-1)
  }
  
  
  print("* Loading posts...")
  
  val analyzer = new CandidatesAnalyzer
  val serializer = new ArtifactSerializer()
  val ids = Source.fromFile(args(1)).getLines().map{ _.toInt }.toList.par
  
  val rawArtifacts = ids.flatMap { id => 
    Try { 
      val jsonFile = new java.io.File(args(0),s"$id.json")
      serializer.deserializeFromFile(jsonFile) 
    }.toOption
  }
  
  println(s" ${rawArtifacts.size} posts loaded. ")
  
  val badParks = {
    val dsc = rawArtifacts.filter { artifact =>
      artifact.asPostSeq.forall { a =>
        val text = a.allUnits.map{_.rawText}.mkString("" , "", "")
        val textOnly = a.allUnits.filter { _.isInstanceOf[TextUnit]}.mkString("" , "", "")
        ((text.toLowerCase.contains("sun.misc.unsafe.park(native") ||
         text.toLowerCase.contains("sun.misc.unsafe.park(unsafe"))== text.toLowerCase.contains("unsafe"))
      }  
    }
    
    (dsc).toList.map{_.id}
  }
  
  //**********************************************************************    
  println(s"* Irrelevant usages of park: ${badParks.size}")
  //**********************************************************************
   
  val artifacts = rawArtifacts filter { e => !(badParks contains e.id) }
  
  val ctr = new AtomicInteger(0)
  print("* Extracting usage info...")
  val res = artifacts.flatMap{ artifact => 
    val posts = artifact.asPostSeq 
    val resForAllPosts = posts.flatMap(post => analyzer.analyze(artifact, post))
    val hasTypeInArtifact = resForAllPosts.exists(_.hasType)
    val v = ctr.incrementAndGet()
    resForAllPosts.map { usage => UnsafeUsage(usage.artifact, usage.methodName, usage.post, usage.hasType, usage.hasUnsafeWord, hasTypeInArtifact)}
  }
  println("Done!")
  print("* Writing intermediate result file...")
    
  val contents = res.map(_.toCsvLine).mkString("\n")
  val writer = new PrintWriter(args(2))
  val tagsHeaders = (1 to 5).map(t => s"tag$t").mkString(",")
  writer.println("artifact,post,hasTypeInArtifact,hasTypeInPost,hasUnsafeWord,methodName,"+tagsHeaders)
  writer.write(contents)
  writer.close()
  println("Done!")
  
} 