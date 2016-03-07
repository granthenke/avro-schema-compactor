package org.avro.compactor.codec;

import net.magik6k.bitbuffer.BitBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Encodes and decodes Avro schema names up to 64 characters long
 *    - The size is stored in 6 bits
 *    - The valid characters are [A-Za-z0-9_.]
 *    - Each character is encoded in 6 bits
 *
 *    TODO: rule flags for more/less strict rules
 *      - 5 bit (lower case alpha + special only)
 *      - 2 digit numeric only (for positions)
 */
public class NameCodec implements BitCodec<String> {

    private static final int NAME_SIZE_BITS = 6;
    private static final int NAME_MAX_LENGTH = (int) Math.pow(2, NAME_SIZE_BITS);
    private static final int NAME_CHAR_BITS = 6;

    private static final Map<Byte, Character> idToChar = new HashMap<Byte, Character>();
    private static final Map<Character, Byte> charToId = new HashMap<Character, Byte>();

    static {
        byte id = 0;
        // Numeric
        for (char ch = '0'; ch <= '9'; ch++) {
            idToChar.put(id, ch);
            id++;
        }
        // Lowercase
        for (char ch = 'a'; ch <= 'z'; ch++) {
            idToChar.put(id, ch);
            id++;
        }
        // Uppercase
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            idToChar.put(id, ch);
            id++;
        }
        // Special
        idToChar.put(id, '_');
        id++;
        idToChar.put(id, '.');

        // Invert
        for(Map.Entry<Byte, Character> entry : idToChar.entrySet()) {
            charToId.put(entry.getValue(), entry.getKey());
        }
    }

    public NameCodec() {}

    public void validate(final String name) {
        if(name.length() > NAME_MAX_LENGTH)
            throw new IllegalArgumentException("Name has a maximum length of " + NAME_MAX_LENGTH + ". Found " + name.length());
        for (final char ch : name.toCharArray()) {
            if(!charToId.containsKey(ch)) {
                throw new IllegalArgumentException("Name contains unsupported character. Found " + ch);
            }
        }
    }

    public int sizeOf(final String name) {
        validate(name);
        int size = 0;
        size += NAME_SIZE_BITS;
        size += name.length() * NAME_CHAR_BITS;
        return size;
    }

    public void encode(final String name, BitBuffer buffer) {
        validate(name);
        buffer.putByte((byte) name.length(), NAME_SIZE_BITS);
        for (final char ch : name.toCharArray()) {
            buffer.putByte(charToId.get(ch), NAME_CHAR_BITS);
        }
    }

    public String decode(BitBuffer buffer) {
        byte size = buffer.getByteUnsigned(NAME_SIZE_BITS);
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < size; i++) {
            b.append(idToChar.get(buffer.getByteUnsigned(NAME_CHAR_BITS)));
        }
        return b.toString();
    }

}
