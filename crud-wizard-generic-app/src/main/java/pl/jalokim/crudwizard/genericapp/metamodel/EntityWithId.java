package pl.jalokim.crudwizard.genericapp.metamodel;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class EntityWithId<I> {

    public abstract I getId();
}
