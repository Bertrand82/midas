package bg.panama.btc.swing;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bg.panama.btc.model.Balance;
import bg.panama.btc.model.v2.Ticker;
import bg.panama.btc.trading.commun.Value;
import bg.panama.btc.trading.first.BalancesFactory;
import bg.panama.btc.trading.first.SessionCurrenciesFactory;
import bg.panama.btc.trading.first.SessionCurrency;
import bg.util.PointDouble;
import bg.util.PointTriple;

public class History implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int SIZE_MAX = 120;

	private List<SessionCurrency> listSessionCurrency_ = null;
	private List<Balance> listBalances = null;
	private SessionCurrency sessionCurrency;
	private String name;

	public History(SessionCurrency sessionCurrency) {
		this.sessionCurrency = sessionCurrency;
		this.name = sessionCurrency.getName();
	}

	public List<PointDouble> getListPointsVariation() {
		List<PointDouble> list = new ArrayList<>();
		for (SessionCurrency s : getListSessionCurrency()) {
			double chd = s.getHourlyChangePerCentByDay();
			if ((chd - SessionCurrency.D_default) < 0.000001) {
				chd = 0;
			} else if (chd > 10) {
				chd = 10.0;
			} else if (chd < -10) {
				chd = -10.0;
			}
			PointDouble p = new PointDouble(s.getDateLastUpdateAsLong(), chd);
			list.add(p);
		}

		return list;
	}

	public List<PointDouble> getListPointsPrixBrut() {
		List<PointDouble> list = new ArrayList<>();
		if (getListSessionCurrency().isEmpty()) {
			return list;
		}
		// double lastPriceReference =
		// listSessionCurrency.get(listSessionCurrency.size() -
		// 1).getLastPrice();
		double lastPriceReference = getListSessionCurrency().get(0).getLastPrice();
		for (SessionCurrency s : getListSessionCurrency()) {
			Ticker t = s.getTicker_Z_1();

			double lastPrice;
			if (t != null) {
				lastPrice = t.getLastPrice();

			} else {
				lastPrice = 0;
			}
			double delta = Math.abs(lastPrice - s.getLastPrice());
			// System.out.println("LastPrice Brut "+name+" K :"+s.getK()+"
			// Ticker Last Price:\t"+lastPrice+"\t Session Last Price\t
			// "+s.getLastPrice()+" \tdelta : "+delta);
			double chd = 100 * (lastPrice - lastPriceReference) / lastPriceReference;
			PointDouble p = new PointDouble(s.getDateLastUpdateAsLong(), chd);
			list.add(p);
		}
		return list;
	}

	public List<PointDouble> getListPointsPrixFiltre() {
		List<PointDouble> list = new ArrayList<>();
		if (getListSessionCurrency().isEmpty()) {
			return list;
		}
		// double lastPriceReference =
		// listSessionCurrency.get(listSessionCurrency.size() -
		// 1).getLastPrice();
		double lastPriceReference = getListSessionCurrency().get(0).getLastPrice();
		for (SessionCurrency s : getListSessionCurrency()) {
			double lastPrice = s.getLastPrice();
			double chd = 100 * (lastPrice - lastPriceReference) / lastPriceReference;
			PointDouble p = new PointDouble(s.getDateLastUpdateAsLong(), chd);
			list.add(p);
		}
		return list;
	}

	/**
	 * 
	 * @return
	 */
	public List<PointDouble> getListPointsStochastiques_1heure() {
		List<PointDouble> list = new ArrayList<>();

		if (getListSessionCurrency().isEmpty()) {
			return list;
		}

		for (SessionCurrency s : getListSessionCurrency()) {
			Value stochas = s.getStochastique_1heure();
			if (stochas != null) {
				PointDouble p = new PointDouble(s.getDateLastUpdateAsLong(), stochas.getV());
				list.add(p);
			}
		}
		System.out.println("ListStochastique list.size :" + list.size() + "  " + list);
		return list;
	}

	/**
	 * 
	 * @return
	 */
	public List<PointDouble> getListPointsStochastiques_10mn() {
		List<PointDouble> list = new ArrayList<>();
		if (getListSessionCurrency().isEmpty()) {
			return list;
		}

		for (SessionCurrency s : getListSessionCurrency()) {
			Value stochas = s.getStochastique_10mn();
			if (stochas != null) {
				PointDouble p = new PointDouble(s.getDateLastUpdateAsLong(), stochas.getV());
				list.add(p);
			}

		}
		return list;
	}

	/**
	 * K =100 * (Prix - B)/(H-B)
	 * 
	 * @return le stochastique calcullé sur l'intervalle de temps dt
	 */

	public Value calculStochastique(long dt, Date date, Ticker ticker, Value Z_1) {
		long timeMin = date.getTime() - dt;
		double hh = -1;// MAx
		double bb = -1;// Minimum
		// Recherche 
		for (SessionCurrency s : getListSessionCurrency()) {
			Ticker t = s.getTicker_Z_1();
			if (t == null) {
			} else {
				if (t.getDate().getTime() > timeMin) {
					double price = t.getLastPrice();
					if (bb < 0) {
						bb = price;// Initialisation
					} else if (price < bb) {
						bb = price;
					}
					if (hh < 0) {
						hh = price;
					} else if (price > hh) {
						hh = price;
					}
				}
			}
		}
		double delta = (hh - bb);
		double k;
		if (delta < 0.000000000000001) {
			k = 0;
		} else {
			k = 100 * (ticker.getLastPrice() - bb) / (hh - bb);
		}
		Value v = new Value();
		v.setV(k);
		v.process(Z_1);
		return v;
	}

	public List<SessionCurrency> getListSessionCurrency() {
		if (this.listSessionCurrency_ == null) {
			this.listSessionCurrency_ = SessionCurrenciesFactory.instance.getSessionsCurrency(SIZE_MAX, this.name);
			this.listSessionCurrency_.add(0, this.sessionCurrency);
			// System.out.println("getListSessionCurrency2 "+this.name+"
			// "+listSessionCurrency_);
		}
		return listSessionCurrency_;
	}
	private static DecimalFormat df = new DecimalFormat("00.0000");
	public List<Balance> getListBalance() {
		if (this.listBalances == null) {
			this.listBalances = BalancesFactory.instance.getBalance(SIZE_MAX, this.sessionCurrency.getShortName(),Balance.TYPE_exchange);	
			for(Balance b : listBalances){
			//System.out.println(" amount in dollar "+b.getCurrency()+"  |"+df.format(b.getAmountInDollar())+"  Achat :"+b.getOrderAchat()+" Vente "+b.getOrderVente()+"   "+b.getDate());
			}
		}
		return listBalances;
	}

	public List<Value> getStochastiques_10mn() {
		List<Value> list = new ArrayList<>();
		for(SessionCurrency sc : getListSessionCurrency()){
			list.add(sc.getStochastique_10mn());
		}
		return list;
	}

	static final NumberFormat df_0 = new DecimalFormat("000"); 
	static final NumberFormat df_1 = new DecimalFormat("##0.0"); 
	static final NumberFormat df_3 = new DecimalFormat("000.000"); 
	
	public SimuResult getSimuResult(int retard) {
		double sommeInitiale = 100;
		if (getListSessionCurrency().isEmpty()) {
			return null;
		}
		double vDollar = sommeInitiale;
		double vCurrency=0;
		double price=0.0;
		int nAcheter = 0;
		int nVendre = 0;
		long timeStart = -1;
		SessionCurrency sc0=null;
		int nVente =0;
		int nAchat = 0;
		for(SessionCurrency sc : getListSessionCurrency()){
			if (timeStart <= 0){
				timeStart = sc.getDate().getTime();
			}
			sc0 = sc;
			Value v  = sc.getStochastique_10mn();
			price  = sc.getTicker_Z_1().getLastPrice();
			SessionCurrency.EtatSTOCHASTIQUE etat = SessionCurrency.getStochastique(v);
			if (etat.acheter){
				nAcheter++;
				nVendre=0;
			}else if (etat.vendre){
				nVendre++;
				nAcheter=0;
			}
			
			double totalD = vDollar+(vCurrency*price);
			//System.out.println("Dollar   "+df_3.format(vDollar)+"\tcurrency  "+df_3.format(vCurrency)+  "\ttotal en dollar  "+df_3.format(totalD)+"   price "+price+" \tacheter: "+etat.acheter+"  vendre "+etat.vendre+"  "+etat);
			if (nAcheter > retard){
				if (vDollar > 0.00000001){
					nAchat++;
				}
				vCurrency += vDollar/price;
				vDollar =0;
				
			}
			if (nVendre > retard){
				if (vCurrency > 0.0000000001){
					nVente++;
				}
				if (vCurrency > 0.0000001){
					vDollar += vCurrency*price;
					vCurrency=0;
					}
			}
		}
		
		double sommefinale_usd = vDollar+vCurrency*price;
		Long timeEnd = sc0.getDate().getTime();
		long duree = timeEnd - timeStart;
		SimuResult result = new SimuResult(sommeInitiale, sommefinale_usd, duree, nVente, nAchat);
		System.err.println("result "+result);
		return result;
	}

	public static class SimuResult {
		public double sommeInnitiale;
		public double sommeFinale;
		public  long duree;
		public int nbVente;
		public int nbAchat;
		public SimuResult(double sommeInnitiale, double sommeFinale, long duree, int nbVente, int nbAchat) {
			super();
			this.sommeInnitiale = sommeInnitiale;
			this.sommeFinale = sommeFinale;
			this.duree = duree;
			this.nbVente = nbVente;
			this.nbAchat = nbAchat;
		}
		@Override
		public String toString() {
			return "SimuResult [sommeInnitiale=" + sommeInnitiale + ", sommeFinale=" + sommeFinale + ", duree_mn=" + duree/60000
					+ ", nbVente=" + nbVente + ", nbAchat=" + nbAchat + "]";
		}
		public String toStringShort() {
			// TODO Auto-generated method stub
			return (df_1.format( 100.* (sommeFinale - sommeInnitiale)/sommeInnitiale)) +"%";
		}
	}

	public List<PointTriple>  getListPointsBalanceHistory() {
		List<PointTriple> list = new ArrayList<>();
		if (getListBalance().isEmpty()) {
			System.out.println("getListPointsBalanceHistory list is embty");
			return list;
		}

		for (Balance s : getListBalance()) {
			double vDollar = s.getAmountInDollar();
			if (s.getDate() != null){
			PointTriple p = new PointTriple(s.getDate().getTime(), vDollar,s.getOrderAchat(),s.getOrderVente());
			list.add(p);
			}
		}
		return list;
	}
	
}
