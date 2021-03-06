package bg.panama.btc.trading.first;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bg.panama.btc.OrderFactory;
import bg.panama.btc.model.ActiveOrder;
import bg.panama.btc.model.ActiveOrders;
import bg.panama.btc.model.Balance;
import bg.panama.btc.model.Balances;
import bg.panama.btc.model.operation.OperationsManager;
import bg.panama.btc.model.operation.Order;
import bg.panama.btc.model.v2.ITicker;
import bg.panama.btc.model.v2.Ticker;
import bg.panama.btc.model.v2.Tickers;


@Entity
public class SessionCurrencies implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger loggerPanic= LogManager.getLogger("panic");
	
	@Id
	@GeneratedValue
	private long id;

	private final Date timeStart = new Date();
	
	
	
	@Embedded
	private AmbianceMarket ambianceMarket = new AmbianceMarket();
	/**
	 * Utile pour faire des tris
	 */
	private Date date = new Date();
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL,mappedBy="sessionCurrencies")
	List<SessionCurrency> lSessionCurrency = new ArrayList<SessionCurrency>();
	private int numero =0;
	
	public SessionCurrencies() {
		
	}
	
	public SessionCurrencies(Tickers tickers) {
		super();
		for (ITicker ticker : tickers.getlTickers()) {
			SessionCurrency z_1_Currency = new SessionCurrency((Ticker) ticker,this);
			lSessionCurrency.add(z_1_Currency);
		}		
	}

	public void update(Tickers tickers) {
		numero++;
		for (Ticker ticker : tickers.getlTickers()) {
			SessionCurrency sessionCurrency = getSessionCurrency_byName(ticker.getName());
			if (sessionCurrency== null){
				sessionCurrency = new SessionCurrency(ticker, this);
				this.lSessionCurrency.add(sessionCurrency);
			}else {
				sessionCurrency.update((Ticker) ticker);
			}
		}
		processModePanic();
	}

	public SessionCurrency getSessionCurrency_byName(String name) {
		for (SessionCurrency z : lSessionCurrency) {
			if (name.equalsIgnoreCase(z.getName())) {
				return z;
			}
		}
		return null;
	}
	public SessionCurrency getSessionCurrency_byShortName(String name) {
		for (SessionCurrency z : lSessionCurrency) {
			if (name.equalsIgnoreCase(z.getShortName())) {
				return z;
			}
		}
		return null;
	}

	public List<SessionCurrency> getListOrder(Comparator<SessionCurrency> comparator) {
		Collections.sort(lSessionCurrency, comparator);
		return lSessionCurrency;

	}

	public static final Comparator<SessionCurrency> comparatorDailyChangePercent = new Comparator<SessionCurrency>() {

		@Override
		public int compare(SessionCurrency o1, SessionCurrency o2) {
			Double d = o2.getHourlyChangePerCentByDay();
			return d.compareTo(o1.getHourlyChangePerCentByDay());
		}
	};
	
	public static final Comparator<SessionCurrency> comparatorHourlyChangePercentByDay = new Comparator<SessionCurrency>() {

		@Override
		public int compare(SessionCurrency o1, SessionCurrency o2) {
			Double d = o2.getHourlyChangePerCentByDay();
			return d.compareTo(o1.getHourlyChangePerCentByDay());
		}
	};
	public List<SessionCurrency> getListOrder_byDailyChangePerCent__DEPRECATED() {
		List<SessionCurrency> listNew = new ArrayList<SessionCurrency>();
		listNew.addAll(lSessionCurrency);
		Collections.sort(listNew, comparatorDailyChangePercent);
		return listNew;

	}
	
	
	

	public List<SessionCurrency> getListOrder_byHourlyChangePerCentByDay() {
		List<SessionCurrency> listNew = new ArrayList<SessionCurrency>();
		listNew.addAll(lSessionCurrency);
		Collections.sort(listNew, comparatorHourlyChangePercentByDay);
		return listNew;

	}

	public synchronized SessionCurrency getTickerBest() {
		SessionCurrency z = getListOrder_byHourlyChangePerCentByDay().get(0);
		return z;
	}

	public synchronized SessionCurrency getTickerWorse() {
		SessionCurrency z = getListOrder_byHourlyChangePerCentByDay().get(lSessionCurrency.size() - 1);
		return z;
	}

	public double getLastPrice(String currency) {
		for (SessionCurrency zcurrency : this.lSessionCurrency) {
			if (zcurrency.getShortName().equalsIgnoreCase(currency)) {
				double lastPrice = zcurrency.getLastPrice();
				return lastPrice;
			}
		}
		return 0;
	}

	public double getHourlyChangePerCentByDay(String currency) {
		for (SessionCurrency zcurrency : this.lSessionCurrency) {
			if (zcurrency.getShortName().equalsIgnoreCase(currency)) {
				double daylyChangePerCent = zcurrency.getHourlyChangePerCentByDay();
				return daylyChangePerCent;
			}
		}
		return 0;
	}

	public List<SessionCurrency> getlSessionCurrency() {
		return lSessionCurrency;
	}


	public SessionCurrency getTickerByCurrency(String currency) {
		for (SessionCurrency zcurrency : this.lSessionCurrency) {
			if (zcurrency.getShortName().equalsIgnoreCase(currency)) {

				return zcurrency;
			}
		}
		return null;
	}
    String  sessionCurrencyBestEligible;
    
	public String getSessionCurrencyBestEligible() {
		return sessionCurrencyBestEligible;
	}

	
	public SessionCurrency getBestEligible() {
		List<SessionCurrency> list = this.getListOrder_byHourlyChangePerCentByDay();
		// Liste ordonnée les premieres sont lthe best ... si elles sont eligibles
		
		for (int i = 0; i < list.size(); i++) {
			
			SessionCurrency sc = (SessionCurrency) list.get(i);
			//Balance b = balancesCurrent.getBalance(sc.getShortName());
			if (sc.isEligible()) {
				this.sessionCurrencyBestEligible =sc.getShortName();
				if (sc.getEtatStochastique_10mn().acheter){
				
					return sc;
				}
				
			}else {
				System.err.println("getBestEligible No elligible :"+sc.getShortName());
			}
		}
		System.err.println("getBestEligible Pas de currency eligible");
		return null;
	}

	public void saveAllInDollar_(Balances balancesCurrent) {
		System.err.println("Save All In Dollar start");	
		OperationsManager.instance.emergencySaveInDollar("");
	}

	public void saveConfiguration_______________DEPRECATED() {
		System.err.println("saveConfiguration");
	}

	public void updateWithArchive_____DEPRECATED(SessionCurrencies sessionArchive) {
		for(SessionCurrency sc : this.lSessionCurrency){
			String name = sc.getName();
			SessionCurrency sArchive = sessionArchive.getSessionCurrency_byName(name);
			sc.updateWithArchive__DEPRECATED(sArchive);
		}
	}

	public int getNumero() {
		return numero;
	}

	public Date getTimeStart() {
		return timeStart;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		
		SessionCurrencies s = new SessionCurrencies();
		s.numero = numero;
		s.sessionCurrencyBestEligible = sessionCurrencyBestEligible;
		s.ambianceMarket = (AmbianceMarket) ambianceMarket.clone();
		for(SessionCurrency sc : lSessionCurrency){
			SessionCurrency sc2 =(SessionCurrency) sc.clone();
			s.lSessionCurrency.add(sc2);
			sc2.setSessionCurrencies(s);
		}		
		return s;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	private void processModePanic() {
		int nNegative=0;
		int nPositive=0;
		int nNegativeMoyenne=0;
		int nPositiveMoyenne=0;
		int nStochasAcheter_10mn =0;
		int nStochasVendre_10mn =0;
		int nCurrencies =0;
		for(SessionCurrency sc : this.lSessionCurrency){
			nCurrencies =0;
			if (sc.getTicker_Z_1().getHourlyChangePerCent() > 0) {
				nPositive ++;
			}else {
				nNegative++;
			}
			if (sc.getHourlyChangePerCentByDayInstant() > 0){
				nPositiveMoyenne ++;
			}else {
				nNegativeMoyenne++;
			}
			SessionCurrency.EtatSTOCHASTIQUE etat_10mn = SessionCurrency.getStochastique(sc.getStochastique_10mn());
			if (etat_10mn.vendre){
				nStochasVendre_10mn++;
			}else {
				nStochasAcheter_10mn++;
			}
		}
		boolean isPanic = false;
		String causePanicStr ="";
		if (nPositive == 0){
			isPanic = true;;
			causePanicStr+="nbPositive == 0;";
		}
		if (nPositiveMoyenne == 0){
			isPanic = true;
			causePanicStr+="nPositiveMoyenne == 0;";
		}
		if (nStochasAcheter_10mn ==0){
			isPanic =true;
			causePanicStr+="nStochasAcheter_10mn == 0;";
		}
		if (this.ambianceMarket == null){
			this.ambianceMarket = new AmbianceMarket();
		}
		this.ambianceMarket.setModePanic(isPanic); 
		this.ambianceMarket.setCausePanic(causePanicStr);
		String trace = ambianceMarket+"| nCurrencies :"+nCurrencies+"| nPositive :"+nPositive+" |  nNegative : "+nNegative+"| nPositiveMoyenne :"+ nPositiveMoyenne+"| nNegativeMoyenne :"+nNegativeMoyenne+"| nStochasAcheter_10mn :"+nStochasAcheter_10mn+"| nStochasVendre_10mn :"+nStochasVendre_10mn;
		if (isModePanic()){
			System.err.println(trace);
		}
		loggerPanic.info(trace);
		
	}

	public boolean isModePanic() {
		if (this.ambianceMarket == null){
			System.err.println("ambianceMarket is nulll !!! Should never happen");
			return true;
		}
		return this.ambianceMarket.isModePanic();
	}

	

	public AmbianceMarket getAmbianceMarket() {
		return ambianceMarket;
	}

	public void setAmbianceMarket(AmbianceMarket ambianceMarket) {
		this.ambianceMarket = ambianceMarket;
	}

	public void checkActiveOrders() {
		System.err.println("checkActiveOrders");
		ActiveOrders activeOrders= OrderFactory.getInstance().getAllActivesOrders();
		
		System.out.println("checkActiveOrders :::::"+activeOrders);
		for(ActiveOrder ao : activeOrders.getlOrders()){
			System.out.println("------> ao :::::"+ao);
			if (ao.getIs_live()){
				if (ao.getSide().equalsIgnoreCase("buy")){
					checkActiveOrderAchat(ao);
				}
			}
		}
	}

	private void checkActiveOrderAchat(ActiveOrder ao) {
		String shortName = ao.getSymbol().substring(0,3);
		SessionCurrency currency = getSessionCurrency_byShortName(shortName);
		if (currency.isEligible()){
			System.err.println("Currency is Elligible NO IMPLEMENTED YET");
		}else {
			System.err.println("Currency is no more Elligible NO IMPLEMENTED YET");
		}
	}

	

}
