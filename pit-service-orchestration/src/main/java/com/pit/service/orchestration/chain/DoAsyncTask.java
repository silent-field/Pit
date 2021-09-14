package com.pit.service.orchestration.chain;


import com.pit.core.thread.PitThreadInfo;
import com.pit.core.thread.PitThreadLocalHolder;
import com.pit.service.orchestration.bean.ResultVO;

/**
 * @author gy
 */
public class DoAsyncTask implements Runnable {

	private ServiceChain sc;

	private Object income;

	private ResultVO output;

	/**
	 * 线程专用
	 */
	protected PitThreadInfo threadInfo = new PitThreadInfo();

	public DoAsyncTask(ServiceChain sc, Object income, ResultVO output) {
		super();
		this.sc = sc;
		this.income = income;
		this.output = output;
		setThreadInfo(PitThreadLocalHolder.getThreadHolder());
	}

	public void setThreadInfo(PitThreadInfo threadInfo) {
		this.threadInfo.copy(threadInfo);
	}

	@Override
	public void run() {
		PitThreadLocalHolder.setThreadHolder(threadInfo);
		sc.doTask(income, output);
	}

}
