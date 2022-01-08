package pl.jalokim.crudwizard.core.datastorage.query;

import lombok.Value;

@Value(staticConstructor = "newPath")
public class WherePath {
    String pathValue;
}
