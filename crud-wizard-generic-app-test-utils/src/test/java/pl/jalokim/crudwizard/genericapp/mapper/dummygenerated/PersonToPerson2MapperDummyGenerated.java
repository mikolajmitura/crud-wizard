package pl.jalokim.crudwizard.genericapp.mapper.dummygenerated;

import java.util.HashMap;
import java.util.Map;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;

public class PersonToPerson2MapperDummyGenerated implements GeneratedMapper {

    @Override
    public Object mainMap(GenericMapperArgument genericMapperArgument) {
        var document = new HashMap<String, Object>();

        ClassMetaModel sourceMetaModel = genericMapperArgument.getSourceMetaModel();
        FieldMetaModel idField = sourceMetaModel.getFieldByName("ID");
        FieldMetaModel serialNumberField = sourceMetaModel.getFieldByName("serialNumber");

        ClassMetaModel targetMetaModel = genericMapperArgument.getTargetMetaModel();
        FieldMetaModel uuidField = targetMetaModel.getFieldByName("uuid");
        FieldMetaModel numberField = targetMetaModel.getFieldByName("number");

        if (idField == null || serialNumberField == null || uuidField == null || numberField == null) {
            throw new IllegalStateException();
        }
        Map<String, Object> sourceObject = (Map<String, Object>) genericMapperArgument.getSourceObject();
        document.put("uuid", "1234-12" + sourceObject.get("ID"));
        document.put("number", "PL_" + sourceObject.get("serialNumber"));
        return document;
    }
}
