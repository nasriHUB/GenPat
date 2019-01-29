/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.node.ast.expr;

import mfix.common.util.LevelLogger;
import mfix.core.node.ast.Node;
import mfix.core.node.match.metric.FVector;
import mfix.core.node.modify.Modification;
import mfix.core.node.modify.Update;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author: Jiajun
 * @date: 2018/9/21
 */
public class PostfixExpr extends Expr {

	private static final long serialVersionUID = 3427634272372187596L;
	private Expr _expression = null;
	private PostOperator _operator = null;
	
	/**
	 * PostfixExpression:
     *	Expression PostfixOperator
	 */
	public PostfixExpr(String fileName, int startLine, int endLine, ASTNode node) {
		super(fileName, startLine, endLine, node);
		_nodeType = TYPE.POSTEXPR;
	}

	public void setExpression(Expr expression) {
		_expression = expression;
	}

	public void setOperator(PostOperator operator) {
		_operator = operator;
	}

	public Expr getExpression() {
		return _expression;
	}

	public PostOperator getOperator() {
		return _operator;
	}

	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(_expression.toSrcString());
		stringBuffer.append(_operator.toSrcString());
		return stringBuffer;
	}

	@Override
	protected void tokenize() {
		_tokens = new LinkedList<>();
		_tokens.addAll(_expression.tokens());
		_tokens.addAll(_operator.tokens());
	}

	@Override
	public boolean compare(Node other) {
		boolean match = false;
		if (other instanceof PostfixExpr) {
			PostfixExpr postfixExpr = (PostfixExpr) other;
			match = _operator.compare(postfixExpr._operator) && _expression.compare(postfixExpr._expression);
		}
		return match;
	}

	@Override
	public List<Node> getAllChildren() {
		List<Node> children = new ArrayList<>(2);
		children.add(_expression);
		children.add(_operator);
		return children;
	}

	@Override
	public void computeFeatureVector() {
		_fVector = new FVector();
		_fVector.inc(FVector.E_POSTFIX);
		_fVector.inc(_operator.toSrcString().toString());
		_fVector.combineFeature(_expression.getFeatureVector());
	}

	@Override
	public boolean postAccurateMatch(Node node) {
		PostfixExpr postfixExpr = null;
		boolean match = false;
		if (getBindingNode() != null) {
			postfixExpr = (PostfixExpr) getBindingNode();
			match = (postfixExpr == node);
		} else if (canBinding(node)) {
			postfixExpr = (PostfixExpr) node;
			setBindingNode(node);
			match = true;
		}
		if (postfixExpr == null) {
			continueTopDownMatchNull();
		} else {
			_expression.postAccurateMatch(postfixExpr.getExpression());
			_operator.postAccurateMatch(postfixExpr.getOperator());
		}
		return match;
	}

	@Override
	public boolean genModidications() {
		if (super.genModidications()) {
			PostfixExpr postfixExpr = (PostfixExpr) getBindingNode();
			if (_expression.getBindingNode() != postfixExpr.getExpression()) {
				Update update = new Update(this, _expression, postfixExpr.getExpression());
				_modifications.add(update);
			} else {
				_expression.genModidications();
			}
			if (!_operator.compare(postfixExpr.getOperator())) {
				Update update = new Update(this, _operator, postfixExpr.getOperator());
				_modifications.add(update);
			}
		}
		return true;
	}

	@Override
	public StringBuffer transfer(Set<String> vars) {
		StringBuffer stringBuffer = super.transfer(vars);
		if (stringBuffer == null) {
			stringBuffer = new StringBuffer();
			StringBuffer tmp = _expression.transfer(vars);
			if(tmp == null) return null;
			stringBuffer.append(tmp);
			tmp = _operator.transfer(vars);
			if(tmp == null) return null;
			stringBuffer.append(tmp);
		}
		return stringBuffer;
	}

	@Override
	public StringBuffer adaptModifications(Set<String> vars) {
		StringBuffer expression = null;
		StringBuffer operator = null;
		Node node = checkModification();
		if (node != null) {
			PostfixExpr postfixExpr = (PostfixExpr) node;
			for (Modification modification : postfixExpr.getModifications()) {
				if (modification instanceof Update) {
					Update update = (Update) modification;
					if (update.getSrcNode() == postfixExpr._expression) {
						expression = update.apply(vars);
						if (expression == null) return null;
					} else {
						operator = update.apply(vars);
						if (operator == null) return null;
					}
				} else {
					LevelLogger.error("@PostfixExpr Should not be this kind of modification : " + modification);
				}
			}
		}
		StringBuffer stringBuffer = new StringBuffer();
		StringBuffer tmp;
		if(expression == null) {
			tmp = _expression.adaptModifications(vars);
			if(tmp == null) return null;
			stringBuffer.append(tmp);
		} else {
			stringBuffer.append(expression);
		}
		if(operator == null) {
			tmp = _operator.adaptModifications(vars);
			if(tmp == null) return null;
			stringBuffer.append(tmp);
		} else {
			stringBuffer.append(operator);
		}
		return stringBuffer;
	}
}