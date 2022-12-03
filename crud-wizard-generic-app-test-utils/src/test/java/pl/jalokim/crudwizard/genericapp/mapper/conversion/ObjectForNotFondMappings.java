package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ObjectForNotFondMappings {

    private String someText;
    private Long someLong;
    private NestedNotFound someObject;
    private List<InsideCollectionElement> list;
    private List<List<InsideCollectionElement>> listOfList;

    @Data
    public static class NestedNotFound {

        private String name;
        private LocalDate localDateSome;
    }

    @Data
    public static class InsideCollectionElement {
        private Long id;
        private String uuid;
        private LocalDateTime modificationDateTime;
    }
}
