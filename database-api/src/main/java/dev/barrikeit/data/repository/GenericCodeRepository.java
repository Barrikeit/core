package dev.barrikeit.data.repository;

import dev.barrikeit.data.entity.GenericCodeEntity;
import dev.barrikeit.exception.DatabaseException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public abstract class GenericCodeRepository<
        E extends GenericCodeEntity<I, C>, I extends Serializable, C extends Serializable>
    extends GenericRepository<E, I> {

  protected GenericCodeRepository(DataSource dataSource) {
    super(dataSource);
  }

  protected abstract void bindCode(PreparedStatement ps, int index, C code) throws SQLException;

  public Optional<E> findByCode(C code) {
    String sql = "SELECT * FROM " + tableName() + " WHERE code = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      bindCode(ps, 1, code);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return Optional.of(mapRow(rs));
      }
    } catch (SQLException e) {
      throw new DatabaseException(
          "findByCode failed for code=%s on '%s': %s", code, tableName(), e.getMessage());
    }
    return Optional.empty();
  }

  public List<E> findByCodeIn(Collection<C> codes) {
    if (codes == null || codes.isEmpty()) return List.of();

    String placeholders = "?,".repeat(codes.size());
    // remove trailing comma
    placeholders = placeholders.substring(0, placeholders.length() - 1);

    String sql = "SELECT * FROM " + tableName() + " WHERE code IN (" + placeholders + ")";
    List<E> results = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      int index = 1;
      for (C code : codes) {
        bindCode(ps, index++, code);
      }
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) results.add(mapRow(rs));
      }
    } catch (SQLException e) {
      throw new DatabaseException("findByCodeIn failed on '%s': %s", tableName(), e.getMessage());
    }
    return results;
  }
}
