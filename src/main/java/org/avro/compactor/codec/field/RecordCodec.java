package org.avro.compactor.codec.field;

import net.magik6k.bitbuffer.BitBuffer;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.avro.compactor.codec.BitCodec;
import org.avro.compactor.codec.FieldCodec;
import org.avro.compactor.codec.NameCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * Encodes and decodes an Avro record schema with up to 64 fields
 *    - The field count is stored in 6 bits
 *    - The field name is encoded/decoded with NameCodec
 *
 *    TODO: rule flags for more/less strict rules
 *      - More fields
 */
public class RecordCodec implements BitCodec<Schema> {

    private static final int FIELDS_COUNT_BITS = 6;
    private static final int FIELDS_MAX_COUNT = (int) Math.pow(2, FIELDS_COUNT_BITS);

    private final NameCodec nameCodec;
    private final FieldCodec fieldCodec;

    public RecordCodec(final NameCodec nameCodec, final FieldCodec fieldCodec) {
        this.nameCodec = nameCodec;
        this.fieldCodec = fieldCodec;
    }

    public void validate(final Schema schema) {
        if (schema.getType() != Type.RECORD)
            throw new IllegalArgumentException("Must be type " + Type.RECORD);
        nameCodec.validate(schema.getName());
        if (schema.getFields().size() > FIELDS_MAX_COUNT)
            throw new IllegalArgumentException("Only supports up to " + FIELDS_MAX_COUNT + " fields. Found " + schema.getFields().size());
        for (final Field field : schema.getFields()) {
            nameCodec.validate(field.name());
            fieldCodec.validate(field.schema());
        }
    }

    public int sizeOf(final Schema schema) {
        validate(schema);
        int size = 0;
        size += nameCodec.sizeOf(schema.getName());
        size += FIELDS_COUNT_BITS;
        for (final Field field : schema.getFields()) {
            size += nameCodec.sizeOf(field.name());
            size += fieldCodec.sizeOf(field.schema());
        }
        return size;
    }

    public void encode(final Schema schema, BitBuffer buffer) {
        validate(schema);
        nameCodec.encode(schema.getName(), buffer);
        buffer.putByte((byte) schema.getFields().size(), FIELDS_COUNT_BITS);
        for (final Field field : schema.getFields()) {
            nameCodec.encode(field.name(), buffer);
            fieldCodec.encode(field.schema(), buffer);
        }
    }

    public Schema decode(BitBuffer buffer) {
        final String name = nameCodec.decode(buffer);
        final Byte fieldCount = buffer.getByteUnsigned(FIELDS_COUNT_BITS);
        final List<Field> fields = new ArrayList<Field>();
        for(int i = 0; i < fieldCount; i++) {
            final String fieldName = nameCodec.decode(buffer);
            final Schema fieldSchema = fieldCodec.decode(buffer);
            fields.add(new Field(fieldName, fieldSchema, null, (Object) null));
        }
        return Schema.createRecord(name, null, null, false, fields);
    }

}


