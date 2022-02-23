package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.InternationalBankAccount;
import dev.vality.damsel.domain.InternationalBankDetails;
import dev.vality.damsel.domain.PayoutToolInfo;
import dev.vality.damsel.domain.RussianBankAccount;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.domain.enums.PayoutStatus;
import dev.vality.magista.domain.enums.PayoutToolType;
import dev.vality.magista.event.handler.PayoutHandler;
import dev.vality.magista.service.PartyManagementService;
import dev.vality.magista.service.PayoutService;
import dev.vality.payout.manager.Event;
import dev.vality.payout.manager.Payout;
import dev.vality.payout.manager.PayoutChange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayoutCreatedHandler implements PayoutHandler {

    private final PayoutService payoutEventService;
    private final PartyManagementService partyManagementService;

    @Override
    public void handle(PayoutChange change, Event event) {
        var payout = new dev.vality.magista.domain.tables.pojos.Payout();
        payout.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payout.setPayoutId(event.getPayoutId());
        payout.setSequenceId(event.getSequenceId());

        Payout payoutSource = change.getCreated().getPayout();
        payout.setStatus(TBaseUtil.unionFieldToEnum(payoutSource.getStatus(), PayoutStatus.class));
        payout.setCreatedAt(TypeUtil.stringToLocalDateTime(payoutSource.getCreatedAt()));

        payout.setPayoutToolId(payoutSource.getPayoutToolId());
        payout.setAmount(payoutSource.getAmount());
        payout.setFee(payoutSource.getFee());
        payout.setCurrencyCode(payoutSource.getCurrency().getSymbolicCode());

        payout.setPartyId(payoutSource.getPartyId());
        payout.setShopId(payoutSource.getShopId());
        PayoutToolInfo payoutToolInfo = partyManagementService.getPayoutToolInfo(
                payoutSource.getPartyId(), payoutSource.getShopId(), payoutSource.getPayoutToolId());
        PayoutToolType payoutToolType = TBaseUtil.unionFieldToEnum(payoutToolInfo, PayoutToolType.class);
        payout.setPayoutToolType(payoutToolType);
        if (payoutToolInfo.isSetRussianBankAccount()) {
            RussianBankAccount russianBankAccount = payoutToolInfo.getRussianBankAccount();
            payout.setPayoutToolRussianBankAccountAccount(russianBankAccount.getAccount());
            payout.setPayoutToolRussianBankAccountBankBik(russianBankAccount.getBankBik());
            payout.setPayoutToolRussianBankAccountBankName(russianBankAccount.getBankName());
            payout.setPayoutToolRussianBankAccountBankPostAccount(russianBankAccount.getBankPostAccount());
        } else if (payoutToolInfo.isSetInternationalBankAccount()) {
            InternationalBankAccount internationalBankAccount = payoutToolInfo.getInternationalBankAccount();
            payout.setPayoutToolInternationalBankAccountNumber(internationalBankAccount.getNumber());
            if (internationalBankAccount.isSetBank()) {
                InternationalBankDetails bank = internationalBankAccount.getBank();
                payout.setPayoutToolInternationalBankAccountBankBic(bank.getBic());
                if (bank.getCountry() != null) {
                    payout.setPayoutToolInternationalBankAccountBankCountryCode(bank.getCountry().name());
                }
                payout.setPayoutToolInternationalBankAccountBankName(bank.getName());
                payout.setPayoutToolInternationalBankAccountBankAddress(bank.getAddress());
                payout.setPayoutToolInternationalBankAccountBankAbaRtn(bank.getAbaRtn());
            }
            if (internationalBankAccount.isSetCorrespondentAccount()) {
                payout.setPayoutToolInternationalBankAccountCorrAccount(
                        internationalBankAccount.getCorrespondentAccount().getNumber());
            }
            payout.setPayoutToolInternationalBankAccountIban(internationalBankAccount.getIban());
        } else if (payoutToolInfo.isSetWalletInfo()) {
            payout.setPayoutToolWalletId(payoutToolInfo.getWalletInfo().getWalletId());
        }

        payoutEventService.savePayout(payout);
    }

    @Override
    public boolean accept(PayoutChange change) {
        return change.isSetCreated();
    }
}
