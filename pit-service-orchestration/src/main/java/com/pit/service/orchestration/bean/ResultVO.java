package com.pit.service.orchestration.bean;

import com.pit.core.json.GsonUtils;
import com.pit.service.orchestration.AbstractTaskHandler;
import com.pit.service.orchestration.ServiceFuture;
import com.pit.service.orchestration.annotation.ServiceErrorCode;
import com.pit.service.orchestration.callback.ServiceChainCallback;
import com.pit.service.orchestration.chain.ServiceChain;
import com.pit.service.orchestration.constants.BaseResultCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
@Slf4j
public class ResultVO<T> implements Serializable {
    /**
     * 返回码
     */
    private String resultCode = "0";

    /**
     * 返回信息
     */
    private String resultMsg = "";

    private Integer serviceIndex;

    private transient List<AbstractTaskHandler> taskHandlerList;

    private transient int successCursor;

    private transient ServiceChain serviceChain;

    private transient String latestServiceName;

    private transient AbstractTaskHandler currentTaskHandler;

    private transient ServiceFuture future;

    private transient ServiceChainCallback scc;

    private AtomicBoolean isAsyn = new AtomicBoolean(false);

    private AtomicBoolean isUsed = new AtomicBoolean(false);

    private T data;

    /**
     * 复制ResultVO
     *
     * @param origin
     */
    public void copyResult(ResultVO origin) {
        copyResult(origin, null);
    }

    /**
     * 复制ResultVO
     *
     * @param origin
     * @param defaultData
     */
    public void copyResult(ResultVO origin, T defaultData) {
        this.resultCode = origin.getResultCode();
        this.addResultMsg(origin.getResultMsg());
        if (null != defaultData) {
            if (null != origin.getData()) {
                this.data = defaultData;
                BeanUtils.copyProperties(origin.getData(), this.data);
                if (!origin.getData().equals(this.data)) {
                    this.data = (T) origin.getData();
                }
            } else {
                this.data = defaultData;
            }
        }
    }

    /**
     * 复制ResultVO
     *
     * @param thisClass
     * @param thisResultCode
     * @param thisMessage
     * @param origin
     * @param defaultData
     */
    public void copyResult(Class<? extends Object> thisClass, String thisResultCode, String thisMessage, ResultVO origin,
                           T defaultData) {
        this.setResultCode(thisClass, thisResultCode + origin.getResultCode());
        this.addResultMsg(origin.getResultMsg());
        if (null != defaultData) {
            if (null != origin.getData()) {
                this.data = defaultData;
                BeanUtils.copyProperties(origin.getData(), this.data);
                if (!origin.getData().equals(this.data)) {
                    this.data = (T) origin.getData();
                }
            } else {
                this.data = defaultData;
            }
        }
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public void setResultCode(Class<? extends Object> thisClass, String resultCode) {
        if (BaseResultCodeConstants.CODE_SUCCESS.equals(resultCode)
                || BaseResultCodeConstants.CODE_SUCCESS_AND_BREAK.equals(resultCode)) {
            setResultCode(resultCode);
            return;
        }
        ServiceErrorCode errorCode = thisClass.getAnnotation(ServiceErrorCode.class);
        if (null != errorCode && StringUtils.isNotBlank(errorCode.value())) {
            if (BaseResultCodeConstants.CODE_NOT_SUPPORT.equals(errorCode.value())) {
                return;
            }
            if (!resultCode.startsWith(errorCode.value()) && !success()
                    && !resultCode.startsWith(BaseResultCodeConstants.CODE_ERROR_BUT_CONTINUE)) {
                if (!ServiceChain.class.equals(thisClass)) {
                    log.warn(resultCode + "错误代码要以" + errorCode.value() + "开头");
                }
            }
        }
        setResultCode(resultCode);
    }

    public boolean success() {
        if (BaseResultCodeConstants.CODE_SUCCESS.equals(resultCode)
                || BaseResultCodeConstants.CODE_SUCCESS_AND_BREAK.equals(resultCode)) {
            return true;
        }
        return false;
    }

    /**
     * 是否继续
     *
     * @return
     */
    public boolean isContinue() {
        if (BaseResultCodeConstants.CODE_SUCCESS.equals(resultCode)
                || BaseResultCodeConstants.CODE_ERROR_BUT_CONTINUE.equals(resultCode)) {
            return true;
        }
        return false;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    /**
     * 消息截取
     *
     * @param length
     * @return
     */
    public String getSimpleResultMsg(int length) {
        if (StringUtils.isBlank(resultMsg)) {
            return resultMsg;
        }
        int index = resultMsg.indexOf(":");
        if (index > 0) {
            length = index;
        }
        String tmp = resultMsg.substring(0, Math.min(length, resultMsg.length()));
        return tmp;
    }

    /**
     * 添加结果消息
     *
     * @param resultMsg
     */
    public void addResultMsg(String resultMsg) {
        appendOrOverwriteResultMsg(resultMsg, true);
    }

    public void appendOrOverwriteResultMsg(String resultMsg, boolean isAppend) {
        if (isAppend) {
            if (StringUtils.isBlank(resultMsg)) {
                return;
            }
            if (StringUtils.isBlank(this.resultMsg)) {
                this.resultMsg = resultMsg;
            } else {
                this.resultMsg = resultMsg + " || " + this.resultMsg;
            }
        } else {
            this.resultMsg = resultMsg;
        }
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String latestServiceName() {
        return latestServiceName;
    }

    public void setLatestServiceName(String latestServiceName) {
        this.latestServiceName = latestServiceName;
    }

    @Override
    public String toString() {
        return "ResultVO [resultCode=" + resultCode + ", resultMsg=" + resultMsg + ", data=" + GsonUtils.toJson(data) + "]";
    }

    public boolean finished() {
        return serviceIndex >= taskHandlerList.size();
    }

    public int increaseServiceIndex() {
        return ++serviceIndex;
    }

    public int getNextServiceIndex() {
        return serviceIndex;
    }

    public int getServiceIndex() {
        return serviceIndex;
    }

    public void setServiceIndex(int serviceIndex) {
        this.serviceIndex = serviceIndex;
    }

    public List<AbstractTaskHandler> getTaskHandlerList() {
        return taskHandlerList;
    }

    public void setTaskHandlerList(List<AbstractTaskHandler> taskHandlerList) {
        // 初始化successCursor
        if (null != taskHandlerList.get(0).getServiceInfo()) {
            this.successCursor = taskHandlerList.get(0).getServiceInfo().getOrder();
        }
        this.taskHandlerList = taskHandlerList;
    }

    public int successCursor() {
        return successCursor;
    }

    public void setSuccessCursor(int successCursor) {
        this.successCursor = successCursor;
    }

    public ServiceChain serviceChain() {
        return serviceChain;
    }

    public void setServiceChain(ServiceChain serviceChain) {
        this.serviceChain = serviceChain;
    }

    public AbstractTaskHandler currentTaskHandler() {
        return currentTaskHandler;
    }

    public void setCurrentTaskHandler(AbstractTaskHandler currentTaskHandler) {
        this.currentTaskHandler = currentTaskHandler;
    }

    public ServiceFuture getFuture() {
        return future;
    }

    public void setFuture(ServiceFuture future) {
        this.future = future;
    }

    public ServiceChainCallback getScc() {
        return scc;
    }

    public void setScc(ServiceChainCallback scc) {
        this.scc = scc;
    }

    public AtomicBoolean getIsUsed() {
        return isUsed;
    }

    public boolean isAsyn() {
        return isAsyn.get();
    }

    public void setAsyn(boolean isAsyn) {
        this.isAsyn.set(isAsyn);
    }
}
