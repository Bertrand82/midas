package bg.panama.btc.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bg.panama.btc.BitfinexClient.EnumService;

public class Symbols {
	private static String jsonStr = "{\"symbols\":[\"btcusd\",\"ltcusd\",\"ltcbtc\",\"ethusd\",\"ethbtc\",\"etcbtc\",\"etcusd\",\"rrtusd\",\"rrtbtc\",\"zecusd\",\"zecbtc\",\"xmrusd\",\"xmrbtc\",\"dshusd\",\"dshbtc\",\"bccbtc\",\"bcubtc\",\"bccusd\",\"bcuusd\",\"btceur\",\"xrpusd\",\"xrpbtc\",\"iotusd\",\"iotbtc\",\"ioteth\",\"eosusd\",\"eosbtc\",\"eoseth\",\"sanusd\",\"sanbtc\",\"saneth\",\"omgusd\",\"omgbtc\",\"omgeth\",\"bchusd\",\"bchbtc\",\"bcheth\",\"neousd\",\"neobtc\",\"neoeth\",\"etpusd\",\"etpbtc\",\"etpeth\",\"qtmusd\",\"qtmbtc\",\"qtmeth\",\"bt1usd\",\"bt2usd\",\"bt1btc\",\"bt2btc\",\"avtusd\",\"avtbtc\",\"avteth\",\"edousd\",\"edobtc\",\"edoeth\",\"btgusd\",\"btgbtc\",\"datusd\",\"datbtc\",\"dateth\",\"qshusd\",\"qshbtc\",\"qsheth\",\"yywusd\",\"yywbtc\",\"yyweth\"]}";

	private static Symbols instance;
	List<String> list = new ArrayList<String>();

	public Symbols() throws Exception {
		this(new JSONObject(jsonStr));
	}

	public Symbols(JSONObject jo) {
		System.out.println("Constructeur symbole");
		instance = this;

		try {
			JSONArray array = jo.getJSONArray(EnumService.symbols.key);
			for (int i = 0; i < array.length(); i++) {
				list.add("" + array.get(i));
			}
		} catch (JSONException e) {
			System.err.println("Error B52 JSONException "+e.getMessage()+" | JsdonObject :"+jo);
		}

	}

	public static Symbols getInstance() {
		if (instance == null) {
			try {
				new Symbols();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public List<String> getList() {
		return list;
	}

	@Override
	public String toString() {
		return "Symbols [list=" + list + "]";
	}

	public List<String> getAllStartWith(String start) {
		List<String> listFiltred = new ArrayList<String>();
		for(String s : this.list){
			if (s.startsWith(start)){
				listFiltred.add(s);
			}
		}
		return listFiltred;
	}
	
	
	public List<String> getAllEndWith(String end) {
		List<String> listFiltred = new ArrayList<String>();
		for(String s : this.list){
			if (s.trim().endsWith(end)){
				listFiltred.add(s.trim());
			}
		}
		return listFiltred;
	}

	public boolean contains(String symbol) {
		return this.list.contains(symbol);
	}
	
	

}
