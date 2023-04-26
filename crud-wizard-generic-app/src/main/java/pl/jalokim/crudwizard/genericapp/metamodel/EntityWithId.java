package pl.jalokim.crudwizard.genericapp.metamodel;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class EntityWithId<I> {

    abstract public I getId();
}
