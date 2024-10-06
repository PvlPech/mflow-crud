package org.pvlpech.mflow.crud.rest;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.pvlpech.mflow.crud.model.Category;
import org.pvlpech.mflow.crud.model.Group;
import org.pvlpech.mflow.crud.model.User;
import org.pvlpech.mflow.crud.service.CategoryService;
import org.pvlpech.mflow.crud.validation.ValidationGroups;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/v1/categories")
@ApplicationScoped
@Produces(APPLICATION_JSON)
@Tag(name = "categories")
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    @GET
    @Operation(summary = "Returns all the categories from the database")
    @APIResponse(
        responseCode = "200",
        description = "Gets all categories",
        content = @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = Category.class, type = SchemaType.ARRAY),
            examples = @ExampleObject(name = "categories", value = Examples.VALID_EXAMPLE_CATEGORY_LIST)
        )
    )
    public Uni<List<Category>> getAll() {
        return categoryService.getAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Returns a category for a given identifier")
    @APIResponse(
        responseCode = "200",
        description = "Gets a category for a given id",
        content = @Content(
            mediaType = APPLICATION_JSON, schema = @Schema(implementation = Category.class),
            examples = @ExampleObject(name = "category", value = Examples.VALID_EXAMPLE_CATEGORY)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "The category is not found for a given identifier"
    )
    public Uni<Response> get(@Parameter(name = "id", required = true) @PathParam("id") Long id) {
        return categoryService.get(id)
            .onItem().ifNotNull().transform(category -> Response.ok(category).build())
            .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Operation(summary = "Creates a category")
    @APIResponse(
        responseCode = "201",
        description = "The URI of the created category",
        headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(implementation = URI.class))
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid category passed in (or no request body found)"
    )
    @APIResponse(
        responseCode = "404",
        description = "No group found"
    )
    public Uni<Response> create(
        @RequestBody(
            name = "category",
            required = true,
            content = @Content(
                mediaType = APPLICATION_JSON,
                schema = @Schema(implementation = Category.class),
                examples = @ExampleObject(name = "valid_category", value = Examples.VALID_EXAMPLE_CATEGORY_TO_CREATE)
            )
        )
        @Valid @ConvertGroup(to = ValidationGroups.Post.class) @NotNull(message = "Category must not be blank") Category category,
        @Context UriInfo uriInfo) {
        return this.categoryService.create(category)
                .onItem().ifNotNull().transform(createdCategory -> {
                    var uri = uriInfo.getAbsolutePathBuilder().path(Long.toString(createdCategory.getId())).build();
                    return Response.created(uri).build();
                })
                .onFailure(ConstraintViolationException.class)
                .transform(cve -> new ResteasyReactiveViolationException(((ConstraintViolationException) cve).getConstraintViolations()));
    }

    @PATCH
    @Path("/{id}")
    @Consumes(APPLICATION_JSON)
    @Operation(summary = "Partially updates an exiting category")
    @APIResponse(
        responseCode = "200",
        description = "Updated the category",
        content = @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = Category.class),
            examples = @ExampleObject(name = "category", value = Examples.VALID_EXAMPLE_CATEGORY)
        )
    )
    @APIResponse(
        responseCode = "400",
        description = "Null category passed in (or no request body found)"
    )
    @APIResponse(
        responseCode = "404",
        description = "No group/parent found"
    )
    public Uni<Response> update(@Parameter(name = "id", required = true) @PathParam("id") Long id,
                                @RequestBody(
                                    name = "valid_category",
                                    required = true,
                                    content = @Content(
                                        schema = @Schema(implementation = Category.class),
                                        examples = @ExampleObject(name = "valid_category", value = Examples.VALID_EXAMPLE_CATEGORY)
                                    )
                                )
                                @NotNull Category category) {
        if (category.getId() == null) {
            category.setId(id);
        }

        return this.categoryService.partialUpdate(category)
            .onItem().ifNotNull().transform(updatedCategory -> Response.ok(updatedCategory).build())
            .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND).build())
            .onFailure(ConstraintViolationException.class)
            .transform(cve -> new ResteasyReactiveViolationException(((ConstraintViolationException) cve).getConstraintViolations()));
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deletes an exiting category")
    @APIResponse(
        responseCode = "204",
        description = "Deletes a category"
    )
    @APIResponse(
        responseCode = "404",
        description = "No category found"
    )
    public Uni<Response> delete(@Parameter(name = "id", required = true) @PathParam("id") Long id) {
        return categoryService.delete(id)
                .map(unused -> Response.noContent().build());
    }
}
