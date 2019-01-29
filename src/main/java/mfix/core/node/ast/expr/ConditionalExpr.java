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
public class ConditionalExpr extends Expr {

    private static final long serialVersionUID = -6125079576530376280L;
    private Expr _condition = null;
    private Expr _first = null;
    private Expr _snd = null;

    /**
     * ConditionalExpression:
     *      Expression ? Expression : Expression
     */
    public ConditionalExpr(String fileName, int startLine, int endLine, ASTNode node) {
        super(fileName, startLine, endLine, node);
        _nodeType = TYPE.CONDEXPR;
    }

    public void setCondition(Expr condition) {
        _condition = condition;
    }

    public void setFirst(Expr first) {
        _first = first;
    }

    public void setSecond(Expr snd) {
        _snd = snd;
    }

    public Expr getCondition() {
        return _condition;
    }

    public Expr getfirst() {
        return _first;
    }

    public Expr getSecond() {
        return _snd;
    }

    @Override
    public StringBuffer toSrcString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(_condition.toSrcString());
        stringBuffer.append("?");
        stringBuffer.append(_first.toSrcString());
        stringBuffer.append(":");
        stringBuffer.append(_snd.toSrcString());
        return stringBuffer;
    }

    @Override
    protected void tokenize() {
        _tokens = new LinkedList<>();
        _tokens.addAll(_condition.tokens());
        _tokens.add("?");
        _tokens.addAll(_first.tokens());
        _tokens.add(":");
        _tokens.addAll(_snd.tokens());
    }

    @Override
    public boolean compare(Node other) {
        boolean match = false;
        if (other instanceof ConditionalExpr) {
            ConditionalExpr conditionalExpr = (ConditionalExpr) other;
            match = _condition.compare(conditionalExpr._condition) && _first.compare(conditionalExpr._first)
                    && _snd.compare(conditionalExpr._snd);
        }
        return match;
    }

    @Override
    public List<Node> getAllChildren() {
        List<Node> children = new ArrayList<>(3);
        children.add(_condition);
        children.add(_first);
        children.add(_snd);
        return children;
    }

    @Override
    public void computeFeatureVector() {
        _fVector = new FVector();
        _fVector.inc(FVector.E_COND);
        _fVector.combineFeature(_condition.getFeatureVector());
        _fVector.combineFeature(_first.getFeatureVector());
        _fVector.combineFeature(_snd.getFeatureVector());
    }

    @Override
    public boolean postAccurateMatch(Node node) {
        boolean match = false;
        ConditionalExpr conditionalExpr = null;
        if (getBindingNode() != null) {
            conditionalExpr = (ConditionalExpr) getBindingNode();
            match = (conditionalExpr == node);
        } else if (canBinding(node)) {
            conditionalExpr = (ConditionalExpr) node;
            setBindingNode(node);
            match = true;
        }
        if (conditionalExpr == null) {
            continueTopDownMatchNull();
        } else {
            _condition.postAccurateMatch(conditionalExpr.getCondition());
            _first.postAccurateMatch(conditionalExpr.getfirst());
            _snd.postAccurateMatch(conditionalExpr.getSecond());
        }
        return match;
    }

    @Override
    public boolean genModidications() {
        if (super.genModidications()) {
            ConditionalExpr conditionalExpr = (ConditionalExpr) getBindingNode();
            if (_condition.getBindingNode() != conditionalExpr.getCondition()) {
                Update update = new Update(this, _condition, conditionalExpr.getCondition());
                _modifications.add(update);
            } else {
                _condition.genModidications();
            }

            if (_first.getBindingNode() != conditionalExpr.getfirst()) {
                Update update = new Update(this, _first, conditionalExpr.getfirst());
                _modifications.add(update);
            } else {
                _first.genModidications();
            }

            if (_snd.getBindingNode() != conditionalExpr.getSecond()) {
                Update update = new Update(this, _snd, conditionalExpr.getSecond());
                _modifications.add(update);
            } else {
                _snd.genModidications();
            }
        }
        return true;
    }

    @Override
    public StringBuffer transfer(Set<String> vars) {
        StringBuffer stringBuffer = super.transfer(vars);
        if (stringBuffer == null) {
            stringBuffer = new StringBuffer();
            StringBuffer tmp;
            tmp = _condition.transfer(vars);
            if (tmp == null) return null;
            stringBuffer.append(tmp);
            stringBuffer.append("?");
            tmp = _first.transfer(vars);
            if (tmp == null) return null;
            stringBuffer.append(tmp);
            stringBuffer.append(":");
            tmp = _snd.transfer(vars);
            if (tmp == null) return null;
            stringBuffer.append(tmp);
        }
        return stringBuffer;
    }

    @Override
    public StringBuffer adaptModifications(Set<String> vars) {
        StringBuffer condition = null;
        StringBuffer first = null;
        StringBuffer snd = null;
        Node node = checkModification();
        if (node != null) {
            ConditionalExpr conditionalExpr = (ConditionalExpr) node;
            for (Modification modification : conditionalExpr.getModifications()) {
                if (modification instanceof Update) {
                    Update update = (Update) modification;
                    Node changedNode = update.getSrcNode();
                    if (changedNode == conditionalExpr._condition) {
                        condition = update.apply(vars);
                        if (condition == null) return null;
                    } else if (changedNode == conditionalExpr._first) {
                        first = update.apply(vars);
                        if (first == null) return null;
                    } else if (changedNode == conditionalExpr._snd) {
                        snd = update.apply(vars);
                        if (snd == null) return null;
                    }
                } else {
                    LevelLogger.error("@ConditionalExpr Should not be this kind of modification : " + modification);
                }
            }
        }

        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer tmp;
        if(condition == null) {
            tmp = _condition.adaptModifications(vars);
            if(tmp == null) return null;
            stringBuffer.append(tmp);
        } else {
            stringBuffer.append(condition);
        }
        stringBuffer.append("?");
        if(first == null) {
            tmp = _first.adaptModifications(vars);
            if(tmp == null) return null;
            stringBuffer.append(tmp);
        } else {
            stringBuffer.append(first);
        }
        stringBuffer.append(":");
        if(snd == null) {
            tmp = _snd.adaptModifications(vars);
            if(tmp == null) return null;
            stringBuffer.append(tmp);
        } else {
            stringBuffer.append(snd);
        }
        return stringBuffer;
    }
}