package pl.jalokim.crudwizard.genericapp.datastorage.query;

import lombok.Value;

@Value(staticConstructor = "newPath")
public class WherePath {
    String pathValue;
}
