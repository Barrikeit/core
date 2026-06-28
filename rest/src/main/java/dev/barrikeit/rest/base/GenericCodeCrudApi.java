package dev.barrikeit.rest.base;

import dev.barrikeit.rest.Response;

import dev.barrikeit.data.dto.BaseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.Serializable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Generic Code CRUD", description = "Generic CRUD operations by code")
public interface GenericCodeCrudApi<C extends Serializable, D extends BaseDto> {

  @Operation(summary = "Update a resource by code")
  @ApiResponse(
      responseCode = "200",
      description = "Updated",
      content = @Content(schema = @Schema(implementation = Object.class)))
  @PutMapping("/code/{code}/update")
  Response<D> updateByCode(@PathVariable("code") C code, @RequestBody D dto);

  @Operation(summary = "Delete a resource by code")
  @ApiResponse(responseCode = "200", description = "Deleted")
  @DeleteMapping("/code/{code}")
  Response<Void> deleteByCode(@PathVariable("code") C code);
}
