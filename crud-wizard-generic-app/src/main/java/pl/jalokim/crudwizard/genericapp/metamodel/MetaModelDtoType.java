package pl.jalokim.crudwizard.genericapp.metamodel;

public enum MetaModelDtoType {

    /**
     * used when exists already in database
     */
    BY_ID,

    /**
     * When is during creation used as reference to other MetaModelDto which has type DEFINITION
     */
    BY_NAME,

    /**
     * Is provides full definition of given MetaModel
     */
    DEFINITION
}
