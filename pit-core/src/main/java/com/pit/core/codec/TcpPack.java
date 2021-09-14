package com.pit.core.codec;

import com.pit.core.exception.TcpPackException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 构建tcp包，支持大小端，支持无符号
 *
 * @author gy
 * @version 1.0
 * @date 2020/7/13.
 */
@Slf4j
public class TcpPack {
    private ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
    private static final int MAX_UNSIGNED_SHORT = 0xFFFF;
    private static final long MAX_UNSIGNED_INT = 0xFFFFFFFFL;
    private static final BigInteger MAX_UNSIGNED_LONG = BigInteger.valueOf(Long.MAX_VALUE).shiftLeft(1).add(BigInteger.ONE);

    public ByteBuf data() {
        return byteBuf;
    }

    public TcpPack putShort(short i, boolean littleEndian) {
        if (littleEndian) {
            byteBuf.writeShortLE(i);
        } else {
            byteBuf.writeShort(i);
        }

        return this;
    }

    public TcpPack putUnsignedShort(int i, boolean littleEndian) {
        if (i > MAX_UNSIGNED_SHORT) {
            throw new TcpPackException(1, "TcpPack.putUnsignedShort : bigger MAX_UNSIGNED_SHORT : " + MAX_UNSIGNED_SHORT);
        }

        if (littleEndian) {
            byteBuf.writeShortLE((short) i);
        } else {
            byteBuf.writeShort((short) i);
        }

        return this;
    }

    public TcpPack putInt(int i, boolean littleEndian) {
        if (littleEndian) {
            byteBuf.writeIntLE(i);
        } else {
            byteBuf.writeInt(i);
        }

        return this;
    }

    public TcpPack putUnsignedInt(long i, boolean littleEndian) {
        if (i > MAX_UNSIGNED_INT) {
            throw new TcpPackException(1, "TcpPack.putUnsignedInt : bigger MAX_UNSIGNED_INT : " + MAX_UNSIGNED_INT);
        }

        if (littleEndian) {
            byteBuf.writeIntLE((int) i);
        } else {
            byteBuf.writeInt((int) i);
        }

        return this;
    }

    public TcpPack putByte(byte i) {
        byteBuf.writeByte(i);
        return this;
    }

    public TcpPack putLong(long i, boolean littleEndian) {
        if (littleEndian) {
            byteBuf.writeLongLE(i);
        } else {
            byteBuf.writeLong(i);
        }

        return this;
    }

    public TcpPack putUnsignedLong(BigInteger i, boolean littleEndian) {
        if (i.compareTo(MAX_UNSIGNED_LONG) > 0) {
            throw new TcpPackException(1, "TcpPack.putUnsignedLong : bigger MAX_UNSIGNED_LONG : " + MAX_UNSIGNED_LONG);
        }

        if (littleEndian) {
            byteBuf.writeLongLE(i.longValue());
        } else {
            byteBuf.writeLong(i.longValue());
        }

        return this;
    }

    public TcpPack putString(String s, boolean littleEndian) {
        if (StringUtils.isEmpty(s)) {
            s = StringUtils.EMPTY;
        }

        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        int len = data.length;
        if (len > 0xFFFF) {
            throw new TcpPackException(2, "string size too large");
        }
        putUnsignedShort(len, littleEndian);
        byteBuf.writeBytes(data);
        return this;
    }

    public void marshall(Marshallable m) {
        m.marshall(this);
    }

    public TcpPack marshall(List<? extends Marshallable> list, boolean littleEndian) {
        putUnsignedInt(list.size(), littleEndian);
        for (Marshallable ma : list) {
            marshall(ma);
        }
        return this;
    }

    public TcpPack putStringList(List<String> list, boolean littleEndian) {
        putUnsignedInt(list.size(), littleEndian);
        for (String s : list) {
            putString(s, littleEndian);
        }

        return this;
    }

    public TcpPack putMap(Map<String, String> map, boolean littleEndian) {
        putUnsignedInt(map.size(), littleEndian);
        map.forEach((k, v) -> {
            putString(k, littleEndian);
            putString(v, littleEndian);
        });
        return this;
    }
}
