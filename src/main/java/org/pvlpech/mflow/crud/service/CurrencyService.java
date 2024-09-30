package org.pvlpech.mflow.crud.service;

import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.pvlpech.mflow.crud.model.Currency;

import java.util.List;

@ApplicationScoped
public class CurrencyService {

    @CacheResult(cacheName = "all-currencies-cache")
    @WithTransaction
    public Uni<List<Currency>> getAll() {
        return Currency.listAll();
    }

    @CacheResult(cacheName = "currency-cache")
    @WithTransaction
    public Uni<Currency> get(Long id) {
        return Currency.findById(id);
    }

}
