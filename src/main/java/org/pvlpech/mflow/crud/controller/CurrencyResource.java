package org.pvlpech.mflow.crud.controller;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.pvlpech.mflow.crud.model.Currency;
import org.pvlpech.mflow.crud.service.CurrencyService;

import java.util.List;

@Path("currencies")
@ApplicationScoped
@Produces("application/json")
public class CurrencyResource {

    @Inject
    CurrencyService currencyService;

    @GET
    public Uni<List<Currency>> get() {
        return currencyService.getAllCurrencies();
    }

    @GET
    @Path("{id}")
    public Uni<Currency> getSingle(Long id) {
        return currencyService.getCurrency(id);
    }

}
