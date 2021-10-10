package pl.jalokim.crudwizard.core.metamodels;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.DOT;

import java.util.ArrayList;
import java.util.List;
import lombok.Value;

@Value
public class PropertyPath {

    public static final String ALL_INDEXES = "[*]";
    public static final String WITH_INDEX_FORMAT = "[%s]";

    String pathValue;
    String concatenationValue;
    Integer index;
    PropertyPath parent;

    public static PropertyPath createRoot() {
        return new PropertyPath(EMPTY, EMPTY, null, null);
    }

    public PropertyPath nextWithIndex(int index) {
        return new PropertyPath(String.format(WITH_INDEX_FORMAT, index), EMPTY, index, this);
    }

    public PropertyPath nextWithName(String name) {
        return new PropertyPath(name, dotWhenIsNotRoot(), null, this);
    }

    public PropertyPath nextWithAllIndexes() {
        return new PropertyPath(ALL_INDEXES, EMPTY, null, this);
    }

    private String dotWhenIsNotRoot() {
        return isRootPath() ? EMPTY : DOT;
    }

    public boolean isRootPath() {
        return this.getPathValue().equals(EMPTY) && this.getParent() == null;
    }

    public boolean isAllIndexes() {
        return isAllIndexes(pathValue);
    }

    public static boolean isAllIndexes(String pathValue) {
        return ALL_INDEXES.equals(pathValue);
    }

    public boolean isArrayElement() {
        return isArrayElement(pathValue);
    }

    public static boolean isArrayElement(String pathValue) {
        return pathValue.matches("\\[(\\d)+]");
    }

    public String buildFullPath() {
        List<String> parts = new ArrayList<>();
        PropertyPath currentPath = this;
        while (!currentPath.isRootPath()) {
            parts.add(0, currentPath.getConcatenationValue() + currentPath.getPathValue());
            currentPath = currentPath.getParent();
        }
        return elements(parts).asConcatText(EMPTY);
    }

    public PropertyPath concat(PropertyPath propertyPath) {
        List<PropertyPath> propertyPathsReversed = propertyPath.getReversedPropertyPathsParts();
        PropertyPath currentNewPath = this;
        for (PropertyPath path : propertyPathsReversed) {
            if (path.isArrayElement()) {
                currentNewPath = currentNewPath.nextWithIndex(path.getIndex());
            } else if (path.isAllIndexes()) {
                currentNewPath = currentNewPath.nextWithAllIndexes();
            } else {
                currentNewPath = currentNewPath.nextWithName(path.getPathValue());
            }
        }
        return currentNewPath;
    }

    public List<PropertyPath> getReversedPropertyPathsParts() {
        List<PropertyPath> propertyPathsReversed = new ArrayList<>();
        PropertyPath currentPath = this;
        while (!currentPath.isRootPath()) {
            propertyPathsReversed.add(0, currentPath);
            currentPath = currentPath.getParent();
        }
        return propertyPathsReversed;
    }
}
