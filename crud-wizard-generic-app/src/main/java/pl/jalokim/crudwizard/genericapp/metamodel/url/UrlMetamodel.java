package pl.jalokim.crudwizard.genericapp.metamodel.url;

import java.util.List;
import lombok.Value;

@Value
public class UrlMetamodel {

    List<UrlPart> urlParts;
    String rawUrl;
}
