package ch.usi.inf.reveal.unsafe_analysis

import ch.usi.inf.reveal.parsing.model.ASTNode
import ch.usi.inf.reveal.parsing.model.java.MethodInvocationNode
import ch.usi.inf.reveal.parsing.model.java.ReferenceTypeNode
import ch.usi.inf.reveal.parsing.model.java.IdentifierNode
import ch.usi.inf.reveal.parsing.model.java.StringLiteralNode
import ch.usi.inf.reveal.parsing.model.java.QualifiedIdentifierNode
import ch.usi.inf.reveal.parsing.model.stacktraces.StackTraceLineNode
import ch.usi.inf.reveal.parsing.model.visitors.AstNodeVisitor
import ch.usi.inf.reveal.parsing.model.java.MemberAccessNode

object CandidatesVisitor {
  def apply() = new CandidatesVisitor(Set(),Set(),Set(),Set(), Set(), Set(), Set())
}

case class CandidatesVisitor(
  ids: Set[IdentifierNode],
  qualifiedIds: Set[QualifiedIdentifierNode],
  referenceTypes: Set[ReferenceTypeNode],
  stringLiterals: Set[StringLiteralNode],
  invocations: Set[MethodInvocationNode],
  traceLines: Set[StackTraceLineNode],
  members: Set[MemberAccessNode],
  val inStackTrace: Boolean = false) extends AstNodeVisitor {
  
  type MyType = CandidatesVisitor 
  
  def visit(node: ASTNode) = node match {
    case id: IdentifierNode    =>  CandidatesVisitor(ids ++ Set(id),qualifiedIds, referenceTypes, stringLiterals, invocations, traceLines, members)
    case qId: QualifiedIdentifierNode => CandidatesVisitor(ids,qualifiedIds + qId, referenceTypes, stringLiterals, invocations, traceLines, members) 
    case rt: ReferenceTypeNode =>  CandidatesVisitor(ids,qualifiedIds, referenceTypes + rt, stringLiterals, invocations, traceLines, members)  
    case sl: StringLiteralNode =>  CandidatesVisitor(ids,qualifiedIds, referenceTypes, stringLiterals + sl, invocations, traceLines, members)
    case inv: MethodInvocationNode => CandidatesVisitor(ids,qualifiedIds, referenceTypes, stringLiterals, invocations + inv, traceLines, members)
    case stLine: StackTraceLineNode => CandidatesVisitor(ids,qualifiedIds, referenceTypes, stringLiterals, invocations, traceLines + stLine, members)
    case member: MemberAccessNode => CandidatesVisitor(ids,qualifiedIds, referenceTypes, stringLiterals, invocations, traceLines, members  + member)
    case _ => this
  }
  
  def merge(vis: CandidatesVisitor) = {
    val x: CandidatesVisitor = vis
    CandidatesVisitor(ids ++ x.ids, qualifiedIds ++ x.qualifiedIds, referenceTypes ++ x.referenceTypes, stringLiterals ++ x.stringLiterals, 
        invocations ++ x.invocations, traceLines ++ x.traceLines, members ++ x.members)
  }
  
}
