package org.pvlpech.mflow.crud.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.pvlpech.mflow.crud.model.User;

/**
 * Mapper to map <code><strong>non-null</strong></code> fields on an input {@link User} onto a target {@link User}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI
        , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserPartialUpdateMapper {

    /**
     * Maps all <code><strong>non-null</strong></code> fields from {@code input} onto {@code target}.
     * @param input The input {@link User}
     * @param target The target {@link User}
     */
    void mapPartialUpdate(User input, @MappingTarget User target);

}
