package com.pit.redis;

import com.pit.core.executor.RetryExecutor;
import com.pit.core.math.NumberUtils2;
import com.pit.core.time.CachingSystemTimer2;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis client
 *
 * @author gy
 * @version 1.0
 * @date 2020/7/7.
 */
public class RedisClient {
    private static final int RETRY_TIME = 3;
    private JedisPoolContainer jedisPoolContainer;

    public RedisClient(JedisPoolContainer jedisPoolContainer) {
        this.jedisPoolContainer = jedisPoolContainer;
    }

    private <T> T exec(RetryExecutor<T, Jedis> executor) {
        return exec(executor, 1);
    }

    private <T> T exec(RetryExecutor<T, Jedis> executor, int retry) {
        for (int i = 0; i <= retry; i++) {
            Jedis jedis = null;
            try {
                jedis = jedisPoolContainer.getClient();
                long start = CachingSystemTimer2.getNow();

                T t = executor.exec(jedis);

                // 计算耗时是否大于阈值，如果默认100ms
                long cost = CachingSystemTimer2.getNow() - start;
                if (cost > 100L) {
                    if (null != jedis) {
                        jedisPoolContainer.executeError(jedis);
                    }
                }
                return t;
            } catch (Exception e) {
                if (null != jedis) {
                    jedisPoolContainer.executeError(jedis);
                }
                if (i == retry) {
                    throw e;
                }
            } finally {
                if (null != jedis) {
                    jedis.close();
                }
            }
        }
        return null;
    }

    // ---------- common begin

    /**
     * 删除 key
     * @param key
     * @return
     */
    public Long del(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.del(key), RETRY_TIME);
    }

    /**
     * 判断 key 是否存在
     * @param key
     * @return
     */
    public Boolean exist(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.exists(key));
    }

    /**
     * 为给定 key 设置过期时间，以秒计。
     * @param key
     * @param seconds
     * @return
     */
    public Long expire(String key, int seconds) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.expire(key, seconds));
    }

    /**
     * EXPIREAT 的作用和 EXPIRE 类似，都用于为 key 设置过期时间。
     * 不同在于 EXPIREAT 命令接受的时间参数是 UNIX 时间戳(unix timestamp)。
     * @param key
     * @param unixTime
     * @return
     */
    public Long expireAt(String key, long unixTime) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.expireAt(key, unixTime));
    }

    /**
     * 设置 key 的过期时间以毫秒计。
     * @param key
     * @param milliseconds
     * @return
     */
    public Long pexpire(String key, long milliseconds) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.pexpire(key, milliseconds));
    }

    /**
     * 设置 key 过期时间的时间戳(unix timestamp) 以毫秒计
     * @param key
     * @param millisecondsTimestamp
     * @return
     */
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.pexpireAt(key, millisecondsTimestamp));
    }
    // ---------- common end

    // ---------- string begin

    /**
     * 获取指定 key 的值。
     * @param key
     * @return
     */
    public String get(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.get(key), RETRY_TIME);
    }

    public byte[] getByte(byte[] key) {
        if (ArrayUtils.isEmpty(key)) {
            return null;
        }
        return exec((jedis) -> jedis.get(key), RETRY_TIME);
    }

    /**
     * 返回 key 中字符串值的子字符
     * @param key
     * @param start
     * @param end
     * @return
     */
    public String getRange(String key, Long start, Long end) {
        if (StringUtils.isAnyBlank(key) || NumberUtils2.isAnyNegativeStrict(start, end)) {
            return null;
        }
        return exec((jedis) -> jedis.getrange(key, start, end), RETRY_TIME);
    }

    /**
     * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。
     * @param key
     * @param value
     * @return
     */
    public String getAndSet(String key, String value) {
        if (StringUtils.isAnyBlank(key, value)) {
            return null;
        }
        return exec((jedis) -> jedis.getSet(key, value), RETRY_TIME);
    }

    /**
     * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)。
     * @param key
     * @param offset
     * @return
     */
    public Boolean getAndSet(String key, Long offset) {
        if (StringUtils.isAnyBlank(key) || NumberUtils2.isAnyNegativeStrict(offset)) {
            return null;
        }
        return exec((jedis) -> jedis.getbit(key, offset), RETRY_TIME);
    }

    /**
     * 获取所有(一个或多个)给定 key 的值。
     * @param keys
     * @return
     */
    public List<String> mget(String... keys) {
        if (ArrayUtils.isEmpty(keys)) {
            return null;
        }

        return exec((jedis) -> jedis.mget(keys));
    }

    /**
     * 设置指定 key 的值
     * @param key
     * @param value
     * @return
     */
    public String set(String key, String value) {
        return set(key, value, 0);
    }

    /**
     * 设置指定 key 的值，并设置过期时间
     * @param key
     * @param value
     * @param nxxx
     * @param expx
     * @param time
     * @return
     */
    public String set(String key, String value, String nxxx, String expx, long time) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.set(key, value, nxxx, expx, time));
    }

    /**
     * 设置 key 的值。并设置过期时间(毫秒)
     * @param key
     * @param value
     * @param milliseconds
     * @return
     */
    public String set(String key, String value, long milliseconds) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.psetex(key, milliseconds, value), RETRY_TIME);
    }

    /**
     * 只有在 key 不存在时设置 key 的值。并设置过期时间(毫秒)
     * @param key
     * @param value
     * @param expireSeconds
     * @return
     */
    public boolean setNX(String key, String value, Long expireSeconds) {
        String code = set(key, value, "NX", "EX", expireSeconds);
        return "OK".equals(code);
    }

    /**
     * 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)。
     * @param key
     * @param offset
     * @param value
     * @return
     */
    public Boolean setBit(String key, Long offset, String value) {
        if (StringUtils.isAnyBlank(key, value) || NumberUtils2.isAnyNegativeStrict(offset)) {
            return null;
        }
        return exec((jedis) -> jedis.setbit(key, offset, value), RETRY_TIME);
    }

    /**
     * 用 value 参数覆写给定 key 所储存的字符串值，从偏移量 offset 开始。
     * @param key
     * @param offset
     * @param value
     * @return
     */
    public Long setRange(String key, Long offset, String value) {
        if (StringUtils.isAnyBlank(key, value) || NumberUtils2.isAnyNegativeStrict(offset)) {
            return null;
        }
        return exec((jedis) -> jedis.setrange(key, offset, value), RETRY_TIME);
    }

    /**
     * 同时设置一个或多个 key-value 对。
     * @param keysvalues
     * @return
     */
    public String mset(String... keysvalues) {
        if (ArrayUtils.isEmpty(keysvalues)) {
            return null;
        }
        return exec((jedis) -> jedis.mset(keysvalues));
    }

    /**
     * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在。
     * @param keysvalues
     * @return
     */
    public Long msetnx(String... keysvalues) {
        if (ArrayUtils.isEmpty(keysvalues)) {
            return null;
        }
        return exec((jedis) -> jedis.msetnx(keysvalues));
    }

    public List<Object> msetAndExpire(long milliseconds, Map<String, String> keyValues) {
        if (MapUtils.isEmpty(keyValues) || NumberUtils2.isAnyNonPositiveStrict(milliseconds)) {
            return null;
        }

        return exec(jedis -> {
            Pipeline pipeline = jedis.pipelined();

            for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                pipeline.psetex(entry.getKey(), milliseconds, entry.getValue());
            }

            return pipeline.syncAndReturnAll();
        });
    }

    /**
     * 返回 key 所储存的字符串值的长度。
     * @param key
     * @return
     */
    public Long strLen(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.strlen(key), RETRY_TIME);
    }

    /**
     * 将 key 中储存的数字值增一。
     * @param key
     * @return
     */
    public Long incr(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.incr(key));
    }

    /**
     * 将 key 所储存的值加上给定的增量值（increment） 。
     * @param key
     * @param increment
     * @return
     */
    public Long incr(String key, long increment) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.incrBy(key, increment));
    }

    /**
     * 将 key 所储存的值加上给定的浮点增量值（increment）
     * @param key
     * @param increment
     * @return
     */
    public Double incr(String key, double increment) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.incrByFloat(key, increment));
    }

    /**
     * 将 key 中储存的数字值减一。
     * @param key
     * @return
     */
    public Long decr(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.decr(key));
    }

    /**
     * key 所储存的值减去给定的减量值（decrement） 。
     * @param key
     * @param increment
     * @return
     */
    public Long decr(String key, long increment) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.decrBy(key, increment));
    }

    /**
     * 如果 key 已经存在并且是一个字符串， APPEND 命令将指定的 value 追加到该 key 原来值（value）的末尾。
     * @param key
     * @param append
     * @return
     */
    public Long append(String key, String append) {
        if (StringUtils.isAnyBlank(key, append)) {
            return null;
        }
        return exec((jedis) -> jedis.append(key, append));
    }

    // ---------- string end

    // ---------- hash begin

    /**
     * 删除一个或多个哈希表字段
     * @param key
     * @param members
     * @return
     */
    public Long hdel(String key, String... members) {
        if (StringUtils.isBlank(key) || ArrayUtils.isEmpty(members)) {
            return null;
        }
        return exec((jedis) -> jedis.hdel(key, members));
    }

    /**
     * 查看哈希表 key 中，指定的字段是否存在。
     * @param key
     * @param field
     * @return
     */
    public Boolean hexist(String key, String field) {
        if (StringUtils.isAnyBlank(key, field)) {
            return null;
        }
        return exec((jedis) -> jedis.hexists(key, field));
    }

    /**
     * 获取存储在哈希表中指定字段的值。
     * @param key
     * @param field
     * @return
     */
    public String hget(String key, String field) {
        if (StringUtils.isAnyBlank(key, field)) {
            return null;
        }
        return exec((jedis) -> jedis.hget(key, field));
    }

    /**
     * 获取在哈希表中所有字段和值
     * @param key
     * @return
     */
    public Map<String, String> hgetAll(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.hgetAll(key), 1);
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment 。
     * @param key
     * @param field
     * @param increment
     * @return
     */
    public Long hincrby(String key, String field, Long increment) {
        if (StringUtils.isAnyBlank(key, field) || increment == null) {
            return null;
        }
        return exec((jedis) -> jedis.hincrBy(key, field, increment));
    }

    /**
     * 获取所有哈希表中的字段
     * @param key
     * @return
     */
    public Set<String> hkeys(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.hkeys(key));
    }

    /**
     * 获取哈希表中字段的数量
     * @param key
     * @return
     */
    public Long hlen(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.hlen(key));
    }

    /**
     * 获取所有给定字段的值
     * @param key
     * @param fields
     * @return
     */
    public List<String> hmget(String key, String... fields) {
        if (StringUtils.isAnyBlank(key) || ArrayUtils.isEmpty(fields)) {
            return null;
        }
        return exec((jedis) -> jedis.hmget(key, fields));
    }

    /**
     * 将哈希表 key 中的字段 field 的值设为 value 。
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Long hset(final String key, final String field, final String value) {
        if (StringUtils.isAnyBlank(key, field, value)) {
            return null;
        }

        return exec((jedis) -> {
            return jedis.hset(key, field, value);
        }, RETRY_TIME);
    }

    /**
     * 只有在字段 field 不存在时，设置哈希表字段的值。
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Long hsetnx(final String key, final String field, final String value) {
        if (StringUtils.isAnyBlank(key, field, value)) {
            return null;
        }

        return exec((jedis) -> {
            return jedis.hsetnx(key, field, value);
        }, RETRY_TIME);
    }

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
     * @param key
     * @param news
     * @return
     */
    public String hmset(String key, Map<String, String> news) {
        if (StringUtils.isAnyBlank(key) || MapUtils.isEmpty(news)) {
            return null;
        }
        return exec((jedis) -> jedis.hmset(key, news));
    }

    /**
     * 获取哈希表中所有值。
     * @param key
     * @return
     */
    public List<String> hvalues(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.hvals(key));
    }
    // ---------- hash end

    // ---------- list begin

    /**
     * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     *
     * @param timeout
     * @param key
     * @return 如果队列为空或者超时没有结果则返回null
     */
    public List<String> blpop(int timeout, String key) {
        if (StringUtils.isAnyBlank(key) || timeout < 0) {
            return null;
        }
        return exec((jedis) -> jedis.blpop(timeout, key));
    }

    /**
     * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     *
     * @param timeout
     * @param key
     * @return 如果队列为空或者超时没有结果则返回null
     */
    public List<String> brpop(int timeout, String key) {
        if (StringUtils.isAnyBlank(key) || timeout < 0) {
            return null;
        }
        return exec((jedis) -> jedis.brpop(timeout, key));
    }

    /**
     * 通过索引获取列表中的元素
     * @param key
     * @param index
     * @return
     */
    public String lindex(String key, Long index) {
        if (StringUtils.isAnyBlank(key) || NumberUtils2.isAnyNegativeStrict(index)) {
            return null;
        }
        return exec((jedis) -> jedis.lindex(key, index));
    }

    /**
     * 在列表的元素前或者后插入元素
     * @param key
     * @param where
     * @param pivot
     * @param value
     * @return
     */
    public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
        if (StringUtils.isAnyBlank(key, pivot, value) || where == null) {
            return null;
        }
        return exec((jedis) -> jedis.linsert(key, where, pivot, value));
    }

    /**
     * 获取列表长度
     * @param key
     * @return
     */
    public Long llen(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.llen(key), RETRY_TIME);
    }

    /**
     * 移出并获取列表的第一个元素
     * @param key
     * @return
     */
    public String lpop(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> {
            String result = jedis.lpop(key);
            return result;
        });
    }

    /**
     * 将一个或多个值插入到列表头部
     * @param key
     * @param values
     * @return
     */
    public Long lpush(String key, String... values) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.lpush(key, values));
    }

    /**
     * 获取列表指定范围内的元素
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> lrange(String key, long start, long end) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.lrange(key, start, end), RETRY_TIME);
    }

    /**
     * 移除列表元素
     * @param key
     * @param count
     * @param value
     * @return
     */
    public Long lrem(String key, Long count, String value) {
        if (StringUtils.isAnyBlank(key, value) || NumberUtils2.isAnyNonPositiveStrict(count)) {
            return null;
        }
        return exec((jedis) -> jedis.lrem(key, count, value), RETRY_TIME);
    }

    /**
     * 通过索引设置列表元素的值
     * @param key
     * @param index
     * @param value
     * @return
     */
    public String lset(String key, Long index, String value) {
        if (StringUtils.isAnyBlank(key, value) || NumberUtils2.isAnyNonPositiveStrict(index)) {
            return null;
        }
        return exec((jedis) -> jedis.lset(key, index, value));
    }

    /**
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
     * @param key
     * @param start
     * @param end
     * @return
     */
    public String ltrim(String key, Long start, Long end) {
        if (StringUtils.isAnyBlank(key) || NumberUtils2.isAnyNegativeStrict(start, end)) {
            return null;
        }
        return exec((jedis) -> jedis.ltrim(key, start, end));
    }

    /**
     * 移除列表的最后一个元素，返回值为移除的元素。
     * @param key
     * @return
     */
    public String rpop(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.rpop(key));
    }

    /**
     *
     * @param key
     * @param values
     * @return
     */
    public Long rpush(String key, String... values) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.rpush(key, values));
    }
    // ---------- list end

    // ---------- set begin

    /**
     * 向集合添加一个或多个成员
     * @param key
     * @param members
     * @return
     */
    public Long sadd(String key, String... members) {
        if (StringUtils.isAnyBlank(key) || ArrayUtils.isEmpty(members)) {
            return null;
        }
        return exec((jedis) -> jedis.sadd(key, members));
    }

    /**
     * 获取集合的成员数
     * @param key
     * @return
     */
    public Long scard(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.scard(key));
    }

    /**
     * 判断 member 元素是否是集合 key 的成员
     * @param key
     * @param member
     * @return
     */
    public Boolean sismember(String key, String member) {
        if (StringUtils.isAnyBlank(key, member)) {
            return null;
        }
        return exec((jedis) -> jedis.sismember(key, member));
    }

    /**
     * 返回集合中的所有成员
     * @param key
     * @return
     */
    public Set<String> smembers(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.smembers(key));
    }

    /**
     * 移除并返回集合中的一个随机元素
     * @param key
     * @return
     */
    public String spop(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.spop(key));
    }

    /**
     * 返回集合中一个或多个随机数
     * @param key
     * @param count
     * @return
     */
    public List<String> srandmember(String key, Integer count) {
        if (StringUtils.isAnyBlank(key) || NumberUtils2.isAnyNonPositiveStrict(count)) {
            return null;
        }
        return exec((jedis) -> jedis.srandmember(key, count));
    }

    /**
     * 移除集合中一个或多个成员
     * @param key
     * @param members
     * @return
     */
    public Long srem(String key, String... members) {
        if (StringUtils.isAnyBlank(key) || ArrayUtils.isEmpty(members)) {
            return null;
        }
        return exec((jedis) -> jedis.srem(key, members));
    }

    /**
     * 返回第一个集合与其他集合之间的差异。
     * @param keys
     * @return
     */
    public Set<String> sdiff(String... keys) {
        if (ArrayUtils.isEmpty(keys)) {
            return null;
        }
        return exec((jedis) -> jedis.sdiff(keys));
    }

    /**
     * 返回给定所有集合的交集
     * @param keys
     * @return
     */
    public Set<String> sinter(String... keys) {
        if (ArrayUtils.isEmpty(keys)) {
            return null;
        }
        return exec((jedis) -> jedis.sinter(keys));
    }

    /**
     * 返回所有给定集合的并集
     * @param keys
     * @return
     */
    public Set<String> sunion(String... keys) {
        if (ArrayUtils.isEmpty(keys)) {
            return null;
        }
        return exec((jedis) -> jedis.sunion(keys));
    }
    // ---------- set end

    // ---------- sorted set begin
    public Long zadd(String key, double score, String member) {
        if (StringUtils.isAnyBlank(key, member)) {
            return null;
        }
        return exec((jedis) -> jedis.zadd(key, score, member));
    }

    /**
     * 获取有序集合的成员数
     * @param key
     * @return
     */
    public Long zcard(String key) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.zcard(key));
    }

    /**
     * 计算在有序集合中指定区间分数的成员数
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Long zcount(String key, double min, double max) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.zcount(key, min, max));
    }

    /**
     * 有序集合中对指定成员的分数加上增量 increment
     * @param key
     * @param increment
     * @param member
     * @return
     */
    public Double zincrby(String key, double increment, String member) {
        if (StringUtils.isAnyBlank(key, member)) {
            return null;
        }
        return exec((jedis) -> jedis.zincrby(key, increment, member));
    }

    /**
     * 在有序集合中计算指定字典区间内成员数量
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Long zlexcount(String key, String min, String max) {
        if (StringUtils.isAnyBlank(key, min, max)) {
            return null;
        }
        return exec((jedis) -> jedis.zlexcount(key, min, max));
    }

    /**
     * 通过索引区间返回有序集合指定区间内的成员
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrange(String key, long start, long end) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.zrange(key, start, end));
    }

    /**
     * 通过字典区间返回有序集合的成员
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrangeByLex(String key, String start, String end) {
        if (StringUtils.isAnyBlank(key, start, end)) {
            return null;
        }
        return exec((jedis) -> jedis.zrangeByLex(key, start, end));
    }

    /**
     * 通过分数返回有序集合指定区间内的成员
     * @param key
     * @param startScore
     * @param endScore
     * @return
     */
    public Set<String> zrangeByScore(String key, double startScore, double endScore) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.zrangeByScore(key, startScore, endScore));
    }

    /**
     * 返回有序集合中指定成员的索引
     * @param key
     * @param member
     * @return
     */
    public Long zrank(String key, String member) {
        if (StringUtils.isAnyBlank(key, member)) {
            return null;
        }
        return exec((jedis) -> jedis.zrank(key, member));
    }

    /**
     * 移除有序集合中的一个或多个成员
     * @param key
     * @param member
     * @return
     */
    public Long zrem(String key, String member) {
        if (StringUtils.isAnyBlank(key, member)) {
            return null;
        }
        return exec((jedis) -> jedis.zrem(key, member));
    }

    /**
     * 移除有序集合中给定的字典区间的所有成员
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Long zremrangeByLex(String key, String min, String max) {
        if (StringUtils.isAnyBlank(key, min, max)) {
            return null;
        }
        return exec((jedis) -> jedis.zremrangeByLex(key, min, max));
    }

    /**
     * 移除有序集合中给定的排名区间的所有成员
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Long zremrangeByLex(String key, long start, long end) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.zremrangeByRank(key, start, end));
    }

    /**
     * 移除有序集合中给定的分数区间的所有成员
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Long zremrangeByScore(String key, double start, double end) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.zremrangeByScore(key, start, end));
    }

    /**
     * 返回有序集中指定区间内的成员，通过索引，分数从高到低
     * @param key
     * @return
     */
    public Set<String> zrevrange(String key, long start, long end) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.zrevrange(key, start, end));
    }

    /**
     * 返回有序集中指定字典区间内的成员，通过索引，分数从高到低
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<String> zrevrangeByLex(String key, String min, String max) {
        if (StringUtils.isAnyBlank(key, min, max)) {
            return null;
        }
        return exec((jedis) -> jedis.zrevrangeByLex(key, max, min));
    }

    /**
     * 返回有序集中指定分数区间内的成员，分数从高到低排序
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<String> zrevrangeByScore(String key, double min, double max) {
        if (StringUtils.isAnyBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.zrevrangeByScore(key, max, min));
    }

    /**
     * 返回有序集中，成员的分数值
     * @param key
     * @param member
     * @return
     */
    public Double zscore(String key, String member) {
        if (StringUtils.isAnyBlank(key, member)) {
            return null;
        }
        return exec((jedis) -> jedis.zscore(key, member));
    }
    // ---------- sorted set end

    // ---------- HyperLogLog begin

    /**
     * HyperLogLog 是用来做基数统计的算法，HyperLogLog 的优点是，在输入元素的数量或者体积非常非常大时，计算基数所需的空间总是固定的。
     * 在 Redis 里面，每个 HyperLogLog 键只需要花费 12 KB 内存，就可以计算接近 2^64 个不同元素的基 数。这和计算基数时，元素越多耗费内存就越多的集合形成鲜明对比。
     * 但是，因为 HyperLogLog 只会根据输入元素来计算基数，而不会储存输入元素本身，所以 HyperLogLog 不能像集合那样，返回输入的各个元素。
     */

    /**
     * 添加指定元素到 HyperLogLog 中。
     * @param key
     * @param members
     * @return
     */
    public Long pfadd(String key, String... members) {
        if (StringUtils.isAnyBlank(key) || ArrayUtils.isEmpty(members)) {
            return null;
        }
        return exec((jedis) -> jedis.pfadd(key, members));
    }

    /**
     * 返回给定 HyperLogLog 的基数估算值。
     * @param keys
     * @return
     */
    public Long pfcount(String... keys) {
        if (ArrayUtils.isEmpty(keys)) {
            return null;
        }
        return exec((jedis) -> jedis.pfcount(keys));
    }

    // ---------- HyperLogLog end
}