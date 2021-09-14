package com.pit.service.orchestration.callback;

import com.pit.service.orchestration.AbstractCallback;
import com.pit.service.orchestration.bean.ResultVO;
import com.pit.service.orchestration.chain.ServiceChain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/16.
 * @description:
 */
public class CallbackCombiner<IncomeType, OutputType> extends AbstractCallback<Object, Throwable, IncomeType, OutputType> {
    private final List<AbstractCallback> callbacks = new ArrayList<>();
    private AtomicBoolean doneOnce = new AtomicBoolean(false);

    public CallbackCombiner() {
        super(Thread.currentThread().getId());
        this.isCombiner = true;
    }

    public CallbackCombiner(boolean fastReturn) {
        super(Thread.currentThread().getId());
        this.fastReturn = fastReturn;
        this.isCombiner = true;
    }

    public CallbackCombiner(Executor tpe) {
        super(Thread.currentThread().getId());
        this.tpe = tpe;
        this.isCombiner = true;
    }

    public CallbackCombiner(Executor tpe, boolean fastReturn) {
        super(Thread.currentThread().getId());
        this.tpe = tpe;
        this.isCombiner = true;
        this.fastReturn = fastReturn;
        // 是fastReturn模式则初始化isDoneFirst
        if (fastReturn) {
            this.isDoneFirst = new AtomicBoolean(false);
        }
    }

    public void combine(AbstractCallback<Object, Throwable, IncomeType, OutputType> callback) {
        if (null == callback) {
            return;
        }
        if (callback.isCombiner()) {
            throw new RuntimeException("combiner can not combine a combiner");
        }

        callback.setCombiner(this);
        this.callbacks.add(callback);
    }

    /**
     * 塞进service chain income output
     *
     * @param serviceChain
     * @param income
     * @param output
     */
    @Override
    public void setChain(ServiceChain serviceChain, IncomeType income, ResultVO<OutputType> output) {
        this.serviceChain = serviceChain;
        this.income = income;
        this.output = output;
        for (AbstractCallback absCallback : this.callbacks) {
            absCallback.setChain(serviceChain, income, output);
        }
        latch.countDown();
    }

    @Override
    public void onComplete(Object result, Throwable e) {
        // 如果是fastReturn则不需要全部做完，能进这里说明至少有一个已经做完了的，非fastReturn才要全部做完
        if (!fastReturn) {
            // 允许多次调用onComplete
            for (AbstractCallback absCallback : this.callbacks) {
                // 如果有一个callback没做完就不继续做下去
                if (!absCallback.isDone()) {
                    return;
                }
            }
        }
        // 是否第一次完成CallbackCombiner，以防两个callback并发触发完成CallbackCombiner
        boolean hasDoneOnce = doneOnce.compareAndSet(false, true);
        // 第一次完成都需要往下走
        if (hasDoneOnce) {
            onDoIt(null, e, COMPLETE_CODE);
        }
    }

    @Override
    public boolean isSyn() {
        for (AbstractCallback AbstractCallback : this.callbacks) {
            // 如果有一个callback是异步的那说明这个combiner就是异步的
            if (!AbstractCallback.isSyn()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void dealSuccess(Object result) {
        // nothing to do
    }

    @Override
    public void dealFail(Throwable e) {
        // nothing to do
    }

    @Override
    public void dealComplete(Object result, Throwable e) {
        // nothing to do
    }

    @Override
    public void dealException(Throwable t) {
        // nothing to do
    }

    public List<AbstractCallback> getCallbacks() {
        return callbacks;
    }
}
