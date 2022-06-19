package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

public enum ClassMetaModelDtoType {

    /**
     * used when exists already in database
     */
    BY_ID,

    /**
     * When is during creation used as reference to other ClassMetaModelDto which has type DEFINITION
     */
    BY_NAME,

    /**
     * Is provides full definition of ClassMetaModel
     */
    DEFINITION
}
