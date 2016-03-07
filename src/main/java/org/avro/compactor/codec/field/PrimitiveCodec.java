package org.avro.compactor.codec.field;

import net.magik6k.bitbuffer.BitBuffer;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.avro.compactor.codec.BitCodec;

public class PrimitiveCodec implements BitCodec<Schema> {

    private static final int FIELD_BITS = 0;

    private final Type type;

    public PrimitiveCodec(final Type type) {
        if (!TypeCodec.isPrimitive(type))
            throw new IllegalArgumentException("Must be a primitive type");
        this.type = type;
    }

    public void validate(final Schema schema) {
        if (schema.getType() != type)
            throw new IllegalArgumentException("Schema type should be " + type + ". Found " + schema.getType());
    }

    public int sizeOf(final Schema schema) {
        validate(schema);
        return FIELD_BITS;
    }

    public void encode(final Schema schema, BitBuffer buffer) {
        validate(schema);
        // Nothing needs to be serialized
    }

    public Schema decode(BitBuffer buffer) {
        // Nothing needs to be deserialized
        return Schema.create(type);

    }
}
