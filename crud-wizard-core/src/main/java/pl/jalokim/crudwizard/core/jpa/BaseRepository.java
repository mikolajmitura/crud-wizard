package pl.jalokim.crudwizard.core.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;

@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Long> {

    default T findExactlyOneById(Long id) {
        return findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException(String.format("Could not find entity for id: %s", id)));
    }
}
