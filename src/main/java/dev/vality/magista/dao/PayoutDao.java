package dev.vality.magista.dao;

import dev.vality.magista.domain.tables.pojos.Payout;

public interface PayoutDao {

    Payout get(String payoutId);

    void save(Payout payout);

    void update(Payout payout);

}
