package org.avro.compactor.codec.field;


import net.magik6k.bitbuffer.BitBuffer;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.avro.compactor.codec.BitCodec;
import org.avro.compactor.codec.FieldCodec;

/**
 * Encodes and decodes an Avro array schema
 */
public class ArrayCodec implements BitCodec<Schema> {

    private final FieldCodec fieldCodec;

    public ArrayCodec(final FieldCodec fieldCodec) {
        this.fieldCodec = fieldCodec;
    }

    public void validate(Schema schema) {
        if (schema.getType() != Type.ARRAY)
            throw new IllegalArgumentException("Must be type " + Type.ARRAY);
        fieldCodec.validate(schema.getElementType());
    }

    public int sizeOf(Schema schema) {
        validate(schema);
        return fieldCodec.sizeOf(schema.getElementType());
    }

    public void encode(Schema schema, BitBuffer buffer) {
        validate(schema);
        fieldCodec.encode(schema.getElementType(), buffer);
    }

    public Schema decode(BitBuffer buffer) {
        final Schema elementSchema = fieldCodec.decode(buffer);
        return Schema.createArray(elementSchema);
    }
}


