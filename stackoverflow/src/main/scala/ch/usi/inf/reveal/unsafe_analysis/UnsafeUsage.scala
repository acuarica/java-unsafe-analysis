package ch.usi.inf.reveal.unsafe_analysis

import ch.usi.inf.reveal.parsing.artifact.StackOverflowArtifact
import ch.usi.inf.reveal.parsing.artifact.StackOverflowPost



case class UnsafeUsage(
  val artifact: StackOverflowArtifact,
  val methodName: Option[String],
  val post: StackOverflowPost,
  val hasType: Boolean,
  val hasUnsafeWord: Boolean,
  val hasTypeInArtifact: Boolean = false
  ){
  
  def hasMethod = methodName.isDefined 
  
  def tags = {
   val tagList = artifact.question.tags
   val missing = (0 until (5 - tagList.size)).map(_ => "")
   tagList ++ missing 
  }
  
  def toCsvLine = {
   val tagStr = tags.mkString(",")
   s"${artifact.id},${post.id},$hasTypeInArtifact,$hasType,${hasUnsafeWord},${methodName.getOrElse("")},$tagStr" 
  }
  
}
  
  
  
  
  