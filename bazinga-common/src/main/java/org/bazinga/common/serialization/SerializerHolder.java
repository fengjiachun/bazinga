package org.bazinga.common.serialization;

import org.bazinga.common.spi.BazingaServiceLoader;


public final class SerializerHolder {

    // SPI
    private static final Serializer serializer = BazingaServiceLoader.load(Serializer.class);

    public static Serializer serializerImpl() {
        return serializer;
    }
}
