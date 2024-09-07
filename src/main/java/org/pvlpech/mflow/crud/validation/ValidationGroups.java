package org.pvlpech.mflow.crud.validation;

import jakarta.validation.groups.Default;

// https://quarkus.io/guides/validation
public interface ValidationGroups {

    interface Post extends Default {
    }
}