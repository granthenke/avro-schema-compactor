package org.avro.compactor.codec.field;

import net.magik6k.bitbuffer.BitBuffer;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.avro.compactor.codec.BitCodec;
import org.avro.compactor.codec.NameCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * Encodes and decodes an Avro enum schema with up to 16 symbols
 *    - The symbol count is stored in 4 bits
 *
 *    TODO: rule flags for more/less strict rules
 *      - More symbols
 *      - only capital alpha symbols, etc
 */
public class EnumCodec implements BitCodec<Schema> {

    private static final int SYMBOLS_COUNT_BITS = 4;
    private static final int SYMBOLS_MAX_COUNT = (int) Math.pow(2, SYMBOLS_COUNT_BITS);

    private final NameCodec nameCodec;
    private final NameCodec symbolCodec;

    public EnumCodec(final NameCodec nameCodec, final NameCodec symbolCodec) {
        this.nameCodec = nameCodec;
        this.symbolCodec = symbolCodec;
    }

    public void validate(final Schema schema) {
        if (schema.getType() != Type.ENUM)
            throw new IllegalArgumentException("Must be type " + Type.ENUM);
        nameCodec.validate(schema.getName());
        if (schema.getEnumSymbols().size() > SYMBOLS_MAX_COUNT)
            throw new IllegalArgumentException("Only supports up to " + SYMBOLS_MAX_COUNT + " symbols. Found " + schema.getEnumSymbols().size());
        for (final String symbol : schema.getEnumSymbols()) {
            symbolCodec.validate(symbol);
        }
    }

    public int sizeOf(final Schema schema) {
        validate(schema);
        int size = 0;
        size += nameCodec.sizeOf(schema.getName());
        size += SYMBOLS_COUNT_BITS;
        for (final String symbol : schema.getEnumSymbols()) {
            size += symbolCodec.sizeOf(symbol);
        }
        return size;
    }

    public void encode(final Schema schema, BitBuffer buffer) {
        validate(schema);
        nameCodec.encode(schema.getName(), buffer);
        buffer.putByte((byte) schema.getEnumSymbols().size(), SYMBOLS_COUNT_BITS);
        for (final String symbol : schema.getEnumSymbols()) {
            symbolCodec.encode(symbol, buffer);
        }
    }

    public Schema decode(BitBuffer buffer) {
        final String name = nameCodec.decode(buffer);
        final Byte symbolCount = buffer.getByteUnsigned(SYMBOLS_COUNT_BITS);
        final List<String> symbols = new ArrayList<String>();
        for(int i = 0; i < symbolCount; i++) {
            final String symbol = symbolCodec.decode(buffer);
            symbols.add(symbol);
        }
        return Schema.createEnum(name, null, null, symbols);
    }

}


