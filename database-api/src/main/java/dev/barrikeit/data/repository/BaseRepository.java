package dev.barrikeit.data.repository;

import dev.barrikeit.data.entity.BaseEntity;
import dev.barrikeit.data.entity.Persistable;
import dev.barrikeit.exception.DatabaseException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.sql.DataSource;

public abstract class BaseRepository<
    E extends BaseEntity & Persistable<I>, I extends Serializable> {

  protected final Logger log = Logger.getLogger(getClass().getName());
  protected final DataSource dataSource;

  protected BaseRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  protected abstract String tableName();

  protected abstract E mapRow(ResultSet rs) throws SQLException;

  protected abstract void assignGeneratedId(E entity, ResultSet rs) throws SQLException;

  protected abstract void bindId(PreparedStatement ps, int index, I id) throws SQLException;

  protected abstract String buildInsertSql();

  protected abstract void bindInsert(PreparedStatement ps, E entity) throws SQLException;

  protected abstract String buildUpdateSql();

  protected abstract void bindUpdate(PreparedStatement ps, E entity) throws SQLException;

  public Optional<E> findById(I id) {
    String sql = "SELECT * FROM " + tableName() + " WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      bindId(ps, 1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return Optional.of(mapRow(rs));
      }
    } catch (SQLException e) {
      throw new DatabaseException(
          "findById failed for id=%s on '%s': %s", id, tableName(), e.getMessage());
    }
    return Optional.empty();
  }

  public List<E> findAll() {
    String sql = "SELECT * FROM " + tableName();
    List<E> results = new ArrayList<>();
    try (Connection conn = dataSource.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) results.add(mapRow(rs));
    } catch (SQLException e) {
      throw new DatabaseException("findAll failed on '%s': %s", tableName(), e.getMessage());
    }
    return results;
  }

  public E save(E entity) {
    return entity.isNew() ? insert(entity) : update(entity);
  }

  public void deleteById(I id) {
    String sql = "DELETE FROM " + tableName() + " WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      bindId(ps, 1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException(
          "deleteById failed for id=%s on '%s': %s", id, tableName(), e.getMessage());
    }
  }

  public boolean existsById(I id) {
    String sql = "SELECT 1 FROM " + tableName() + " WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      bindId(ps, 1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new DatabaseException(
          "existsById failed for id=%s on '%s': %s", id, tableName(), e.getMessage());
    }
  }

  public long count() {
    String sql = "SELECT COUNT(*) FROM " + tableName();
    try (Connection conn = dataSource.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      if (rs.next()) return rs.getLong(1);
    } catch (SQLException e) {
      throw new DatabaseException("count failed on '%s': %s", tableName(), e.getMessage());
    }
    return 0L;
  }

  private E insert(E entity) {
    entity.onPrePersist();
    boolean clientSideId = entity.getId() != null;

    if (!clientSideId) {
      entity.initId();
      clientSideId = entity.getId() != null;
    }

    String sql = buildInsertSql();
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                sql,
                clientSideId ? Statement.NO_GENERATED_KEYS : Statement.RETURN_GENERATED_KEYS)) {
      bindInsert(ps, entity);
      ps.executeUpdate();

      if (!clientSideId) {
        try (ResultSet keys = ps.getGeneratedKeys()) {
          assignGeneratedId(entity, keys);
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException("insert failed on '%s': %s", tableName(), e.getMessage());
    }
    return entity;
  }

  private E update(E entity) {
    entity.onPreUpdate();
    String sql = buildUpdateSql();
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      bindUpdate(ps, entity);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException("update failed on '%s': %s", tableName(), e.getMessage());
    }
    return entity;
  }
}
