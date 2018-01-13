package bg.panama.btc.trading.first;



import java.text.DecimalFormat;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bg.panama.btc.BitfinexClient;
import bg.panama.btc.OrderManager;
import bg.panama.btc.BitfinexClient.EnumService;
import bg.panama.btc.model.ActiveOrder;
import bg.panama.btc.model.ActiveOrders;
import bg.panama.btc.model.Balances;
import bg.panama.btc.model.v2.ITicker;
import bg.panama.btc.model.v2.Tickers;
import bg.panama.btc.model.v2.TickersFactory;
import bg.panama.btc.swing.MidasGUI;
import bg.panama.btc.swing.ConfigFileProtected;
import bg.panama.btc.swing.SymbolsConfig;



public class ThreadBalance implements Runnable{

	private static final Logger loggerTrade= LogManager.getLogger("trade");
	private static final Logger loggerTradeComparaison= LogManager.getLogger("tradeComparaison");
	private static final Logger loggerTradeBalance= LogManager.getLogger("tradeBalance");
	
	private long timeSleeping = 60l * 1000l;
	private Thread t = new Thread(this);
	private String symbolsCurrenciesSelected;
	boolean isOn = true;
	private BitfinexClient bitfinexClient;
	private Config config;
	
	public ThreadBalance(Config config)  {	
		this.config = config;
		try {			
			this.symbolsCurrenciesSelected = SymbolsConfig.getInstance().getSymbolsSelectedRequest();
			String pwd = config.getPassword();
			ConfigFileProtected pcf  = new ConfigFileProtected(pwd);
			String key = pcf.get(ConfigFileProtected.keyBifinexApiKey);
			String keySecret = pcf.get(ConfigFileProtected.keyBitfinexSecretKey);
			bitfinexClient= new BitfinexClient(key, keySecret);
			t.setName("BalanceTh");
			t.setDaemon(true);
			t.start();
			
		} catch (Exception e) {
			isOn = false;
			this.timeSleeping=1l;
			awake();
			e.printStackTrace();
		}
	}
	private Balances balances_;
	TickersFactory tickersFactory = TickersFactory.getInstance();
	public void run() {
			System.err.println("ThreadBalance Start");
			loggerTrade.info( "Thread Balance");
			while(isOn ){
				try {
					Balances balances_ = fetchBalances();
					
					System.err.println("ThreadBalance balances "+balances_);
					//List<Order> orders = balances.process(sessionCurrencies);
					if(this.config.isOrderAble()){
						//OrderManager.getInstance().sendOrders(this.bitfinexClient,orders);
					}
					//this.sessionCurrencies.setBalancesCurrent(balances);
					MidasGUI.getInstance().updateThreadBalance(balances_);
					
				} catch (Exception e) {
					log("Exception22: "+e.getClass()+" "+e.getMessage());
					System.err.println("Exception22 "+e.getClass()+" "+e.getMessage());
					e.printStackTrace();
				} catch (Throwable e) {
					log("Exception23: "+e.getClass()+" "+e.getMessage());
					e.printStackTrace();
				}
				sleep(timeSleeping);
			}
			log( "Thread balance stopped");
	}
	
	



	
	
	

	private static DecimalFormat decimalFormat = new DecimalFormat("0.000");
	

	
	
	private Balances fetchBalances() throws Exception{
		return   (Balances) bitfinexClient.serviceProcess(EnumService.balances,"",null);
	}
	
	public void stop(String from){
		log( "stop request from "+from);
		this.isOn =false;
		this.awake();
	}
	public synchronized void awake() {
		notifyAll();
	}
	
	private synchronized void sleep(long time){
		try {			
			wait(time);			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void log(String s){
		loggerTrade.info( s);
		loggerTradeComparaison.info(s);
	}

	
	
	public Config getConfig() {
		return config;
	}



	
}