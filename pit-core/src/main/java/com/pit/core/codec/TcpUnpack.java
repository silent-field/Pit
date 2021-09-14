package com.pit.core.codec;

import com.pit.core.exception.TcpUnpackException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解tcp包，支持大小端，支持无符号
 *
 * @author gy
 * @version 1.0
 * @date 2020/7/13.
 */
@Slf4j
public class TcpUnpack {
    private ByteBuf byteBuf;

    public TcpUnpack(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    public static void main(String[] args) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeLongLE(Long.MAX_VALUE + 10);

        // 小端
        byte[] bytes = new byte[8];
        buf.readBytes(bytes);
        for (int i = 0; i < 4; i++) {
            byte head = bytes[i];
            byte tail = bytes[8 - (i + 1)];

            bytes[i] = tail;
            bytes[8 - (i + 1)] = head;
        }

        BigInteger bigInteger = new BigInteger(1, bytes);

        log.info(Long.MAX_VALUE + "");
        log.info(bigInteger + "");
        log.info(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN).toString());
    }

    public ByteBuf data() {
        return byteBuf;
    }

    public short getShort(boolean littleEndian) {
        if (littleEndian) {
            return byteBuf.readShortLE();
        } else {
            return byteBuf.readShort();
        }
    }

    public int getUnsignedShort(boolean littleEndian) {
        if (littleEndian) {
            return byteBuf.readUnsignedShortLE();
        } else {
            return byteBuf.readUnsignedShort();
        }
    }

    public int getInt(boolean littleEndian) {
        if (littleEndian) {
            return byteBuf.readIntLE();
        } else {
            return byteBuf.readInt();
        }
    }

    public long getUnsignedInt(boolean littleEndian) {
        if (littleEndian) {
            return byteBuf.readUnsignedIntLE();
        } else {
            return byteBuf.readUnsignedInt();
        }
    }

    public byte getByte() {
        return byteBuf.readByte();
    }

    public long getLong(boolean littleEndian) {
        if (littleEndian) {
            return byteBuf.readLongLE();
        } else {
            return byteBuf.readLong();
        }
    }

    public BigInteger getUnsignedLong(boolean littleEndian) {
        byte[] bytes = new byte[8];
        byteBuf.readBytes(bytes);

        if (littleEndian) {
            // 转成小端
            ArrayUtils.reverse(bytes);
        }

        return new BigInteger(1, bytes);
    }

    public String getString(boolean littleEndian) {
        int len = getUnsignedShort(littleEndian);
        byte[] byteArray = new byte[len];
        byteBuf.readBytes(byteArray);
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    public void unmarshal(Unmarshallable m) {
        m.unmarshal(this);
    }

    public <T extends Unmarshallable> List<T> unmarshalList(Class<T> clz, boolean littleEndian) {
        long size = getUnsignedInt(littleEndian);
        List<T> list = new ArrayList<>();
        try {
            for (int i = 0; i < size; i++) {
                T unmarshallable = clz.newInstance();
                unmarshallable.unmarshal(this);
                list.add(unmarshallable);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new TcpUnpackException(1, e.getMessage());
        }
        return list;
    }

    public List<Long> unmarshalUnsignedIntList(boolean littleEndian) {
        long size = getUnsignedInt(littleEndian);
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(getUnsignedInt(littleEndian));
        }
        return list;
    }

    public Map<String, String> unmarshalMap(boolean littleEndian) {
        long size = getUnsignedInt(littleEndian);
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(getString(littleEndian), getString(littleEndian));
        }
        return map;
    }
}
