package ch.usi.inf.reveal.unsafe_analysis

import ch.usi.inf.reveal.parsing.artifact.StackOverflowArtifact



case class Result(val artifact: StackOverflowArtifact, val methods: Set[String], val hasUnsafeType: Boolean, val failed: Boolean = false){
  
  lazy val hasUnsafeMethod = methods.size > 0
  
  def toCsvLine = s"${artifact.question.id},$hasUnsafeType,$hasUnsafeMethod"
}