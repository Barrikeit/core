package dev.barrikeit.data.mapper;

import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.data.entity.BaseEntity;
import org.mapstruct.MappingTarget;

public interface BaseMapper<E extends BaseEntity, D extends BaseDto> {
  D toDto(E source);

  E toEntity(D source);

  void updateEntity(D source, @MappingTarget E target);
}
