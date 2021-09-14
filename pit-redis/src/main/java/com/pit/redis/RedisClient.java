package com.pit.redis;

import com.pit.core.executor.RetryExecutor;
import com.pit.core.time.CachingSystemTimer2;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.*;

/**
 * redis client
 *
 * @author gy
 * @version 1.0
 * @date 2020/7/7.
 */
public class RedisClient {
    private JedisPoolContainer jedisPoolContainer;

    public RedisClient(JedisPoolContainer jedisPoolContainer) {
        this.jedisPoolContainer = jedisPoolContainer;
    }

    private static final int RETRY_TIME = 3;

    private static final int INCR_MAX = 10000;

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

    public String get(String key) {
        if (StringUtils.isBlank(key)) {
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

    public String set(String key, String value, long milliseconds) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> {
            return jedis.psetex(key, milliseconds, value);
        }, RETRY_TIME);
    }

    public Long del(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.del(key), RETRY_TIME);
    }

    public Long incr(String key) {
        return incr(key, INCR_MAX);
    }

    public Long incr(String key, long max) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> {
            long incr = jedis.incr(key);
            if (incr > max) {
                jedis.del(key);
                incr = 0;
            }
            return incr;
        }, RETRY_TIME);
    }

    public String set(String key, String value) {
        return set(key, value, 0);
    }

    public boolean setNX(String key, String value, Long expireSeconds) {
        String code = set(key, value, "NX", "PX", expireSeconds);
        return "OK".equals(code);
    }

    public String set(String key, String value, String nxxx, String expx, long time) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> jedis.set(key, value, nxxx, expx, time));
    }

    public Long rpush(String key, long milliseconds, String... values) {
        if (StringUtils.isBlank(key)) {
            return 0L;
        }
        return exec((jedis) -> {
            Long result = jedis.rpush(key, values);
            if (milliseconds > 0) {
                jedis.pexpire(key, milliseconds);
            }
            return result;
        });
    }

    public Long setList(String key, long milliseconds, int retryTime, String... values) {
        if (StringUtils.isBlank(key)) {
            return 0L;
        }
        return exec((jedis) -> {
            Long result = 0L;
            int retryTimeTemp = retryTime;
            while (result != values.length && retryTimeTemp-- > 0) {
                jedis.del(key);
                result = jedis.rpush(key, values);
            }
            if (milliseconds > 0) {
                jedis.pexpire(key, milliseconds);
            }
            return result;
        });
    }

    public List<String> lrange(String key, long start, long end) {
        if (StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }
        return exec((jedis) -> jedis.lrange(key, start, end), RETRY_TIME);
    }

    public long llen(String key) {
        if (StringUtils.isBlank(key)) {
            return 0L;
        }
        return exec((jedis) -> jedis.llen(key), RETRY_TIME);
    }

    public List<String> blpop(int timeout, String key) {
        if (StringUtils.isBlank(key) || timeout < 0) {
            return Collections.emptyList();
        }
        return exec((jedis) -> {
            List<String> result = jedis.blpop(timeout, key);
            return result;
        });
    }

    public String lpop(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return exec((jedis) -> {
            String result = jedis.lpop(key);
            return result;
        });
    }

    public void zadd(String key, double score, String member) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        exec((jedis) -> {
            jedis.zadd(key, score, member);
            return null;
        });
    }

    public Set<String> zrangeByScore(String key, double startScore, double endScore) {
        if (StringUtils.isBlank(key)) {
            return Collections.emptySet();
        }
        return exec((jedis) -> {
            Set<String> set = jedis.zrangeByScore(key, startScore, endScore);
            return set;
        });
    }

    public void zrem(String key, String member) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        exec((jedis) -> {
            return jedis.zrem(key, member);
        }, RETRY_TIME);
    }

    public Long hset(final String key, final String field, final String value) {
        if (StringUtils.isAnyBlank(key, field, value)) {
            return null;
        }

        return exec((jedis) -> {
            return jedis.hset(key, field, value);
        }, RETRY_TIME);
    }

    public List<String> mget(String... keys) {
        if (ArrayUtils.isEmpty(keys)) {
            return Collections.emptyList();
        }

        return exec((jedis) -> {
            return jedis.mget(keys);
        });
    }

    public String mset(String... keysvalues) {
        return exec((jedis) -> {
            return jedis.mset(keysvalues);
        });
    }

    public List<Object> msetAndExpire(long milliseconds, Map<String, String> keyValues) {
        if (MapUtils.isEmpty(keyValues) || milliseconds <= 0) {
            return Collections.EMPTY_LIST;
        }

        return exec(jedis -> {
            Pipeline pipeline = jedis.pipelined();

            for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                pipeline.psetex(entry.getKey(), milliseconds, entry.getValue());
            }

            return pipeline.syncAndReturnAll();
        });
    }

    public List<Object> msetAndExpire(long milliseconds, Map<String, String> keyValues, long bakMilliseconds, Map<String, String> keyValuesBak) {
        if (MapUtils.isEmpty(keyValues) || milliseconds <= 0) {
            return Collections.EMPTY_LIST;
        }

        return exec(jedis -> {
            Pipeline pipeline = jedis.pipelined();

            for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                pipeline.psetex(entry.getKey(), milliseconds, entry.getValue());
            }

            for (Map.Entry<String, String> entry : keyValuesBak.entrySet()) {
                pipeline.psetex(entry.getKey(), bakMilliseconds, entry.getValue());
            }

            return pipeline.syncAndReturnAll();
        });
    }

    public Map<String, Response<String>> batchGet(List<String> keys) {
        return exec((jedis) -> {
            Pipeline pipelined = jedis.pipelined();
            HashMap<String, Response<String>> map = new HashMap<String, Response<String>>();
            for (String key : keys) {
                map.put(key, pipelined.get(key));
            }

            pipelined.sync();
            return map;
        });
    }

    public Map<String, String> hgetAll(String key) {
        if (StringUtils.isBlank(key)) {
            return new HashMap<>();
        }
        return exec((jedis) -> jedis.hgetAll(key), 1);
    }

    public Map<String, Response<Map<String, String>>> batchHGetAll(List<String> keys) {
        return exec((jedis) -> {
            Pipeline pipelined = jedis.pipelined();
            HashMap<String, Response<Map<String, String>>> map = new HashMap<>();
            for (String key : keys) {
                map.put(key, pipelined.hgetAll(key));
            }

            pipelined.sync();
            return map;
        });
    }

    public Map<String, Response<String>> batchHGet(List<String> keys, String field) {
        return exec((jedis) -> {
            Pipeline pipelined = jedis.pipelined();
            HashMap<String, Response<String>> map = new HashMap<String, Response<String>>();
            for (String key : keys) {
                map.put(key, pipelined.hget(key, field));
            }

            pipelined.sync();
            return map;
        });
    }
}