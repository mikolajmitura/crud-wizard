package pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query

import pl.jalokim.crudwizard.genericapp.datastorage.query.AbstractExpression
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel

class DataStorageQuerySamples {

    static DataStorageQuery createDsQuery(ClassMetaModel selectFromClassModel, AbstractExpression whereExpression = null) {
        DataStorageQuery.builder()
            .selectFrom(selectFromClassModel)
            .where(whereExpression)
            .build()
    }
}
