package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapperCompiledCodeMetadataRepository extends JpaRepository<MapperCompiledCodeMetadataEntity, Long> {

}
