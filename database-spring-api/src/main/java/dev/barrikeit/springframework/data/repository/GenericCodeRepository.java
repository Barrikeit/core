package dev.barrikeit.springframework.data.repository;

import dev.barrikeit.springframework.data.entity.GenericCodeEntity;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * <b>Generic Code Repository Interface</b>
 *
 * <p>This interface serves as a base repository for generic entities with code, extending from
 * GenericRepository. It provides standard data access methods for any entity type that extends
 * {@link GenericCodeEntity}.
 *
 * @param <E> the entity type that extends {@link GenericCodeEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <C> the type of the entity's code, which must be {@link Serializable}.
 */
@NoRepositoryBean
public interface GenericCodeRepository<
        E extends GenericCodeEntity<I, C>, I extends Serializable, C extends Serializable>
    extends GenericRepository<E, I> {

  Optional<E> findByCode(C code);

  List<E> findByCodeIn(List<C> codes);
}
