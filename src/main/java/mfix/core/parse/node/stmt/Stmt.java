/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.parse.node.stmt;

import mfix.core.parse.node.Node;
import org.eclipse.jdt.core.dom.ASTNode;

import java.io.Serializable;

/**
 * @author: Jiajun
 * @date: 2018/9/21
 */
public abstract class Stmt extends Node implements Serializable {

	private static final long serialVersionUID = -4168850816999087148L;

	protected Stmt(String fileName, int startLine, int endLine, ASTNode node, Node parent) {
		super(fileName, startLine, endLine, node, parent);
	}
	
	@Override
	public Stmt getParentStmt() {
		return this;
	}
	
	public abstract Node bindingNode(Node patternNode);
	
}
