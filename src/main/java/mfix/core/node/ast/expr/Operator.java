/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.node.ast.expr;

import mfix.core.node.NodeUtils;
import mfix.core.node.abs.CodeAbstraction;
import mfix.core.node.ast.Node;
import mfix.core.node.ast.VarScope;
import mfix.core.node.ast.stmt.Stmt;
import mfix.core.node.match.metric.FVector;
import mfix.core.node.modify.Adaptee;
import mfix.core.pattern.cluster.NameMapping;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Jiajun
 * @date: 2018/9/21
 */
public abstract class Operator extends Node {

	private static final long serialVersionUID = -6189851424541008969L;

	public Operator(String fileName, int startLine, int endLine, ASTNode oriNode) {
		super(fileName, startLine, endLine, oriNode);
	}

	@Override
	public Stmt getParentStmt() {
		return getParent().getParentStmt();
	}

	@Override
	public List<Stmt> getChildren() {
		return new ArrayList<>(0);
	}

	@Override
	public List<Node> getAllChildren() {
		return new ArrayList<>(0);
	}

	@Override
	public void computeFeatureVector() {
		_selfFVector = new FVector();
		_selfFVector.inc(toSrcString().toString());
		_completeFVector = new FVector();
		_completeFVector.inc(toSrcString().toString());
	}

	@Override
	public void doAbstraction(CodeAbstraction abstracter) {
		if (isChanged()) {
			_abstract = false;
		}
	}

	@Override
	public Set<Node> expand(Set<Node> nodes) {
		super.expand(nodes);
		if (isChanged()) {
			nodes.addAll(getParent().getAllChildren());
		}
		return nodes;
	}

	@Override
	public boolean postAccurateMatch(Node node) {
		if (getBindingNode() == node) return true;
		if (getBindingNode() == null && canBinding(node)
				&& toSrcString().toString().equals(node.toSrcString().toString())) {
			setBindingNode(node);
			return true;
		}
		return false;
	}

	@Override
	public boolean genModifications() {
		return true;
	}

	@Override
	public StringBuffer transfer(VarScope vars, Map<String, String> exprMap, String retType, Set<String> exceptions,
                                 Adaptee metric) {
		StringBuffer stringBuffer = super.transfer(vars, exprMap, retType, exceptions, metric);
		if (stringBuffer == null) {
			metric.inc();
			stringBuffer = toSrcString();
		}
		return stringBuffer;
	}

	@Override
	public StringBuffer adaptModifications(VarScope vars, Map<String, String> exprMap, String retType,
                                           Set<String> exceptions, Adaptee metric) {
		return toSrcString();
	}

	@Override
	protected StringBuffer toFormalForm0(NameMapping nameMapping, boolean parentConsidered, Set<String> keywords) {
		if (isChanged()) {
			keywords.add(toSrcString().toString());
			return toSrcString();
		} else if (isConsidered()) {
			return new StringBuffer(nameMapping.getOpID(this));
		} else {
			return null;
		}
	}

	@Override
	public boolean patternMatch(Node node, Map<Node, Node> matchedNode) {
		if (node == null || isConsidered() != node.isConsidered() || node.getNodeType() != getNodeType()) {
			return false;
		}
		return NodeUtils.patternMatch(this, node, matchedNode);
	}
}
