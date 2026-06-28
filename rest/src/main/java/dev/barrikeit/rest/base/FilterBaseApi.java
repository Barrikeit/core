package dev.barrikeit.rest.base;

import dev.barrikeit.rest.Response;

import dev.barrikeit.data.dto.BaseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.Serializable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Generic Filter", description = "Generic filter/search operations shared by resources")
public interface FilterBaseApi<I extends Serializable, D extends BaseDto> {

  @Operation(summary = "Search resources with pagination and optional search text")
  @ApiResponse(
      responseCode = "200",
      description = "Page of resources",
      content = @Content(schema = @Schema(implementation = Page.class)))
  @GetMapping
  Response<Page<D>> findAllFiltered(
      @Parameter(hidden = true) @PageableDefault(size = 20) Pageable page,
      @Parameter(description = "Search query", required = false)
          @RequestParam(required = false, defaultValue = "")
          String search);
}
