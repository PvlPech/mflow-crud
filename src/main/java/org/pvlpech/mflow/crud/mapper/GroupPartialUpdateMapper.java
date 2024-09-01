package org.pvlpech.mflow.crud.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.pvlpech.mflow.crud.model.Group;

/**
 * Mapper to map <code><strong>non-null</strong></code> fields on an input {@link Group} onto a target {@link Group}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI
        , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GroupPartialUpdateMapper {

    /**
     * Maps all <code><strong>non-null</strong></code> fields from {@code input} onto {@code target}.
     * @param input The input {@link Group}
     * @param target The target {@link Group}
     */
    void mapPartialUpdate(Group input, @MappingTarget Group target);

}
