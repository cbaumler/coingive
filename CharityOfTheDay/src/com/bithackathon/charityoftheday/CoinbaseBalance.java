package com.bithackathon.charityoftheday;

public class CoinbaseBalance {
	 
	 private double balanceBtc;
	 private double balanceUsd;
	 private boolean valid;

	public double getBalanceBtc() {
		return balanceBtc;
	}

	public void setBalanceBtc(double balanceBtc) {
		this.balanceBtc = balanceBtc;
	}

	public double getBalanceUsd() {
		return balanceUsd;
	}

	public void setBalanceUsd(double balanceUsd) {
		this.balanceUsd = balanceUsd;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
