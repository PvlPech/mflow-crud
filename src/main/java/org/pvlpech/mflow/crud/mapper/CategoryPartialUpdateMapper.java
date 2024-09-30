package org.pvlpech.mflow.crud.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.pvlpech.mflow.crud.model.Category;

/**
 * Mapper to map <code><strong>non-null</strong></code> fields on an input {@link Category} onto a target {@link Category}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI
        , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryPartialUpdateMapper {

    /**
     * Maps all <code><strong>non-null</strong></code> fields from {@code input} onto {@code target}.
     * @param input The input {@link Category}
     * @param target The target {@link Category}
     */
    void mapPartialUpdate(Category input, @MappingTarget Category target);

}
