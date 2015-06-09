package ch.usi.inf.reveal.unsafe_analysis


import reflect.runtime.universe._
import ch.usi.inf.reveal.parsing.artifact._
import ch.usi.inf.reveal.parsing.model._
import ch.usi.inf.reveal.parsing.model.java._
import ch.usi.inf.reveal.parsing.units._
import scala.io.Source

class CandidatesAnalyzer {
   
   val methodNames = Source.fromFile("methodNames.txt").getLines().toList   //one name per line  
   val unsafeFullyQualified = "sun.misc.Unsafe"
   val unsafeSimple = "Unsafe"
     
   def mergeIds(ids: Seq[IdentifierNode]) = {
     ids.map { _.name }.mkString(".")
   }
   
   def isUnsafeCandidate(qId: QualifiedIdentifierNode) = {
      val merged = mergeIds(qId.identifiers)
      merged == unsafeFullyQualified || 
      merged.toLowerCase() == "unsafe" //checks sun.misc.Unsafe.<method> | Unsafe.<method> | unsafe.<method> | UNSAFE.<method>
   }
   
   def hasType(visitor: CandidatesVisitor) = {
     val refTypes = visitor.referenceTypes.exists { refType => refType.name == unsafeFullyQualified || refType.simpleName ==  unsafeSimple }
     
     val qualifiedId = visitor.qualifiedIds.exists { qId => 
       val idString = mergeIds(qId.identifiers)
       val isFullyQualified = idString == unsafeFullyQualified
       val isSimpleType = idString == unsafeSimple
       val (withoutLastId,_) = qId.splitLastIdentifier()
       val isMethodQualifiedType = isUnsafeCandidate(withoutLastId.getOrElse(qId))
       
       isFullyQualified || isSimpleType || isMethodQualifiedType
     }
     
     val literals = visitor.stringLiterals.exists { literal => 
       val value = clean(literal) 
         value == "theUnsafe" || //used in Unsafe.class.getDeclaredField for new instances via reflection on HotSpotVM
         value == unsafeFullyQualified //used to create new instances Unsafe via reflection
       } 

     val invocations = visitor.invocations.exists { inv =>  
       inv.qualifiedIdentifier match{
         case Some(qId) => isUnsafeCandidate(qId)
         case _ => false
       }
     }
     
     refTypes || qualifiedId || literals || invocations
   }
   
   def clean(node: StringLiteralNode) = {
     node.valueRep.drop(1).dropRight(1) //removes " 
   }
   
   def isUnsafeMethodName(name: String) = {
       if(methodNames contains name) Some(name)
       else None
   }
   
   
   def getMethods(visitor: CandidatesVisitor) = {
     val qIds = visitor.qualifiedIds.flatMap {qId => 
       val mName = qId.identifiers.last.name
       isUnsafeMethodName(mName)
     }
     val ids = visitor.ids.flatMap{ id => isUnsafeMethodName(id.name) } 
     val literals = visitor.stringLiterals.flatMap { literal => isUnsafeMethodName(clean(literal)) }
     val invocations = visitor.invocations.flatMap { invocation => isUnsafeMethodName(invocation.identifier.name) } 
       
     (qIds ++ ids ++ literals ++ invocations).toSet
   }
   
  
   def units2Nodes(units: Seq[InformationUnit]) = {
     units.map{ unit =>
       unit match{
         case text: TextUnit => text.codeFragments
         case _ => unit.astNode
       }
     }
   }
   
   def post2Nodes(post: StackOverflowPost) = {
     post match{
       case question: StackOverflowQuestion =>
         units2Nodes(question.allUnits)
       case _ => units2Nodes(post.allUnits)
     }
   }
   
   
   def visitNodes(nodes: Seq[ASTNode]) = {
     val visitors = nodes.map { node => node.accept(CandidatesVisitor()) }
     val merged = visitors.reduce((v1,v2) => v1 merge v2)
     val _hasType = hasType(merged)
     val methods = getMethods(merged)
     (_hasType, methods)
   }
   
   
   def hasUnsafeWord(post: StackOverflowPost) = {
     val units = post.allUnits
       units.exists { unit => {
           val tokens = unit.rawText.toLowerCase().split("\\s").filterNot(_.isEmpty)
           tokens exists (t => t startsWith "unsaf")
         }
     }     
   }
   
   
   def analyze(artifact: StackOverflowArtifact, post: StackOverflowPost) = {
     val units = post.allUnits
     val nodes = post2Nodes(post)
     val (hasType,methods) = visitNodes(nodes)
     val unitHasUnsafeWord = hasUnsafeWord(post)
     val methodsUsage = methods.toSeq.map{ method => 
       UnsafeUsage(
           artifact,
           Some(method),
           post,
           hasType,
           unitHasUnsafeWord)
     }
     
     methodsUsage match{
       case Seq() => Seq(UnsafeUsage(artifact,None,post,hasType,unitHasUnsafeWord))
       case _ => methodsUsage
     }
   }
   
   def analyze(artifact: StackOverflowArtifact) = {
     val units = artifact.units
     val question = artifact.question
     val nodes = units.flatMap { unit => unit match {
        case textUnit: TextUnit =>  Seq(textUnit.astNode, textUnit.codeFragments) 
        case structuredUnit: StructuredFragmentUnit => Seq(structuredUnit.astNode)
      }
     }
     
     val allNodes = nodes
     
     val visitors = allNodes.map { node => node.accept(CandidatesVisitor()) }
     val merged =  {
       val temp = visitors.reduce((v1,v2) => v1 merge v2)
       val traceIds = temp.traceLines.map { e => e.methodName }
       CandidatesVisitor(temp.ids,temp.qualifiedIds -- traceIds, temp.referenceTypes, temp.stringLiterals, temp.invocations, temp.traceLines, temp.members)
     }
   
     val _hasType = hasType(merged) || units.exists { unit => unit.rawText.contains(unsafeFullyQualified) }
          
     Result(artifact, getMethods(merged), _hasType)
   }
}