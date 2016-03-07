package org.avro.compactor.codec.field;

import net.magik6k.bitbuffer.BitBuffer;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.avro.compactor.codec.BitCodec;
import org.avro.compactor.codec.FieldCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * Encodes and decodes an Avro union schema with up to 16 types
 *    - The type count is stored in 4 bits
 *
 *    TODO: rule flags for more/less strict rules
 *      - More types
 */
public class UnionCodec implements BitCodec<Schema> {

    private static final int TYPES_COUNT_BITS = 4;
    private static final int TYPES_MAX_COUNT = (int) Math.pow(2, TYPES_COUNT_BITS);

    private final FieldCodec fieldCodec;

    public UnionCodec(final FieldCodec fieldCodec) {
        this.fieldCodec = fieldCodec;
    }

    public void validate(final Schema schema) {
        if (schema.getType() != Type.UNION)
            throw new IllegalArgumentException("Must be type " + Type.UNION);
        if (schema.getTypes().size() > TYPES_MAX_COUNT)
            throw new IllegalArgumentException("Only supports up to " + TYPES_MAX_COUNT + " types. Found " + schema.getTypes().size());
        for (Schema typeSchema : schema.getTypes()) {
            fieldCodec.validate(typeSchema);
        }
    }

    public int sizeOf(final Schema schema) {
        validate(schema);
        int size = 0;
        size += TYPES_COUNT_BITS;
        for (Schema typeSchema : schema.getTypes()) {
            size += fieldCodec.sizeOf(typeSchema);
        }
        return size;
    }

    public void encode(final Schema schema, BitBuffer buffer) {
        validate(schema);
        buffer.putByte((byte) schema.getTypes().size(), TYPES_COUNT_BITS);
        for (Schema typeSchema : schema.getTypes()) {
            fieldCodec.encode(typeSchema, buffer);
        }
    }

    public Schema decode(BitBuffer buffer) {
        final Byte typeCount = buffer.getByteUnsigned(TYPES_COUNT_BITS);
        final List<Schema> types = new ArrayList<Schema>();
        for(int i = 0; i < typeCount; i++) {
            final Schema typeSchema = fieldCodec.decode(buffer);
            types.add(typeSchema);
        }
        return Schema.createUnion(types);
    }
}


