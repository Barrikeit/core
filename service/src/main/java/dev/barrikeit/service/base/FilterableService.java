package dev.barrikeit.service.base;

import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.service.filter.base.BaseFilter;
import dev.barrikeit.service.filter.base.BaseFilterBuilder;
import org.springframework.data.domain.Pageable;

public interface FilterableService<D extends BaseDto, F extends BaseFilter> {
  BaseFilterBuilder<D, F> instanceFilterBuilder(Pageable page, String search);
}
