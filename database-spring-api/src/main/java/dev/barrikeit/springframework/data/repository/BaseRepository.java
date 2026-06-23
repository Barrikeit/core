package dev.barrikeit.springframework.data.repository;

import dev.barrikeit.springframework.data.entity.BaseEntity;
import java.io.Serializable;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * <b>Base Repository</b>
 *
 * <p>Generic repository that provides CRUD operations, filtering and criteria query capabilities
 * for entities extending {@link BaseEntity}, using {@link
 * org.springframework.data.jpa.domain.Specification}.
 *
 * @param <E> the entity type extending {@link BaseEntity}
 * @param <I> the type of the entity's identifier (must be {@link Serializable})
 */
@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity, I extends Serializable>
    extends CrudBaseRepository<E, I>, FilterBaseRepository<E> {}
