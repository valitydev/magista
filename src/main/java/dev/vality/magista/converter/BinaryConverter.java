package dev.vality.magista.converter;

public interface BinaryConverter<T> {

    T convert(byte[] bin, Class<T> clazz);

}
