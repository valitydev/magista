package dev.vality.magista.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.damsel.domain.*;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.serializer.kit.json.JsonHandler;
import dev.vality.geck.serializer.kit.json.JsonProcessor;
import dev.vality.geck.serializer.kit.tbase.TBaseHandler;
import dev.vality.geck.serializer.kit.tbase.TBaseProcessor;
import dev.vality.geck.serializer.kit.tbase.TErrorUtil;
import dev.vality.magista.domain.enums.FailureClass;
import org.apache.thrift.TBase;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DamselUtil {

    public static LocalDateTime getAdjustmentStatusCreatedAt(InvoicePaymentAdjustmentStatus adjustmentStatus) {
        return switch (adjustmentStatus.getSetField()) {
            case CAPTURED -> TypeUtil.stringToLocalDateTime(adjustmentStatus.getCaptured().getAt());
            case CANCELLED -> TypeUtil.stringToLocalDateTime(adjustmentStatus.getCancelled().getAt());
            default -> null;
        };
    }

    public static String getInvoiceStatusDetails(dev.vality.damsel.domain.InvoiceStatus invoiceStatus) {
        return switch (invoiceStatus.getSetField()) {
            case FULFILLED -> invoiceStatus.getFulfilled().getDetails();
            case CANCELLED -> invoiceStatus.getCancelled().getDetails();
            default -> null;
        };
    }

    public static String toJsonString(TBase tBase) {
        return toJson(tBase).toString();
    }

    public static JsonNode toJson(TBase tBase) {
        try {
            return new TBaseProcessor().process(tBase, new JsonHandler());
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static <T extends TBase> T fromJson(String jsonString, Class<T> type) {
        try {
            return new JsonProcessor().process(new ObjectMapper().readTree(jsonString), new TBaseHandler<>(type));
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static long getAmount(
            List<FinalCashFlowPosting> finalCashFlowPostings,
            Predicate<FinalCashFlowPosting> predicate) {
        return finalCashFlowPostings.stream()
                .filter(predicate)
                .mapToLong(posting -> posting.getVolume().getAmount())
                .sum();
    }


    public static Map<FeeType, Long> getFees(List<FinalCashFlowPosting> finalCashFlowPostings) {
        return finalCashFlowPostings.stream()
                .collect(
                        Collectors.groupingBy(
                                DamselUtil::getFeeType,
                                Collectors.summingLong(posting -> posting.getVolume().getAmount())
                        )
                );
    }

    public static FeeType getFeeType(FinalCashFlowPosting cashFlowPosting) {
        CashFlowAccount source = cashFlowPosting.getSource().getAccountType();
        CashFlowAccount destination = cashFlowPosting.getDestination().getAccountType();

        if (source.isSetProvider() && source.getProvider() == ProviderCashFlowAccount.settlement
                && destination.isSetMerchant() && destination.getMerchant() == MerchantCashFlowAccount.settlement) {
            return FeeType.AMOUNT;
        }

        if (source.isSetMerchant()
                && source.getMerchant() == MerchantCashFlowAccount.settlement
                && destination.isSetSystem()) {
            return FeeType.FEE;
        }

        if (source.isSetSystem()
                && destination.isSetExternal()) {
            return FeeType.EXTERNAL_FEE;
        }

        if (source.isSetSystem()
                && destination.isSetProvider()) {
            return FeeType.PROVIDER_FEE;
        }

        return FeeType.UNKNOWN;
    }

    public static OperationFailure toOperationFailure(
            FailureClass failureClass, String failure,
            String failureDescription) {
        return switch (failureClass) {
            case operation_timeout -> OperationFailure.operation_timeout(new OperationTimeout());
            case failure -> OperationFailure.failure(TErrorUtil.toGeneral(failure).setReason(failureDescription));
        };
    }
}
