package org.avro.compactor.codec;

import net.magik6k.bitbuffer.BitBuffer;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.avro.compactor.codec.field.ArrayCodec;
import org.avro.compactor.codec.field.EnumCodec;
import org.avro.compactor.codec.field.FixedCodec;
import org.avro.compactor.codec.field.MapCodec;
import org.avro.compactor.codec.field.TypeCodec;
import org.avro.compactor.codec.field.PrimitiveCodec;
import org.avro.compactor.codec.field.RecordCodec;
import org.avro.compactor.codec.field.UnionCodec;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Support LogicalType
 */
public class FieldCodec implements BitCodec<Schema> {

    private final Map<Type, BitCodec<Schema>> fieldCodecs = new HashMap<Type, BitCodec<Schema>>();

    public FieldCodec(final NameCodec nameCodec, final NameCodec symbolCodec) {
        // Primitive
        for (Type type : TypeCodec.primitiveTypes) {
            fieldCodecs.put(type, new PrimitiveCodec(type));
        }

        // Complex
        fieldCodecs.put(Type.RECORD, new RecordCodec(nameCodec, this));
        fieldCodecs.put(Type.UNION, new UnionCodec(this));
        fieldCodecs.put(Type.ARRAY, new ArrayCodec(this));
        fieldCodecs.put(Type.MAP, new MapCodec(this));
        fieldCodecs.put(Type.ENUM, new EnumCodec(nameCodec, symbolCodec));
        fieldCodecs.put(Type.FIXED, new FixedCodec(nameCodec));
    }

    public void validate(final Schema schema) {
        final Type type = schema.getType();
        TypeCodec.validate(type);
        if(fieldCodecs.containsKey(type)) {
            fieldCodecs.get(type).validate(schema);
        } else {
            throw new UnsupportedOperationException("Fields of type " + type + " are not supported");
        }
    }

    public int sizeOf(final Schema schema) {
        validate(schema);
        int size = 0;
        final Type type = schema.getType();
        size += TypeCodec.sizeOf(type);
        size += fieldCodecs.get(type).sizeOf(schema);
        return size;
    }

    public void encode(final Schema schema, BitBuffer buffer) {
        validate(schema);
        final Type type = schema.getType();
        TypeCodec.encode(type, buffer);
        fieldCodecs.get(type).encode(schema, buffer);
    }

    public Schema decode(BitBuffer buffer) {
        final Type type = TypeCodec.decode(buffer);
        return fieldCodecs.get(type).decode(buffer);
    }
}
