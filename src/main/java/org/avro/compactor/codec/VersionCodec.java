package org.avro.compactor.codec;

import net.magik6k.bitbuffer.BitBuffer;

/**
 * Encodes and decodes the compactor version
 *    - The version is stored in 4 bits
 *
 *    TODO: Add feature flags to enabled higher compaction
 */
public class VersionCodec implements BitCodec<Byte> {

    private static final int VERSION_BITS = 4;
    private static final int MAX_VERSION = (int) Math.pow(2, VERSION_BITS);

    public VersionCodec() {}

    public void validate(final Byte version) {
        if(version > MAX_VERSION)
            throw new IllegalArgumentException("Version can not be larger than " + MAX_VERSION + ". Found " + version);
    }

    public int sizeOf(final Byte version) {
        validate(version);
        return VERSION_BITS;
    }

    public void encode(final Byte version, BitBuffer buffer) {
        validate(version);
        buffer.putByte(version, VERSION_BITS);
    }

    public Byte decode(BitBuffer buffer) {
        return buffer.getByteUnsigned(VERSION_BITS);
    }

}
