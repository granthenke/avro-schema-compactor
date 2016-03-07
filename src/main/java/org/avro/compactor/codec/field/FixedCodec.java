package org.avro.compactor.codec.field;

import net.magik6k.bitbuffer.BitBuffer;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.avro.compactor.codec.BitCodec;
import org.avro.compactor.codec.NameCodec;

/**
 * Encodes and decodes an Avro fixed schema with a fixed size up to 256
 *
 * TODO: rule flags for more/less strict rules
 */
public class FixedCodec implements BitCodec<Schema> {

    private static final int FIXED_SIZE_BITS = 8;
    private static final int MAX_FIXED_SIZE = (int) Math.pow(2, FIXED_SIZE_BITS);

    private final NameCodec nameCodec;

    public FixedCodec(final NameCodec nameCodec) {
        this.nameCodec = nameCodec;
    }

    public void validate(Schema schema) {
        if (schema.getType() != Type.FIXED)
            throw new IllegalArgumentException("Must be type " + Type.FIXED);
        nameCodec.validate(schema.getName());
        if(schema.getFixedSize() > MAX_FIXED_SIZE)
            throw new IllegalArgumentException("Only supports a fixed size up to " + MAX_FIXED_SIZE + ". Found " + schema.getFixedSize());
    }

    public int sizeOf(Schema schema) {
        validate(schema);
        int size = 0;
        size += nameCodec.sizeOf(schema.getName());
        size += FIXED_SIZE_BITS;
        return size;
    }

    public void encode(Schema schema, BitBuffer buffer) {
        validate(schema);
        nameCodec.encode(schema.getName(), buffer);
        buffer.putByte((byte) schema.getFixedSize(), FIXED_SIZE_BITS);
    }

    public Schema decode(BitBuffer buffer) {
        final String name = nameCodec.decode(buffer);
        final Byte size = buffer.getByteUnsigned(FIXED_SIZE_BITS);
        return Schema.createFixed(name, null, null, size);
    }
}


