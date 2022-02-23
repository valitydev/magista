package dev.vality.magista.query.impl;

import dev.vality.magista.query.BaseFunction;
import dev.vality.magista.query.BaseQueryValidator;
import dev.vality.magista.query.QueryContext;
import dev.vality.magista.query.QueryParameters;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by vpankrashkin on 08.08.16.
 */
public abstract class ScopedBaseFunction<T, CT> extends BaseFunction<T, CT> {

    public ScopedBaseFunction(Object descriptor, QueryParameters params, String name) {
        super(descriptor, params, name);

    }

    @Override
    public ScopedBaseParameters getQueryParameters() {
        return (ScopedBaseParameters) super.getQueryParameters();
    }

    @Override
    protected QueryParameters createQueryParameters(QueryParameters parameters, QueryParameters derivedParameters) {
        return new ScopedBaseParameters(parameters, derivedParameters);
    }

    protected FunctionQueryContext getContext(QueryContext context) {
        return this.getContext(context, FunctionQueryContext.class);
    }

    public static class ScopedBaseParameters extends QueryParameters {

        public ScopedBaseParameters(Map<String, Object> parameters, QueryParameters derivedParameters) {
            super(parameters, derivedParameters);
        }

        public ScopedBaseParameters(QueryParameters parameters, QueryParameters derivedParameters) {
            super(parameters, derivedParameters);
        }

        public String getMerchantId() {
            return getStringParameter(Parameters.MERCHANT_ID_PARAM, false);
        }

        public String getShopId() {
            return getStringParameter(Parameters.SHOP_ID_PARAM, false);
        }

        public List<String> getShopIds() {
            return getArrayParameter(Parameters.SHOP_IDS_PARAM, false);
        }

        public List<Integer> getShopCategoryIds() {
            return getArrayParameter(Parameters.SHOP_CATEGORY_IDS_PARAM, false);
        }

    }

    public static class ScopedBaseValidator extends BaseQueryValidator {
        @Override
        public void validateParameters(QueryParameters parameters) throws IllegalArgumentException {
            super.validateParameters(parameters);
            ScopedBaseParameters scopedParameters = super.checkParamsType(parameters, ScopedBaseParameters.class);

            if (scopedParameters.getShopId() != null && scopedParameters.getShopIds() != null) {
                checkParamsResult(true,
                        String.format("Need to specify only one parameter: %s or %s",
                                Parameters.SHOP_ID_PARAM, Parameters.SHOP_IDS_PARAM));
            }

            if (!StringUtils.hasLength(scopedParameters.getMerchantId())
                    && (StringUtils.hasLength(scopedParameters.getShopId())
                    || !CollectionUtils.isEmpty(scopedParameters.getShopIds()))) {
                checkParamsResult(true, Parameters.SHOP_ID_PARAM,
                        "when searching by shop_id/shop_ids, merchant_id must be set");
            }
        }

    }

}
