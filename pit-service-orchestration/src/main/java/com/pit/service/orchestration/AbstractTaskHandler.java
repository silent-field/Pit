package com.pit.service.orchestration;

import com.pit.core.exception.ExceptionUtils;
import com.pit.service.orchestration.bean.ResultVO;
import com.pit.service.orchestration.bean.ServiceInfo;
import com.pit.service.orchestration.constants.BaseResultCodeConstants;
import com.pit.service.orchestration.task.DoCompleteTaskHandler;
import com.pit.service.orchestration.task.DoFailTaskHandler;
import com.pit.service.orchestration.task.DoServiceTaskHandler;
import com.pit.service.orchestration.task.DoSuccessTaskHanlder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
@Slf4j
public abstract class AbstractTaskHandler<I, R> {
	protected IServiceLogService logService;

	protected ServiceInfo<I, R> serviceInfo;

	protected IServiceBefore<I, R> serviceBefore;

	protected String type;

	public AbstractTaskHandler(com.pit.service.orchestration.IService<I, R> service, IServiceLogService logService, IServiceBefore<I, R> serviceBefore, String type) {
		if (null != service) {
			this.serviceInfo = new ServiceInfo<I, R>(service);
		} else {
			this.serviceInfo = null;
		}
		this.logService = logService;
		this.serviceBefore = serviceBefore;
		this.type = type;
	}

	/**
	 * 执行
	 *
	 * @param income
	 * @param output
	 * @return
	 * @throws Exception
	 */
	public abstract AbstractCallback doTask(I income, ResultVO<R> output) throws Exception;

	/**
	 * 开始日志
	 *
	 * @param income
	 * @param output
	 */
	protected void addBeginLog(I income, ResultVO<R> output) {
		addLog(income, output, "begin", null);
	}

	/**
	 * 结束日志
	 *
	 * @param income
	 * @param output
	 * @param beginTime
	 */
	protected void addEndLog(I income, ResultVO<R> output, Long beginTime) {
		addLog(income, output, "end", beginTime);
	}

	/**
	 * 打印日志
	 *
	 * @param income
	 * @param output
	 * @param remark
	 * @param beginTime
	 */
	private void addLog(I income, ResultVO<R> output, String remark, Long beginTime) {
		if (!logService.isNeedLog()) {
			return;
		}

		if (null == serviceInfo) {
			return;
		}

		String methodName = serviceInfo.getSimpleClassName() + "." + this.type;
		try {
			logService.sendRunningAccountLog(income, output, methodName, remark, beginTime);
		} catch (Exception e) {
			log.error("logService error", e);
		}
	}

	/**
	 * 执行
	 *
	 * @param income
	 * @param output
	 * @param isSetResultCode
	 * @return
	 */
	protected AbstractCallback exc(I income, ResultVO<R> output, boolean isSetResultCode) {
		AbstractCallback AbstractCallback = null;
		// 如果为空则说明只有一种情况就是DoFinalTask的,但是也不会走到这里的
		if (null == this.serviceInfo) {
			return null;
		}
		long begin = System.currentTimeMillis();
		addBeginLog(income, output);
		try {
			if (DoServiceTaskHandler.TYPE.equals(this.type)) {
				AbstractCallback = serviceInfo.getService().doService(income, output);
			} else if (DoSuccessTaskHanlder.TYPE.equals(this.type)) {
				AbstractCallback = serviceInfo.getService().doSuccess(income, output);
			} else if (DoFailTaskHandler.TYPE.equals(this.type)) {
				AbstractCallback = serviceInfo.getService().doFail(income, output);
			} else if (DoCompleteTaskHandler.TYPE.equals(this.type)) {
				AbstractCallback = serviceInfo.getService().doComplate(income, output);
			}
		} catch (Exception e) {
			log.error("", e);
			if (isSetResultCode) {
				if (output.success()) {
					output.setResultCode(this.getClass(), BaseResultCodeConstants.CODE_900000);
				}
			}
			output.addResultMsg("system error occur:" + ExceptionUtils.getStackTrace(e));
			// 不能放finally，要不然resultCode就不是真实的
		} finally {
			addEndLog(income, output, begin);
		}
		return AbstractCallback;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ServiceInfo<I, R> getServiceInfo() {
		return serviceInfo;
	}
}
