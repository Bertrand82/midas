package bg.panama.btc.trading.first;

import bg.panama.btc.model.Balances;
import bg.panama.btc.model.v2.Ticker;
import bg.panama.btc.model.v2.Tickers;

public class ServiceCurrencies {

	
	private Tickers tickersCurrent;
	private SessionCurrencies sessionCurrencies;
	private Balances balances;
	
	private static ServiceCurrencies instance = new ServiceCurrencies();

	public static ServiceCurrencies getInstance() {
		return instance;
	}

	private ServiceCurrencies() {
		super();
	}

	public Tickers getTickersCurrent() {
		return tickersCurrent;
	}

	public void setTickersCurrent(Tickers tickersCurrent) {
		this.tickersCurrent = tickersCurrent;
	}
	
	public double getPriceInDollar(String currency) {
		if (this.tickersCurrent == null){
			return 0d;
		}
		Ticker t = tickersCurrent.getTickerByName("t"+currency+"usd");
		if (t == null){
			if (currency.equalsIgnoreCase("usd")){
				return 1.0;
			}else {
				System.err.println("No Ticker- for >>>"+currency);
			}
			return 0;
		}
		return t.getLastPrice();
	}

	public SessionCurrencies getSessionCurrencies() {
		return sessionCurrencies;
	}

	public void setSessionCurrencies(SessionCurrencies sessionCurrencies) {
		this.sessionCurrencies = sessionCurrencies;
	}

	public Balances getBalances() {
		return balances;
	}

	public void setBalances(Balances balances) {
		this.balances = balances;
	}

}
