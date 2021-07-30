package pl.jalokim.crudwizard.genericapp.service.translator;

import static pl.jalokim.utils.constants.Constants.DOT;
import static pl.jalokim.utils.constants.Constants.EMPTY;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import pl.jalokim.utils.collection.Elements;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectNodePath {

    String nodeName;
    String concatValue;
    ObjectNodePath parent;

    public static ObjectNodePath rootNode() {
        return new ObjectNodePath(EMPTY, EMPTY, null);
    }

    public ObjectNodePath nextNode(String nodeName) {
        return new ObjectNodePath(nodeName, parent == null ? EMPTY : DOT, this);
    }

    public ObjectNodePath nextCollectionNode(int index) {
        return new ObjectNodePath(wrapAsNodeName(index), EMPTY, this);
    }

    private static String wrapAsNodeName(int index) {
        return String.format("[%s]", index);
    }

    public String getFullPath() {
        List<String> nodes = new ArrayList<>();

        var currentNode = this;
        while (currentNode != null) {
            nodes.add(0, currentNode.getConcatValue() + currentNode.getNodeName());
            currentNode = currentNode.getParent();
        }

        return Elements.elements(nodes)
            .asConcatText(EMPTY);
    }
}
