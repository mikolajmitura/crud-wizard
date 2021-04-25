package pl.jalokim.crudwizard.test.utils.cleaner;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DummyRepository extends JpaRepository<DummyEntity, Long> {

}
