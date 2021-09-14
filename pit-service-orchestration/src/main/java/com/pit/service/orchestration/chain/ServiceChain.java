package com.pit.service.orchestration.chain;

import com.pit.core.exception.ExceptionUtils;
import com.pit.service.orchestration.*;
import com.pit.service.orchestration.annotation.ServiceOrder;
import com.pit.service.orchestration.annotation.ServiceType;
import com.pit.service.orchestration.bean.ResultVO;
import com.pit.service.orchestration.callback.CallbackCombiner;
import com.pit.service.orchestration.callback.ServiceChainCallback;
import com.pit.service.orchestration.constants.BaseResultCodeConstants;
import com.pit.service.orchestration.constants.SysConstants;
import com.pit.service.orchestration.task.*;
import com.pit.service.orchestration.util.SpringBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
@Component
@Slf4j
public class ServiceChain {
	private Map<String, List<AbstractTaskHandler>> taskHandlerMap = new ConcurrentHashMap<>();

	private AtomicBoolean isReady = new AtomicBoolean(false);

	private IServiceBefore serviceBefore;
	private IServiceLogService logService;

	public void init() {
		if (MapUtils.isEmpty(taskHandlerMap)) {
			initServiceChain();
		}
		checkReady();
	}

	/**
	 * 初始化链路
	 */
	private synchronized void initServiceChain() {
		// 判断过滤器表中是否有对象
		if (taskHandlerMap != null && taskHandlerMap.size() > 0) {
			return;
		}
		isReady.set(false);

		serviceBefore = SpringBeanUtils.getSpringBeanByType(IServiceBefore.class);
		logService = SpringBeanUtils.getSpringBeanByType(IServiceLogService.class);

		String[] serviceNames = SpringBeanUtils.getSpringBeanNamesByType(IService.class);

		Map<String, List<IService>> servicesMap = new HashMap<>();
		// 处理ServiceMap逻辑
		for (String name : serviceNames) {
			IService service = SpringBeanUtils.getBean(name);

			Class clazz = AopUtils.getTargetClass(service);

			// service类别名
			String serviceTypeValue = "";
			ServiceType serviceType = (ServiceType) clazz.getAnnotation(ServiceType.class);
			if (null == serviceType) {
				continue;
			} else {
				serviceTypeValue = serviceType.value();
			}

			// service顺序
			int serviceOrderValue = 0;
			ServiceOrder serviceOrder = (ServiceOrder) clazz.getAnnotation(ServiceOrder.class);
			if (null == serviceOrder) {
				throw new RuntimeException(clazz.getName() + " must set @ServiceOrder on the class");
			} else {
				serviceOrderValue = serviceOrder.value();
			}

			putServiceInServicesMap(serviceTypeValue, serviceOrderValue, service, clazz, servicesMap);
		}

		// 处理服务链
		for (Map.Entry<String, List<IService>> entry : servicesMap.entrySet()) {
			List<AbstractTaskHandler> taskList = new ArrayList<>();
			taskHandlerMap.put(entry.getKey(), taskList);
			List<IService> serviceList = entry.getValue();
			// service进
			for (IService service : serviceList) {
				Class clazz = AopUtils.getTargetClass(service);
				if (hasServiceTask(clazz)) {
					taskList.add(new DoServiceTaskHandler(service, logService, serviceBefore));
				}
			}
			// 其它的出
			for (int index = 1; index <= serviceList.size(); index++) {
				IService service = serviceList.get(serviceList.size() - index);
				Class clazz = AopUtils.getTargetClass(service);
				if (hasSuccessTask(clazz)) {
					taskList.add(new DoSuccessTaskHanlder(service, logService, serviceBefore));
				}
				if (hasFailTask(clazz)) {
					taskList.add(new DoFailTaskHandler(service, logService, serviceBefore));
				}
				if (hasCompleteTask(clazz)) {
					taskList.add(new DoCompleteTaskHandler(service, logService, serviceBefore));
				}
			}
			// 加入个FinalTask
			taskList.add(new DoFinalTaskHandler(logService));
		}

		isReady.set(true);
		log.info(toString());
	}

	private boolean hasServiceTask(Class clazz) {
		try {
			if (null != clazz.getDeclaredMethod("doService", Object.class, ResultVO.class)) {
				return true;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			// nothing to do
		}
		return false;
	}

	private boolean hasSuccessTask(Class clazz) {
		try {
			if (null != clazz.getDeclaredMethod("doSuccess", Object.class, ResultVO.class)) {
				return true;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			// nothing to do
		}
		return false;
	}

	private boolean hasFailTask(Class clazz) {
		try {
			if (null != clazz.getDeclaredMethod("doFail", Object.class, ResultVO.class)) {
				return true;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			// nothing to do
		}
		return false;
	}

	private boolean hasCompleteTask(Class clazz) {
		try {
			if (null != clazz.getDeclaredMethod("doComplete", Object.class, ResultVO.class)) {
				return true;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			// nothing to do
		}
		return false;
	}

	/**
	 * 把服务按顺序放进服务链
	 *
	 * @param serviceEnum
	 * @param serviceOrderValue
	 * @param service
	 * @param clazz
	 */
	private void putServiceInServicesMap(String serviceEnum, int serviceOrderValue, IService service, Class clazz, Map<String, List<IService>> servicesMap) {
		List<IService> serviceList = servicesMap.get(serviceEnum);
		if (CollectionUtils.isEmpty(serviceList)) {
			serviceList = new ArrayList<IService>();
			servicesMap.put(serviceEnum, serviceList);
		}
		if (serviceList.size() == 0) {
			serviceList.add(service);
			return;
		}
		int realIndex = 0;
		for (int index = 0; index < serviceList.size(); index++) {
			IService serviceTmp = serviceList.get(index);
			Class tmpClass = AopUtils.getTargetClass(serviceTmp);
			ServiceOrder serviceOrderTmp = (ServiceOrder) tmpClass.getAnnotation(ServiceOrder.class);

			// 不允许没设置serviceOrder
			if (null == serviceOrderTmp) {
				throw new RuntimeException(tmpClass.getName() + " must set @ServiceOrder on the class");
			}

			// 不允许有相同的serviceOrder
			if (serviceOrderValue == serviceOrderTmp.value()) {
				throw new RuntimeException(tmpClass.getName() + " and " + clazz.getName() + " have same order, is not allowed");
			} else if (serviceOrderValue < serviceOrderTmp.value()) {
				realIndex = index;
				break;
			} else {
				realIndex++;
			}
		}
		// 这样不会干掉以前的service，但是会在以前的service前插入新的service
		serviceList.add(realIndex, service);
	}

	/**
	 * 启动链路调用
	 *
	 * @return
	 */
	public void doServer(Object income, ResultVO output, String serviceEnum) {
		doServer(income, output, 0, null, serviceEnum, false);
	}

	/**
	 * 启动子链路调用
	 *
	 * @return
	 */
	public ServiceChainCallback doServerWithCallback(Object income, ResultVO output, String serviceEnum) {
		return doServerWithCallback(income, output, 0, serviceEnum);
	}

	/**
	 * 启动子链路调用
	 *
	 * @return
	 */
	public ServiceChainCallback doServerWithCallback(Object income, ResultVO output, long waitSecond, String serviceEnum) {
		return doServerWithCallback(income, output, waitSecond, null, serviceEnum);
	}

	/**
	 * 启动子链路调用
	 *
	 * @return
	 */
	public ServiceChainCallback doServerWithCallback(Object income, ResultVO output, Executor tpe, String serviceEnum) {
		return doServerWithCallback(income, output, 0, tpe, serviceEnum);
	}

	/**
	 * 启动子链路调用
	 *
	 * @return
	 */
	public ServiceChainCallback doServerWithCallback(Object income, ResultVO output, long waitSecond, Executor tpe, String serviceEnum) {
		return doServer(income, output, waitSecond, tpe, serviceEnum, true);
	}

	/**
	 * 启动链路调用
	 *
	 * @param income
	 * @param output
	 * @param serviceEnum
	 * @return
	 */
	private ServiceChainCallback doServer(Object income, ResultVO output, long waitSecond, Executor tpe, String serviceEnum, boolean isChildChain) {
		// 检查初始化
		init();
		// 获取任务列表
		List<AbstractTaskHandler> taskHandlerList = taskHandlerMap.get(serviceEnum);
		// 检查任务列表
		if (CollectionUtils.isEmpty(taskHandlerList)) {
			String msg = serviceEnum + "未能找到服务类别";
			log.error(msg);
			output.setResultCode(this.getClass(), BaseResultCodeConstants.CODE_900001);
			output.addResultMsg(msg);
			return null;
		}

		// 判断output是否已经服务过了，没服务过就设置output里面isUsed
		if (!output.getIsUsed().compareAndSet(false, true)) {
			throw new RuntimeException("ResultVO已经服务过了,请重新new对象,服务链名：" + serviceEnum);
		}

		// 塞任务队列进output对象
		output.setTaskHandlerList(taskHandlerList);
		// 塞进serviceChain
		output.setServiceChain(this);

		// 处理ssc
		ServiceChainCallback scc = null;
		// 如果是子链才需要callback
		if (isChildChain) {
			scc = new ServiceChainCallback(waitSecond);
			output.setScc(scc);
		}

		// 获取future
		ServiceFuture future = output.getFuture();

		// 正式处理任务
		if (null == future) {
			// 有线程池加入且是子链的才能异步执行
			if (null == tpe || !isChildChain || null == scc) {
				doTask(income, output);
			} else {
				// 子链callback不为空
				doSync(tpe, income, output);
			}
			return scc;
		} else {
			if (isChildChain) {
				throw new RuntimeException("子链不能用future串联");
			}
			// 如果future不为空则表明链条聚合
			future.init(this, income, output);
			return null;
		}
	}

	/**
	 * 异步执行doTask
	 *
	 * @param tpe
	 * @param income
	 * @param output
	 */
	private void doSync(Executor tpe, Object income, ResultVO output) {
		try {
			Runnable r = new DoAsyncTask(this, income, output);
			tpe.execute(r);
		} catch (RejectedExecutionException r) {
			// TODO 背压
			// 如果无法执行子链则由本线程执行
			doTask(income, output);
		}
	}

	/**
	 * 处理任务
	 *
	 * @param income
	 * @param output
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void doTask(Object income, ResultVO output) {
		// 获取当前的index
		int index = output.getServiceIndex();

		// 如果没处理完任务链则继续处理，处理完了则结束
		if (index >= output.getTaskHandlerList().size()) {
			// 结束前调用一下future
			ServiceFuture future = output.getFuture();
			if (null != future) {
				future.setLastIncome(income);
				future.setLastOutput(output);
				try {
					future.workNext();
				} catch (Throwable t) {
					// 非常严重的异常
					log.error("adam future system error occur:", t);
					output.addResultMsg("adam future system error occur:" + ExceptionUtils.getStackTrace(t));
				}
			}
			return;
		}

		// 获取当前任务
		AbstractTaskHandler tasker = (AbstractTaskHandler) output.getTaskHandlerList().get(index);

		// index滚动到下一个任务
		output.increaseServiceIndex();

		// dotask
		try {
			AbstractCallback AbstractCallback = null;
			// 这个task是不是应该做，如果不应该做就跳过, null==serviceInfo说明是finaltask
			if (null == tasker.getServiceInfo() || output.successCursor() >= tasker.getServiceInfo().getOrder()) {
				if (DoSuccessTaskHanlder.TYPE.equals(tasker.getType()) && output.success()) {// 如果成功则走success
					AbstractCallback = tasker.doTask(income, output);
				} else if (DoFailTaskHandler.TYPE.equals(tasker.getType()) && !output.success()) {// 如果失败则走fail
					AbstractCallback = tasker.doTask(income, output);
				} else if (DoServiceTaskHandler.TYPE.equals(tasker.getType()) || DoCompleteTaskHandler.TYPE.equals(tasker.getType())) {// 如果其它的正常处理
					AbstractCallback = tasker.doTask(income, output);
				} else if (DoFinalTaskHandler.TYPE.equals(tasker.getType())) {// 如果是finaltask直接运行
					AbstractCallback = tasker.doTask(income, output);
				}

				// 任务是不是该继续走
				if (output.isContinue() && DoServiceTaskHandler.TYPE.equals(tasker.getType())) {
					// 如果是的话它肯定不能是调用链任务的最后一个
					if (index + 1 < output.getTaskHandlerList().size()) {
						// 成功的游标向下走
						AbstractTaskHandler taskerNext = (AbstractTaskHandler) output.getTaskHandlerList().get(index + 1);
						// 如果是service类型不需要判空ServiceInfo
						if (DoServiceTaskHandler.TYPE.equals(taskerNext.getType())) {
							int successCursor = taskerNext.getServiceInfo().getOrder();
							if (successCursor > output.successCursor()) {
								output.setSuccessCursor(successCursor);
							}
						}
					}
				}
			}

			// 如果返回是空的话说明不用异步，则继续函数嵌套走后面
			if (null == AbstractCallback || AbstractCallback.isSyn()) {
				doTask(income, output);
				return;
			} else if (AbstractCallback.isCombiner()) {
				// 如果是combine，callback都为空情况下也和null一样处理
				CallbackCombiner combiner = (CallbackCombiner) AbstractCallback;
				if (CollectionUtils.isEmpty(combiner.getCallbacks()) || combiner.isSyn()) {
					doTask(income, output);
					return;
				} else {
					// 把东西都设置好，让callback来完成后面的工作
					AbstractCallback.setChain(this, income, output);
					return;
				}
			} else {
				// 把东西都设置好，让callback来完成后面的工作
				AbstractCallback.setChain(this, income, output);
				return;
			}

		} catch (Throwable t) {
			// 非常严重的异常
			log.error("system error occur:", t);
			// 如果是doservice的任务并且任务内部都是成功的才设置成框架的error
			if (output.success() && DoServiceTaskHandler.TYPE.equals(tasker.getType())) {
				output.setResultCode(this.getClass(), BaseResultCodeConstants.CODE_900000);
			}
			output.addResultMsg("system error occur:" + ExceptionUtils.getStackTrace(t));
		}

		return;
	}

	/**
	 * 查处理链是否已经准备好
	 */
	private void checkReady() {
		for (int i = 0; i < 20; i++) {
			if (isReady.get()) {
				return;
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					log.error("checkReady", e);
				}
			}
		}
	}

	/**
	 * 重刷
	 */
	public void reset() {
		taskHandlerMap = new ConcurrentHashMap<String, List<AbstractTaskHandler>>();
		initServiceChain();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("TaskChain [");
		sb.append(SysConstants.LINE_SEPARATOR);
		int lineLong = 80;
		int orderLong = 6;
		for (Map.Entry<String, List<AbstractTaskHandler>> entry : taskHandlerMap.entrySet()) {
			sb.append(" MAP :" + entry.getKey());
			sb.append(SysConstants.LINE_SEPARATOR);
			List<AbstractTaskHandler> taskList = entry.getValue();
			for (AbstractTaskHandler task : taskList) {
				String taskLine = "    ";
				ServiceOrder serviceOrder = null;
				String simpleName = " end ";
				if (null != task.getServiceInfo()) {
					Class serviceClass = AopUtils.getTargetClass(task.getServiceInfo().getService());
					simpleName = serviceClass.getSimpleName();
					serviceOrder = (ServiceOrder) serviceClass.getAnnotation(ServiceOrder.class);
				}

				if (null != serviceOrder) {
					String orderStr = taskLine + serviceOrder.value();
					if (orderStr.length() < orderLong) {
						for (int spaceIndex = 0; spaceIndex < (orderLong - orderStr.length()); spaceIndex++) {
							orderStr = orderStr + " ";
						}
					}
					taskLine = taskLine + orderStr + "  ";
				}
				taskLine = taskLine + simpleName + ":" + task.getType();
				sb.append(taskLine);
				if (taskLine.length() < lineLong) {
					for (int spaceIndex = 0; spaceIndex < (lineLong - taskLine.length()); spaceIndex++) {
						sb.append(" ");
					}
				}
				sb.append("(" + simpleName + ":" + task.getType() + ")");
				sb.append(SysConstants.LINE_SEPARATOR);
			}
		}
		sb.append("]");
		return sb.toString();
	}

}
