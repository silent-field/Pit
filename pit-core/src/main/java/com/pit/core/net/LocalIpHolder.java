package com.pit.core.net;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;

/**
 * @author gy
 */
@Slf4j
public class LocalIpHolder {
	private volatile static String localIp = null;

	private volatile static String pid = null;

	private volatile static String instanceId = null;

	public static String getInstanceId() {
		if (null == instanceId) {
			instanceId = getInstanceId(getIp(), getPid());
		}
		return instanceId;
	}

	public static void setIP(String ip) {
		localIp = ip;
		instanceId = getInstanceId(ip, getPid());
	}

	private static String getInstanceId(String ip, String pid) {
		return ip + ":" + pid;
	}

	public static String getIp() {
		if (null == localIp) {
			try {
				initLocalIp();
			} catch (Exception e) {
				log.error("LocalIpHolder getIp", e);
			}
		}

		return localIp;
	}

	private synchronized static void initLocalIp() throws Exception {
		if (null != localIp) {
			return;
		}

		localIp = IpUtil.getIp();
	}

	public static String getPid() {
		if (null == pid) {
			try {
				initPid();
			} catch (Exception e) {
				log.error("LocalIpHolder getPid", e);
			}
		}

		return pid;
	}

	private synchronized static void initPid() {
		if (null != pid) {
			return;
		}
		String pidTmp = ManagementFactory.getRuntimeMXBean().getName();
		String[] pidTmpArr = pidTmp.split("@");
		pid = pidTmpArr[0];
	}


	public static void main(String[] args) {
		System.out.println(LocalIpHolder.getIp());
		System.out.println(LocalIpHolder.getPid());
		System.out.println(LocalIpHolder.getInstanceId());
	}
}
