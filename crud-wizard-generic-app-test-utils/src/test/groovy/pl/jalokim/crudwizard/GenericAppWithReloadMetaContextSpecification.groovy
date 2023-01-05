package pl.jalokim.crudwizard

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto
import pl.jalokim.crudwizard.genericapp.reload.MetaContextTestLoader

class GenericAppWithReloadMetaContextSpecification extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private MetaContextTestLoader metaContextTestLoader

    @Autowired
    protected MetaModelContextService metaModelContextService

    def setup() {
        metaContextTestLoader.reload()
    }

    ClassMetaModel findClassMetaModelByName(String name) {
        metaModelContextService.getMetaModelContext().getClassMetaModels()
            .fetchAll().find {
            it.name == name
        }
    }

    ApiTagDto createApiTagDtoByName(String tagName) {
        def tagMetaModel = metaModelContextService.getMetaModelContext().getApiTags()
            .fetchAll().find {
            it.name == tagName
        }
        ApiTagDto.builder()
            .id(tagMetaModel.id)
            .build()
    }

    DataStorageMetaModelDto findDataStorageMetaModelDtoByClass(Class<?> dataStorageClass) {
        def foundDsMetaModel = metaModelContextService.getMetaModelContext().getDataStorages()
            .fetchAll().find {
            it.dataStorage.class == dataStorageClass
        }
        DataStorageMetaModelDto.builder()
            .id(foundDsMetaModel.id)
            .build()
    }
}
