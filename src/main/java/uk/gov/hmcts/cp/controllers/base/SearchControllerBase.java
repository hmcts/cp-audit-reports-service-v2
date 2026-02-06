package uk.gov.hmcts.cp.controllers.base;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;

public class SearchControllerBase<V, S, T, R> {

    protected final V service;

    private final Converter<S, T> converter;
    private final Function<List<T>, R> toResponse;

    protected SearchControllerBase(final V service,
                                   final Converter<S, T> converter,
                                   final Function<List<T>, R> toResponse) {

        this.service = service;
        this.converter = converter;
        this.toResponse = toResponse;
    }

    protected ResponseEntity<R> responseOk(final List<S> list) {

        return ResponseEntity.ok(toResponse.apply(list.stream().map(converter::convert).toList()));
    }
}
