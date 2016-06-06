package org.bazinga.common.spi;

import java.util.ServiceLoader;

public final class BazingaServiceLoader {

    public static <S> S load(Class<S> serviceClass) {
        return ServiceLoader.load(serviceClass).iterator().next();
    }
}
