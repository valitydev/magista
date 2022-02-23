package dev.vality.magista.dao;

import java.util.Optional;

public interface EventDao {

    Optional<Long> getLastPayoutEventId();

}
