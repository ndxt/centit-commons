package com.centit.support.network;

import com.centit.support.common.ObjectException;
import com.sun.tools.javac.util.Assert;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.function.Function;

public abstract class HardWareUtil {

    /**
     * 获取所有满足过滤条件的本地IP地址对象
     *
     * @param addressFilter          过滤器，null表示不过滤，获取所有地址
     * @return 过滤后的地址对象列表
     */
    public static LinkedHashSet<InetAddress> localAddressList(Function<InetAddress, Boolean> addressFilter) {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new ObjectException(e);
        }

        if (networkInterfaces == null) {
            throw new ObjectException("Get network interface error!");
        }

        final LinkedHashSet<InetAddress> ipSet = new LinkedHashSet<>();

        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();

            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress != null && (null == addressFilter || addressFilter.apply(inetAddress))) {
                    ipSet.add(inetAddress);
                }
            }
        }

        return ipSet;
    }


    /**
     * 获取本机网卡IP地址，规则如下：
     *
     * <pre>
     * 1. 查找所有网卡地址，必须非回路（loopback）地址、非局域网地址（siteLocal）、IPv4地址
     * 2. 如果无满足要求的地址，调用 {@link InetAddress#getLocalHost()} 获取地址
     * </pre>
     * <p>
     * 此方法不会抛出异常，获取失败将返回{@code null}<br>
     * <p>
     * 见：https://github.com/dromara/hutool/issues/428
     *
     * @return 本机网卡IP地址，获取失败返回{@code null}
     * @since 3.0.1
     */
    public static InetAddress getLocalhost() {
        final LinkedHashSet<InetAddress> localAddressList = localAddressList( address -> {
            // 非loopback地址，指127.*.*.*的地址
            return false == address.isLoopbackAddress()
                // 需为IPV4地址
                && address instanceof Inet4Address;
        });

        if (localAddressList!=null && !localAddressList.isEmpty()) {
            InetAddress address2 = null;
            for (InetAddress inetAddress : localAddressList) {
                if (false == inetAddress.isSiteLocalAddress()) {
                    // 非地区本地地址，指10.0.0.0 ~ 10.255.255.255、172.16.0.0 ~ 172.31.255.255、192.168.0.0 ~ 192.168.255.255
                    return inetAddress;
                } else if (null == address2) {
                    address2 = inetAddress;
                }
            }

            if (null != address2) {
                return address2;
            }
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // ignore
        }

        return null;
    }


    /**
     * 获得指定地址信息中的硬件地址
     *
     * @param inetAddress {@link InetAddress}
     * @return 硬件地址
     * @since 5.7.3
     */
    public static byte[] getHardwareAddress(InetAddress inetAddress) {
        if (null == inetAddress) {
            return null;
        }

        try {
            final NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            if (null != networkInterface) {
                return networkInterface.getHardwareAddress();
            }
        } catch (SocketException e) {
            throw new ObjectException(e);
        }
        return null;
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
    public static long getDataCenterId(long maxDatacenterId) {
        Assert.check(maxDatacenterId > 0, "maxDatacenterId must be > 0");
        if(maxDatacenterId == Long.MAX_VALUE){
            maxDatacenterId -= 1;
        }
        long id = 1L;
        byte[] mac = null;
        try{
            mac = getHardwareAddress(getLocalhost());
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

    public static int getPid() throws ObjectException {
        final String processName = ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isBlank(processName)) {
            throw new ObjectException("Process name is blank!");
        }
        final int atIndex = processName.indexOf('@');
        if (atIndex > 0) {
            return Integer.parseInt(processName.substring(0, atIndex));
        } else {
            return processName.hashCode();
        }
    }

}
