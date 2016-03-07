package org.avro.compactor.codec.field;

import net.magik6k.bitbuffer.BitBuffer;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.avro.compactor.codec.BitCodec;
import org.avro.compactor.codec.FieldCodec;

/**
 * Encodes and decodes an Avro array schema
 */
public class MapCodec implements BitCodec<Schema> {

    private final FieldCodec fieldCodec;

    public MapCodec(final FieldCodec fieldCodec) {
        this.fieldCodec = fieldCodec;
    }

    public void validate(final Schema schema) {
        if (schema.getType() != Type.MAP)
            throw new IllegalArgumentException("Must be type " + Type.MAP);
        fieldCodec.validate(schema.getValueType());
    }

    public int sizeOf(final Schema schema) {
        validate(schema);
        return fieldCodec.sizeOf(schema.getValueType());
    }

    public void encode(final Schema schema, BitBuffer buffer) {
        validate(schema);
        fieldCodec.encode(schema.getValueType(), buffer);
    }

    public Schema decode(final BitBuffer buffer) {
        final Schema valueSchema = fieldCodec.decode(buffer);
        return Schema.createMap(valueSchema);
    }
}


