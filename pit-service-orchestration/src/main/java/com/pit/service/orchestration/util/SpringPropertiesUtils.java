package com.pit.service.orchestration.util;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

/**
 * @author gy
 */
@Component
public class SpringPropertiesUtils implements EmbeddedValueResolverAware {
    private static StringValueResolver valueResolver;

    public static String getPropertiesValue(String name) {
        return valueResolver.resolveStringValue(name);
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
        SpringPropertiesUtils.valueResolver = stringValueResolver;
    }

}