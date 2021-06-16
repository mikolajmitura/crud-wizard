package pl.jalokim.crudwizard.datastorage.inmemory.generator;

public interface IdGenerator<T> {

    T getNext();
}
