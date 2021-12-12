package pl.jalokim.crudwizard.core.datastorage.query;

import lombok.Value;

@Value
public class OrderPath {

    String path;
    OrderDirection orderDirection;

    public static OrderPath orderPathFromText(String orderPath) {
        if (orderPath.matches("(.)+\\((asc|desc)\\)")) {
            String[] pathParts = orderPath.split("\\(");
            return OrderPath.newOrder(pathParts[0], OrderDirection.valueOf(pathParts[1].replace(")", "").toUpperCase()));
        }
        return OrderPath.newOrder(orderPath);
    }

    public static OrderPath newOrder(String path) {
        return new OrderPath(path, OrderDirection.ASC);
    }

    public static OrderPath newOrder(String path, OrderDirection orderDirection) {
        return new OrderPath(path, orderDirection);
    }
}
