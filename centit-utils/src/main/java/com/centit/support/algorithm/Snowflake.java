package com.centit.support.algorithm;

import com.centit.support.common.ObjectException;
import com.centit.support.network.HardWareUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class Snowflake implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 默认的起始时间，为Thu, 04 Nov 2010 01:42:54 GMT
     */
    public static long DEFAULT_TWEPOCH = 1288834974657L;
    /**
     * 默认回拨时间，2S
     */
    public static long DEFAULT_TIME_OFFSET = 2000L;

    private static final long WORKER_ID_BITS = 5L;
    // 最大支持机器节点数0~31，一共32个
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
    private static final long DATA_CENTER_ID_BITS = 5L;
    // 最大支持数据中心节点数0~31，一共32个
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private static final long MAX_DATA_CENTER_ID = -1L ^ (-1L << DATA_CENTER_ID_BITS);
    // 序列号12位（表示只允许workId的范围为：0-4095）
    private static final long SEQUENCE_BITS = 12L;
    // 机器节点左移12位
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    // 数据中心节点左移17位
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    // 时间毫秒数左移22位
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;
    // 序列掩码，用于限定序列最大值不能超过4095
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);// 4095

    /**
     * 初始化时间点
     */
    private final long twepoch;
    private final long workerId;
    private final long dataCenterId;
    private final boolean useSystemClock;
    /**
     * 允许的时钟回拨毫秒数
     */
    private final long timeOffset;
    /**
     * 当在低频模式下时，序号始终为0，导致生成ID始终为偶数<br>
     * 此属性用于限定一个随机上限，在不同毫秒下生成序号时，给定一个随机数，避免偶数问题。<br>
     * 注意次数必须小于{@link #SEQUENCE_MASK}，{@code 0}表示不使用随机数。<br>
     * 这个上限不包括值本身。
     */
    private final long randomSequenceLimit;

    /**
     * 自增序号，当高频模式下时，同一毫秒内生成N个ID，则这个序号在同一毫秒下，自增以避免ID重复。
     */
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 获取机器ID，使用进程ID配合数据中心ID生成<br>
     * 机器依赖于本进程ID或进程名的Hash值。
     *
     * <p>
     * 此算法来自于mybatis-plus#Sequence
     * </p>
     *
     * @param datacenterId 数据中心ID
     * @param maxWorkerId  最大的机器节点ID
     * @return ID
     * @since 5.7.3
     */
    public static long makeWorkerId(long datacenterId, long maxWorkerId) {
        final StringBuilder mpid = new StringBuilder();
        mpid.append(datacenterId);
        try {
            mpid.append(HardWareUtils.getPid());
        } catch (ObjectException igonre) {
            //ignore
        }
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }


    /**
     * 获取数据中心ID<br>
     * 数据中心ID依赖于本地网卡MAC地址。
     * <p>
     * 此算法来自于mybatis-plus#Sequence
     * </p>
     *
     * @param maxDatacenterId 最大的中心ID
     * @return 数据中心ID
     * @since 5.7.3
     */
    public static long makeDataCenterId(long maxDatacenterId) {
        //Assert.check(maxDatacenterId > 0, "maxDatacenterId must be > 0");
        if(maxDatacenterId == Long.MAX_VALUE){
            maxDatacenterId -= 1;
        }
        long id = 1L;
        byte[] mac = null;
        try{
            mac = HardWareUtils.getHardwareAddress(HardWareUtils.getLocalhost());
        }catch (ObjectException ignore){
            // ignore
        }
        if (null != mac) {
            id = ((0x000000FF & (long) mac[mac.length - 2])
                | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
            id = id % (maxDatacenterId + 1);
        }
        return id;
    }

    public Snowflake() {
        this.twepoch = DEFAULT_TWEPOCH;
        this.dataCenterId = Snowflake.makeDataCenterId(MAX_DATA_CENTER_ID);
        this.workerId = Snowflake.makeWorkerId(this.dataCenterId , MAX_WORKER_ID);
        this.useSystemClock = false;
        this.timeOffset = DEFAULT_TIME_OFFSET;
        this.randomSequenceLimit = 0;
    }

    public Snowflake(long workerId, long dataCenterId) {
        this.twepoch = DEFAULT_TWEPOCH;
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
        this.useSystemClock = false;
        this.timeOffset = DEFAULT_TIME_OFFSET;
        this.randomSequenceLimit = 0;
    }

    /**
     * 根据Snowflake的ID，获取机器id
     *
     * @param id snowflake算法生成的id
     * @return 所属机器的id
     */
    public long getWorkerId(long id) {
        return id >> WORKER_ID_SHIFT & ~(-1L << WORKER_ID_BITS);
    }

    /**
     * 根据Snowflake的ID，获取数据中心id
     *
     * @param id snowflake算法生成的id
     * @return 所属数据中心
     */
    public long getDataCenterId(long id) {
        return id >> DATA_CENTER_ID_SHIFT & ~(-1L << DATA_CENTER_ID_BITS);
    }

    /**
     * 根据Snowflake的ID，获取生成时间
     *
     * @param id snowflake算法生成的id
     * @return 生成的时间
     */
    public long getGenerateDateTime(long id) {
        return (id >> TIMESTAMP_LEFT_SHIFT & ~(-1L << 41L)) + twepoch;
    }

    /**
     * 下一个ID
     *
     * @return ID
     */
    public synchronized long nextId() {
        long timestamp = genTime();
        if (timestamp < this.lastTimestamp) {
            if (this.lastTimestamp - timestamp < timeOffset) {
                // 容忍指定的回拨，避免NTP校时造成的异常
                timestamp = lastTimestamp;
            } else {
                // 如果服务器时间有问题(时钟后退) 报错。
                throw new ObjectException("Clock moved backwards. Refusing to generate id for "+(lastTimestamp - timestamp)+"ms");
            }
        }

        if (timestamp == this.lastTimestamp) {
            final long sequence = (this.sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
            this.sequence = sequence;
        } else {
            // issue#I51EJY
            if (randomSequenceLimit > 1) {
                sequence = ThreadLocalRandom.current().nextLong(randomSequenceLimit);
            } else {
                sequence = 0L;
            }
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << TIMESTAMP_LEFT_SHIFT)
            | (dataCenterId << DATA_CENTER_ID_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence;
    }

    /**
     * 下一个ID（字符串形式）
     *
     * @return ID 字符串形式
     */
    public String nextIdStr() {
        return Long.toString(nextId());
    }

    public String nextIdHex() {
        return Long.toHexString(nextId());
    }

    public String nextIdBase64() {
        byte[] uidBytes = new byte[8];
        ByteBaseOpt.writeInt64(uidBytes, nextId(), 0);
        return new String(Base64.encodeBase64URLSafe(uidBytes));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------ Private method start

    /**
     * 循环等待下一个时间
     *
     * @param lastTimestamp 上次记录的时间
     * @return 下一个时间
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = genTime();
        // 循环直到操作系统时间戳变化
        while (timestamp == lastTimestamp) {
            timestamp = genTime();
        }
        if (timestamp < lastTimestamp) {
            // 如果发现新的时间戳比上次记录的时间戳数值小，说明操作系统时间发生了倒退，报错
            throw new ObjectException(
                "Clock moved backwards. Refusing to generate id for "+(lastTimestamp - timestamp)+"ms");
        }
        return timestamp;
    }

    /**
     * 生成时间戳
     *
     * @return 时间戳
     */
    private long genTime() {
        return /*this.useSystemClock ? SystemClock.now() : */System.currentTimeMillis();
    }
}
