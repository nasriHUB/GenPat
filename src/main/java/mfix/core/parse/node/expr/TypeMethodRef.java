/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.parse.node.expr;

import mfix.common.util.Constant;
import mfix.core.comp.Modification;
import mfix.core.parse.match.metric.FVector;
import mfix.core.parse.node.Node;
import org.eclipse.jdt.core.dom.ASTNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Jiajun
 * @date: 2018/9/21
 */
public class TypeMethodRef extends Expr implements Serializable {

	private static final long serialVersionUID = 7592430157234480970L;

	/**
	 * TypeMethodReference:
     *	Type :: 
     *	    [ < Type { , Type } > ]
     *	    Identifier
	 */
	public TypeMethodRef(String fileName, int startLine, int endLine, ASTNode node) {
		super(fileName, startLine, endLine, node);
	}
	
	@Override
	public StringBuffer toSrcString() {
		return new StringBuffer(_oriNode.toString());
	}
	
	@Override
	public StringBuffer applyChange(Map<String, String> exprMap, Set<String> allUsableVars) {
		return toSrcString();
	}
	
	@Override
	public StringBuffer replace(Map<String, String> exprMap, Set<String> allUsableVars) {
		String result = exprMap.get(toSrcString().toString());
		if(result != null) {
			return new StringBuffer(result);
		}
		return toSrcString();
	}
	
	@Override
	public StringBuffer printMatchSketch() {
		StringBuffer stringBuffer = new StringBuffer();
		if(isKeyPoint()) {
			stringBuffer.append(_oriNode.toString());
		} else {
			stringBuffer.append(Constant.PLACE_HOLDER);
		}
		return stringBuffer;
	}
	
	@Override
	protected void tokenize() {
		_tokens = new LinkedList<>();
		_tokens.add(_oriNode.toString());
	}

	@Override
	public boolean compare(Node other) {
		boolean match = false;
		if(other instanceof TypeMethodRef) {
			match = _oriNode.toString().equals(((TypeMethodRef) other)._oriNode.toString());
		}
		return match;
	}
	
	@Override
	public Map<String, Set<Node>> getCalledMethods() {
		if(_keywords == null) {
			_keywords = new HashMap<>(0);
		}
		return _keywords;
	}
	
	@Override
	public List<Node> getAllChildren() {
		return new ArrayList<>(0);
	}
	
	@Override
	public List<Modification> extractModifications() {
		return new LinkedList<>();
	}
	
	@Override
	public void deepMatch(Node other) {
		_tarNode = other;
		if(other instanceof TypeMethodRef) {
			_matchNodeType = _oriNode.toString().equals(((TypeMethodRef) other)._oriNode.toString());
		} else {
			_matchNodeType = false;
		}
	}
	
	@Override
	public boolean matchSketch(Node sketch) {
		if(sketch instanceof TypeMethodRef) {
			((TypeMethodRef) sketch)._binding = this;
			_binding = sketch;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean bindingSketch(Node sketch) {
		_binding = sketch;
		sketch.setBinding(this);
		if(sketch instanceof TypeMethodRef) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void resetAllNodeTypeMatch() {
		_matchNodeType = false;
	}

	@Override
	public void setAllNodeTypeMatch() {
		_matchNodeType = true;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new FVector();
	}
	
}
