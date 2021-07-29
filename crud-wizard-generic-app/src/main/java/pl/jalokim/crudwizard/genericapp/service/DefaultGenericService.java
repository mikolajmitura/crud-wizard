package pl.jalokim.crudwizard.genericapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;
import pl.jalokim.crudwizard.genericapp.config.GenericService;

@Service
@RequiredArgsConstructor
@GenericService
public class DefaultGenericService {

    @GenericMethod
    public ResponseEntity<Object> saveOrReadFromDataStorages(GenericServiceArgument genericServiceArgument) {
        // TODO save or read from data storages
        // when save to ds then get object map by mapper and save
        // when read then map by mapper and return
        return null;
    }
}
