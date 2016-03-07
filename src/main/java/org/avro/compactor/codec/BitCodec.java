package org.avro.compactor.codec;

import net.magik6k.bitbuffer.BitBuffer;

public interface BitCodec<T> {

    void validate(final T data);

    int sizeOf(final T data);

    void encode(final T data, BitBuffer buffer);

    T decode(BitBuffer buffer);

}
