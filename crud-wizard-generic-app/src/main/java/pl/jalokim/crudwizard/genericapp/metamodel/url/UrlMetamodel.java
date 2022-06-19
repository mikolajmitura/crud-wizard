package pl.jalokim.crudwizard.genericapp.metamodel.url;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Value;

@Value
public class UrlMetamodel {

    List<UrlPart> urlParts;
    String rawUrl;

    // TODO #github-38 should be provided all data storages for all url path variables.
    //  But one DS per every path variable or many DS to one Path variable???
    //  with that can be done some validation that some object under some variable exists in some DS.
    Map<String, List<DataStorageMetaForUrlModel>> dataStoragesByUrVariableName;

    public List<String> getPathVariablesNames() {
        return elements(urlParts)
            .filter(UrlPart::isPathVariable)
            .map(UrlPart::getVariableName)
            .asList();
    }

    public String getLastVariableNameInUrl() {
        return elements(urlParts)
            .filter(UrlPart::isPathVariable)
            .map(UrlPart::getVariableName)
            .getLastOrNull();
    }

    public List<DataStorageMetaForUrlModel> getDataStoragesForLastVariableName() {
        return Optional.ofNullable(getLastVariableNameInUrl())
            .map(dataStoragesByUrVariableName::get)
            .orElse(Collections.emptyList());
    }
}
