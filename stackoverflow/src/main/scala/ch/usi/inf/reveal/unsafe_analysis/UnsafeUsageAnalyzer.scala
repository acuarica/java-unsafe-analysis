package ch.usi.inf.reveal.unsafe_analysis

import scala.io.Source
import java.io._
import ch.usi.inf.reveal.parsing.units.TextUnit
import ch.usi.inf.reveal.parsing.artifact.ArtifactSerializer
import scala.util.Try


/**
 * @author ponza
 */

case class PlainUnsafeUsage(artifactId: Int, postId: Int, hasTypeInArtifact: Boolean, hasTypeInPost: Boolean, hasWordInPost: Boolean, methodName: String)
case class MethodUsage(methodName: String, occurrencesInQuestion: Int, occurrencesInAnswers: Int, occurrencesInBoth: Int) {
  val total = occurrencesInQuestion + occurrencesInAnswers + occurrencesInBoth
}

object UnsafeUsageAnalyzer extends App {
  
  println("* Step 3: Unsafe usage analysis. ")
    
  if (args.size < 3) {
    println("Usage error (wrong number of arguments). ")
    println("Usage: UnsafeUsageAnalyzer <artifact-folder> <candidates-file> <intermediate-usage-file>")
    println("Example: UnsafeUsageAnalyzer ./data/json ./results/candidates.csv ./results/intermediate.csv")
    println("<artifact-folder> is the folder containing serialized stack overflow posts.")
    println("<candidates-file> is the file obtained from step 1.")
    println("<intermediate-usage-file> is the file containing the intermediate usage results from step 2.")
    
    System.exit(-1)
  }
  
  val lines = Source.fromFile(args(2)).getLines
  val methods = Source.fromFile("./methodNames.txt").getLines.toList
 
  val rawEntries = lines.drop(1).map( line => { 
    val elems = line.split(",").toList
    PlainUnsafeUsage(elems(0).toInt, elems(1).toInt, elems(2).toBoolean, elems(3).toBoolean, elems(4).toBoolean, if (elems.size > 5) elems(5) else "" )
  }).toList
  
  val rawQuestionIds = rawEntries.map { _.artifactId }.distinct  
  
  val analyzer = new CandidatesAnalyzer
  val serializer = new ArtifactSerializer()
  val ids = Source.fromFile(args(1)).getLines().map{ _.toInt }.toList.par
  
  val rawArtifacts = ids.flatMap { id => 
    Try { 
      val jsonFile = new File(args(0),s"$id.json")
      serializer.deserializeFromFile(jsonFile) 
    }.toOption
  }
     
  val entries = rawEntries
  val artifacts = rawArtifacts
      
  val id2artifact = artifacts.map { a => (a.id, a) }.toMap
  
  val totalQuestionIds = rawArtifacts.map{_.id}
  
  val methodUsages = entries.filter { _.methodName != "" }
  val artifactIdsWithMethodUsages = methodUsages.map { _.artifactId }.distinct
  val artifactIdsWithoutMethodUsages = totalQuestionIds.diff(artifactIdsWithMethodUsages).distinct
  
  //**********************************************************************
  println(s"* Total Artifacts (Questions): ${totalQuestionIds.size}")
  println(s"* Total Artifacts (Questions) with method mentions: ${artifactIdsWithMethodUsages.size}")
  println(s"* Total Artifacts (Questions) without mentions: ${artifactIdsWithoutMethodUsages.size}")
  println(s"* Total method mentions: ${methodUsages.size}")
  //**********************************************************************  

  val totalPosts = artifacts.flatMap { _.answers.map{_.id} }  
    
  val usagesPerMethod = entries.groupBy( usage => usage.methodName ) withDefaultValue List()
    
  val total = usagesPerMethod.map { e => e._2.size }.toList.reduce(_+_)
  
  val postsWithMethodUsage =  (usagesPerMethod.filter{ _._1 != ""}).values.flatten.map{_.artifactId}.toList.distinct

  val trueUsagesPerMethod = (usagesPerMethod-(""))
  
  val usage2mentions = methods.map { method =>
    (method, trueUsagesPerMethod(method).size)
  }
  
  val questionsUsages = methodUsages.filter(usage => usage.artifactId == usage.postId)
  val answeredQuestionUsages = questionsUsages.filter { usage => id2artifact(usage.artifactId).answers.size > 0 } //questionsUsages.filter { q => methodUsages.filter{_.artifactId == q.artifactId}.exists { u => u.postId != q.artifactId }}
  
  val questionsWithMethod = questionsUsages.filter { usage => usage.methodName == "" }
  val questionsWithoutMethod = questionsUsages.filter { usage => usage.methodName != "" }.map{_.artifactId}.distinct
    
  val unanswered = (questionsUsages.map {_.artifactId} diff answeredQuestionUsages.map{_.artifactId})
  
  val usagesInQuestions = methodUsages.filter{ u => u.artifactId == u.postId && u.methodName != "" }
  val usagesInAnswers = methodUsages.filter { u => u.artifactId != u.postId && u.methodName != "" }
  
  val usagesInQuestionsByMethod = usagesInQuestions groupBy { _.methodName } withDefaultValue List()
  val usagesInAnswersByMethod = usagesInAnswers groupBy { _.methodName } withDefaultValue List()
  
  val usagesInQuestionsOnlyByMethod = methods.map { method =>
    val usageInQuestion = usagesInQuestionsByMethod(method)
    val usageInAnswer = usagesInAnswersByMethod(method)
    val usageInQuestionOnly = usageInQuestion.filterNot { uq => usageInAnswer.exists { ua => uq.artifactId == ua.artifactId } }
    (method, usageInQuestionOnly)
  }.toMap
  
   val usagesInAnswersOnlyByMethod = methods.map { method =>
    val usageInQuestion = usagesInQuestionsByMethod(method)
    val usageInAnswer = usagesInAnswersByMethod(method)
    val usageInAnswerOnly = usageInAnswer.filterNot { ua => usageInQuestion.exists { uq => uq.artifactId == ua.artifactId } }
    (method, usageInAnswerOnly)
  }.toMap
  
  val usagesInBothByMethod = methods.map { method =>
    val usageInQuestion = usagesInQuestionsByMethod(method)
    val usageInAnswer = usagesInAnswersByMethod(method)
    val usageInBoth = usageInAnswer.filter { ua => usageInQuestion.exists { uq => uq.artifactId == ua.artifactId } }
    (method, usageInBoth)
  }.toMap
  
  print("* Writing results...")
  
  {
    val writer = new PrintWriter("./results/method-usages.csv")
    val writer_detail = new PrintWriter("./results/method-usages-detail.csv")
    writer.println("method,usagesInQuestionsOnly,usagesInAnswersOnly,usagesInBoth")
    writer_detail.println("method,question,answer_optional,where")
    val toBePlotted = methods.map { method =>
      val uqs = usagesInQuestionsOnlyByMethod(method).size
      usagesInQuestionsOnlyByMethod(method).foreach { u => writer_detail.println(s"${u.methodName},${u.postId},,only_question")}
      val uas = usagesInAnswersOnlyByMethod(method).size
      usagesInAnswersOnlyByMethod(method).foreach { u => writer_detail.println(s"${u.methodName},${u.artifactId},${u.postId},only_answer")}
      val ubs = usagesInBothByMethod(method).size
      usagesInBothByMethod(method).foreach { u => writer_detail.println(s"${u.methodName},${u.artifactId},${u.postId},both")}
      MethodUsage(method,uqs,uas,ubs)
    }.foreach { mu =>
      writer.println(s"${mu.methodName},${mu.occurrencesInQuestion},${mu.occurrencesInAnswers},${mu.occurrencesInBoth}")
    }
    writer.close()
    writer_detail.close()
    println("done.")
  }
  
}