/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.node.ast.expr;

import mfix.core.node.NodeUtils;
import mfix.core.node.ast.Node;
import mfix.core.node.ast.VarScope;
import mfix.core.node.match.metric.FVector;
import mfix.core.node.modify.Adaptee;
import mfix.core.node.modify.Update;
import mfix.core.pattern.cluster.NameMapping;
import mfix.core.pattern.cluster.VIndex;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StringLiteral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Jiajun
 * @date: 2018/9/21
 */
public class StrLiteral extends Expr {

	private static final long serialVersionUID = -8857803991178178009L;
	private String _value = null;
	
	/**
	 * String literal nodes.
	 */
	public StrLiteral(String fileName, int startLine, int endLine, ASTNode node) {
		super(fileName, startLine, endLine, node);
		_nodeType = TYPE.SLITERAL;
		_fIndex = VIndex.EXP_STR_LIT;
	}

	public void setValue(StringLiteral literal) {
		_value = literal.getEscapedValue();
	}

	@Deprecated
	public void setValue(String value) {
		_value = value.replace("\\", "\\\\")
				.replace("\'", "\\'")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\b", "\\b")
				.replace("\t", "\\t")
				.replace("\r", "\\r")
				.replace("\f", "\\f")
				.replace("\0", "\\0");
	}

	public String getValue() {
		return _value;
	}

	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
//		stringBuffer.append("\"");
		stringBuffer.append(_value);
//		stringBuffer.append("\"");
		return stringBuffer;
	}

	@Override
	protected StringBuffer toFormalForm0(NameMapping nameMapping, boolean parentConsidered, Set<String> keywords) {
		if (isChanged()) {
			keywords.add(toString());
			return toSrcString();
		} else if (isConsidered()){
			keywords.add("String");
			return new StringBuffer("String::").append(nameMapping.getExprID(this));
		} else {
			return null;
		}
//		return leafFormalForm(nameMapping, parentConsidered, keywords);
	}

	@Override
	protected void tokenize() {
		_tokens = new LinkedList<>();
//		_tokens.add("\"" + _value + "\"");
		String string = _value.replace(".", "\".\"");
		_tokens.addAll(Arrays.asList(string.split("\\.")));
	}

	@Override
	public boolean compare(Node other) {
		boolean match = false;
		if (other != null && other instanceof StrLiteral) {
			StrLiteral strLiteral = (StrLiteral) other;
			match = _value.equals(strLiteral._value);
		}
		return match;
	}

	@Override
	public List<Node> getAllChildren() {
		return new ArrayList<>(0);
	}

	@Override
	public void computeFeatureVector() {
		_selfFVector = new FVector();
		_selfFVector.inc(FVector.E_STR);

		_completeFVector = new FVector();
		_completeFVector.inc(FVector.E_STR);
	}

	@Override
	public boolean postAccurateMatch(Node node) {
		if (getBindingNode() == node) return false;
		if (getBindingNode() == null && canBinding(node)) {
			setBindingNode(node);
			return true;
		}
		return false;
	}

	@Override
	public boolean genModifications() {
		if (super.genModifications()) {
			StrLiteral literal = (StrLiteral) getBindingNode();
			if (!getValue().equals(literal.getValue())) {
				Update update = new Update(this, this, literal);
				_modifications.add(update);
			}
		}
		return true;
	}

	@Override
	public StringBuffer transfer(VarScope vars, Map<String, String> exprMap, String retType, Set<String> exceptions,
                                 Adaptee metric) {
		metric.inc();
		return toSrcString();
	}

	@Override
	public StringBuffer adaptModifications(VarScope vars, Map<String, String> exprMap, String retType,
                                           Set<String> exceptions, Adaptee metric) {
		Node node = NodeUtils.checkModification(this);
		if (node != null) {
			return ((Update) node.getModifications().get(0)).apply(vars, exprMap, retType, exceptions, metric);
		}
		return toSrcString();
	}
}
