package com.pit.service.orchestration;

import com.pit.core.thread.PitThreadInfo;
import com.pit.core.thread.PitThreadLocalHolder;
import com.pit.service.orchestration.bean.ResultVO;
import com.pit.service.orchestration.callback.CallbackCombiner;
import com.pit.service.orchestration.callback.ICallbackSender;
import com.pit.service.orchestration.chain.ServiceChain;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
public abstract class AbstractCallback<ResultType, ErrorType extends Throwable, IncomeType, OutputType> {
    /**
     * 是否组合
     */
    protected boolean isCombiner = false;

    protected final static int SUCCESS_CODE = 0;

    protected final static int ERROR_CODE = 1;

    protected final static int COMPLETE_CODE = 2;

    protected volatile ServiceChain serviceChain;

    protected volatile IncomeType income;

    protected volatile ResultVO<OutputType> output;

    protected volatile ICallbackSender sender;

    /**
     * 有没做过一次Complete method
     */
    protected volatile boolean isDualComplete = false;

    /**
     * 是否已经完成callback的内容
     */
    protected volatile boolean isDone = false;

    /**
     * 是否已经切换过线程,因为success或fail已经切换过线程的情况下，complete就没必要再切换一次
     */
    protected volatile boolean isSwitched = false;

    /**
     * 如果是fastReturn，则只要有一个callback回调就进行下一步操作，如果为false则全部callback回来再下一步操作
     */
    protected volatile boolean fastReturn = false;

    /**
     * 如果fastReturn模式，isDoneFirst表示是否已经有第一个callback已经完成了
     */
    protected AtomicBoolean isDoneFirst;

    /**
     * 切换的线程池
     */
    protected volatile Executor tpe;

    /**
     * 后备切换的线程池
     */
    protected volatile Executor tpeBak;

    /**
     * call back合并器
     */
    protected volatile CallbackCombiner<IncomeType, OutputType> combiner;

    /**
     * 防止后面步骤还没走完，ServiceChain和income还有output还没注入进来就跑掉了
     */
    protected final CountDownLatch latch = new CountDownLatch(1);

    /**
     * 母线程ID，发送请求的线程ID
     */
    protected volatile long parentThreadId;

    /**
     * 触发callback的线程ID
     */
    protected volatile long triggerThreadId = 0;

    /**
     * countdownLatch wait time (second), 默认10分钟超时时间
     */
    protected volatile long waitTime = 600;

    /**
     * 线程专用
     */
    protected PitThreadInfo threadHolder = new PitThreadInfo();


    public AbstractCallback(long parentThreadId) {
        super();
        this.parentThreadId = parentThreadId;
        setThreadHolder(PitThreadLocalHolder.getThreadHolder());
    }

    public AbstractCallback(long parentThreadId, long waitTime) {
        super();
        this.parentThreadId = parentThreadId;
        if (0 != waitTime) {
            this.waitTime = waitTime;
        }
        setThreadHolder(PitThreadLocalHolder.getThreadHolder());
    }

    public void setThreadHolder(PitThreadInfo threadHolder) {
        this.threadHolder.copy(threadHolder);
    }

    /**
     * 处理成功
     *
     * @param result
     */
    public abstract void dealSuccess(ResultType result);

    /**
     * 处理失败
     *
     * @param e
     */
    public abstract void dealFail(ErrorType e);

    /**
     * 处理完成
     *
     * @param result
     * @param e
     */
    public abstract void dealComplete(ResultType result, ErrorType e);

    /**
     * 异常处理
     *
     * @param t
     */
    public abstract void dealException(Throwable t);

    public void onSuccess(ResultType result) {
        onDoIt(result, null, SUCCESS_CODE);
    }

    public void onFail(ErrorType e) {
        onDoIt(null, e, ERROR_CODE);
    }

    /**
     * 当前线程/独立开线程去执行callback
     *
     * @param result
     * @param e
     * @param type
     */
    protected void onDoIt(ResultType result, ErrorType e, int type) {
        setTriggerThreadId();
        PitThreadLocalHolder.setThreadHolder(threadHolder);
        if (null != sender && needResend(result, e, type)) {
            sender.doSend(this);
            return;
        }

        // 切换线程
        if (null == this.tpe || true == this.isSwitched) {
            doit(result, e, type);
        } else {
            try {
                this.isSwitched = true;
                this.tpe.execute(() -> {
                    PitThreadLocalHolder.setThreadHolder(threadHolder);
                    doit(result, e, type);
                });
            } catch (RejectedExecutionException r) {
                // TODO 是否执行
            } catch (Throwable t) {
                dealException(t);
            }
        }
    }

    /**
     * 初次设置触发的threadId
     */
    private void setTriggerThreadId() {
        if (0 == triggerThreadId) {
            triggerThreadId = Thread.currentThread().getId();
        }
    }

    /**
     * 如果需要重发则@Override这个方法
     *
     * @param result
     * @param e
     * @param type
     * @return
     */
    public boolean needResend(ResultType result, ErrorType e, int type) {
        return false;
    }

    /**
     * 执行
     *
     * @param result
     * @param e
     * @param type
     */
    private void doit(ResultType result, ErrorType e, int type) {
        try {
            loopWaitChain();
            switch (type) {
                case SUCCESS_CODE:
                    dealSuccess(result);
                    break;
                case ERROR_CODE:
                    dealFail(e);
                    break;
                case COMPLETE_CODE:
                    // 如果已经进行过一次完成method，就无需再做一次了
                    if (isDualComplete) {
                        return;
                    }
                    isDualComplete = true;
                    dealComplete(result, e);
                    break;
            }
        } catch (Throwable t) {
            // TODO 加入背压
            dealException(t);
        } finally {
            // 如果是complete就没必要再onComplete, complete完了就workNext
            if (SUCCESS_CODE == type || ERROR_CODE == type) {
                onComplete(result, e);
            } else {
                isDone = true;
                workNext();
            }
        }
    }

    /**
     * 防止返回太快，还没执行完回调函数就回调了
     */
    protected void loopWaitChain() {
        // 如果触发线程和母线程是同一个的就说明是串行的，没必要等了
        if (triggerThreadId == parentThreadId) {
            return;
        }

        try {
            if (!latch.await(waitTime, TimeUnit.SECONDS)) {
                throw new RuntimeException("callback wait service chain timeout:" + this.getClass().getName() + " for time:" + waitTime + " seconds.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("callback error can not wait service chain and output", e);
        }
    }

    /**
     * 触发执行下一个任务
     */
    private void workNext() {
        // 如果有combiner则由combiner去完成后面步骤
        if (null != this.combiner) {
            // 如果是非fastReturn模式，或者是fastReturn模式且第一次的就继续做了
            if (!fastReturn || isDoneFirst.compareAndSet(false, true)) {
                this.combiner.onComplete(null, null);
                return;
            } else {
                // 是fastReturn模式，已经不是第一次完成则不继续做了
                return;
            }
        }

        // 没有combiner则自己完成
        if (null == serviceChain || null == output) {
            return;
        }
        serviceChain.doTask(income, output);
    }

    public void onComplete(ResultType result, ErrorType e) {
        onDoIt(result, e, COMPLETE_CODE);
    }

    public void setExecutor(Executor tpe) {
        this.tpe = tpe;
    }

    public Executor getExecutor() {
        return this.tpe;
    }

    /**
     * 塞进service chain income output
     *
     * @param serviceChain
     * @param income
     * @param output
     */
    public void setChain(ServiceChain serviceChain, IncomeType income, ResultVO<OutputType> output) {
        this.serviceChain = serviceChain;
        this.income = income;
        this.output = output;
        latch.countDown();
    }

    public boolean isSyn() {
        return triggerThreadId == parentThreadId;
    }

    public boolean isDone() {
        return isDone;
    }

    public CallbackCombiner getCombiner() {
        return combiner;
    }

    public void setCombiner(CallbackCombiner combiner) {
        this.combiner = combiner;
    }

    public boolean isCombiner() {
        return isCombiner;
    }


}
