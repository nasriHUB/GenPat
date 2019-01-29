package mfix.core.node.modify;

import mfix.core.node.ast.Node;

import java.util.Set;

public class Update extends Modification {

    private Node _srcNode;
    private Node _tarNode;

    public Update(Node parent, Node srcNode, Node tarNode) {
        super(parent);
        _srcNode = srcNode;
        _tarNode= tarNode;
    }

    public Node getSrcNode() {
        return _srcNode;
    }

    public Node getTarNode() {
        return _tarNode;
    }

    public StringBuffer apply(Set<String> vars) {
        if (_tarNode == null) {
            return new StringBuffer();
        }
        return _tarNode.transfer(vars);
    }

    @Override
    public String toString() {
        return "[UPD]" + _srcNode + " TO " + _tarNode;
    }
}