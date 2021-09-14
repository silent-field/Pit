/**
 * 
 */
package com.pit.service.orchestration;

import com.pit.service.orchestration.bean.ParamPair;
import com.pit.service.orchestration.bean.ResultVO;
import com.pit.service.orchestration.chain.ServiceChain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author gy
 *
 */
public class ServiceFuture {

	private int size = 3;

	private int index = 0;

	private ServiceChain serviceChain;

	private Object lastIncome;

	private ResultVO lastOutput;

	List<ParamPair> pairList = new ArrayList<>();

	private CountDownLatch latch = new CountDownLatch(1);

	public ServiceFuture(int size) {
		super();
		this.size = size;
	}

	public ServiceFuture() {
		super();
	}

	public void init(ServiceChain serviceChain, Object income, ResultVO output) {
		this.serviceChain = serviceChain;
		pairList.add(new ParamPair(income, output));
	}

	public boolean waitEnd(long timeout, TimeUnit unit) {
		try {
			if (timeout == 0) {
				latch.await();
				return true;
			} else {
				return latch.await(timeout, unit);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("future error can not wait work end:", e);
		}
	}

	public void work() {
		workNext();
	}

	/**
	 * 下一个
	 */
	public void workNext() {
		if (null == serviceChain) {
			latch.countDown();
			return;
		}
		if (index >= pairList.size()) {
			latch.countDown();
			return;
		}
		ParamPair pair = pairList.get(index++);
		serviceChain.doTask(pair.getIncome(), pair.getOutput());
	}

	public Object getLastIncome() {
		return lastIncome;
	}

	public void setLastIncome(Object lastIncome) {
		this.lastIncome = lastIncome;
	}

	public ResultVO getLastOutput() {
		return lastOutput;
	}

	public void setLastOutput(ResultVO lastOutput) {
		this.lastOutput = lastOutput;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public List<ParamPair> getPairList() {
		return pairList;
	}

	public void setPairList(List<ParamPair> pairList) {
		this.pairList = pairList;
	}

}
