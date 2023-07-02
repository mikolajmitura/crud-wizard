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
     * When is during creation used as reference to other MetaModelDto which has type DEFINITION and className provided
     * WARNING! Only supported when exists one definition with given class name in temporary context which doesn't
     * have generic types (field: genericTypes), extended types (field: extendsFromModels), additional validators (field: validators)
     */
    BY_RAW_CLASSNAME,

    /**
     * Is provides full definition of given MetaModel
     */
    DEFINITION
}
