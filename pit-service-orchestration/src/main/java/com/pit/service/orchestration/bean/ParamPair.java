package com.pit.service.orchestration.bean;

/**
 * @author gy
 */
public class ParamPair {

	private Object income;

	private ResultVO output;

	private int status = 0;

	public ParamPair(Object income, ResultVO output) {
		super();
		this.income = income;
		this.output = output;
	}

	public Object getIncome() {
		return income;
	}

	public void setIncome(Object income) {
		this.income = income;
	}

	public ResultVO getOutput() {
		return output;
	}

	public void setOutput(ResultVO output) {
		this.output = output;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
