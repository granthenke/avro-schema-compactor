package org.avro.compactor.codec.field;

import net.magik6k.bitbuffer.BitBuffer;
import org.apache.avro.Schema.Type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encodes and decodes Avro schema types
 *    - The type id is stored in 4 bits
 *
 *    TODO: Support LogicalType
 */
public class TypeCodec {

    private static final int ID_BITS = 4;

    private static final Map<Byte, Type> idToType = new HashMap<Byte, Type>();
    private static final Map<Type, Byte> typeToId = new HashMap<Type, Byte>();

    public static final List<Type> primitiveTypes = Arrays.asList(
        Type.NULL,
        Type.BOOLEAN,
        Type.INT,
        Type.LONG,
        Type.FLOAT,
        Type.DOUBLE,
        Type.STRING,
        Type.BYTES
    );

    public static final List<Type> complexTypes = Arrays.asList(
        Type.RECORD,
        Type.UNION,
        Type.ARRAY,
        Type.MAP,
        Type.ENUM,
        Type.FIXED
    );

    static {
        byte id = 0;
        // Primitive
        for (Type t : primitiveTypes) {
            idToType.put(id, t);
            id++;
        }
        // Complex
        for (Type t : complexTypes) {
            idToType.put(id, t);
            id++;
        }

        // Invert
        for(Map.Entry<Byte, Type> entry : idToType.entrySet()) {
            typeToId.put(entry.getValue(), entry.getKey());
        }
    }

    public static boolean isPrimitive(final Type type) {
        return primitiveTypes.contains(type);
    }

    public static boolean isComplex(final Type type) {
        return complexTypes.contains(type);
    }

    public static void validate(final Type type) {
        if(!typeToId.containsKey(type))
            throw new IllegalArgumentException("Type " + type + " is not supported");
    }

    public static int sizeOf(final Type type) {
        validate(type);
        return ID_BITS;
    }

    public static void encode(final Type type, BitBuffer buffer) {
        validate(type);
        buffer.putByte(typeToId.get(type), ID_BITS);
    }

    public static Type decode(BitBuffer buffer) {
        return idToType.get(buffer.getByteUnsigned(ID_BITS));
    }

}
