package org.avro.compactor;

import net.magik6k.bitbuffer.BitBuffer;
import org.apache.avro.Schema;
import org.avro.compactor.codec.BitCodec;
import org.avro.compactor.codec.FieldCodec;
import org.avro.compactor.codec.NameCodec;
import org.avro.compactor.codec.VersionCodec;

/**
 * TODO: autodetect compatibility and set feature flags
 * - example: If all enum symbols are all caps and alpha (set higher compaction rule)
 * - example: If field names are only 32 characters long (set higher compaction rule)
 */
public class SchemaCompactor {

    private static byte CURRENT_VERSION = 1;
    private static BitCodec<Byte> VERSION_CODEC = new VersionCodec();
    private static NameCodec NAME_CODEC = new NameCodec();
    private static FieldCodec FIELD_CODEC = new FieldCodec(NAME_CODEC, NAME_CODEC);

    public static void validate(final Schema schema) {
        FIELD_CODEC.validate(schema);
    }

    public static int sizeOf(final Schema schema) {
        return (int) Math.ceil(((double) sizeOfBits(schema)) / 8);
    }

    private static int sizeOfBits(final Schema schema) {
        validate(schema);
        int size = 0;
        size += VERSION_CODEC.sizeOf(CURRENT_VERSION);
        size += FIELD_CODEC.sizeOf(schema);
        return size;
    }

    public static byte[] encode(final Schema schema) {
        validate(schema);
        final BitBuffer buffer = BitBuffer.allocate(sizeOfBits(schema));
        VERSION_CODEC.encode(CURRENT_VERSION, buffer);
        FIELD_CODEC.encode(schema, buffer);
        return buffer.asByteArray();
    }

    public static Schema decode(final byte[] bytes) {
        final BitBuffer buffer = BitBuffer.wrap(bytes);
        final byte version = VERSION_CODEC.decode(buffer); // TODO: handle old versions
        return FIELD_CODEC.decode(buffer);
    }


}
