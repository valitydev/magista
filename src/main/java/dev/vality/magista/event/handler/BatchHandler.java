package dev.vality.magista.event.handler;

import dev.vality.magista.event.Processor;
import dev.vality.magista.event.mapper.Mapper;

import java.util.List;
import java.util.Map;

public interface BatchHandler<C, P> {

    default boolean accept(C change) {
        return getMappers().stream().anyMatch(mapper -> mapper.accept(change));
    }

    Processor handle(List<Map.Entry<C, P>> changes);

    List<? extends Mapper> getMappers();

}
