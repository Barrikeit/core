package dev.barrikeit.data.repository;

import dev.barrikeit.data.entity.GenericEntity;
import java.io.Serializable;
import javax.sql.DataSource;

public abstract class GenericRepository<E extends GenericEntity<I>, I extends Serializable>
    extends BaseRepository<E, I> {

  protected GenericRepository(DataSource dataSource) {
    super(dataSource);
  }
}
