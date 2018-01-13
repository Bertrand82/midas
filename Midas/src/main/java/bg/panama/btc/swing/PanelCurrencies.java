package bg.panama.btc.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import bg.panama.btc.model.Balance;
import bg.panama.btc.model.Balances;
import bg.panama.btc.model.v2.ITicker;
import bg.panama.btc.trading.first.AlgoProcessCurrencies;
import bg.panama.btc.trading.first.SessionCurrencies;
import bg.panama.btc.trading.first.SessionCurrency;
import bg.util.heartBeat.HeartBeat;
import bg.util.heartBeat.ICheckAlive;

public class PanelCurrencies extends JPanel implements ICheckAlive {
	private static final DecimalFormat df = new DecimalFormat("0,000,000.00");
	private static final DecimalFormat df2 = new DecimalFormat("0.000;-.000");
	private static final DecimalFormat df3 = new DecimalFormat("#######0.000;");
	//private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss");
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");


	private static final long serialVersionUID = 1L;
	SessionCurrencies session;
	private JLabel labelTitre_ = new JLabel("Total ");
	private JLabel labelBest_ = new JLabel("Best ");
	private JLabel labelMontantTotal = new JLabel("montant Total");
	private JCheckBox checkBoxDisplayVariationPrice = new JCheckBox("Variation ", true);
	String[] columnNames = { "Symbol","montant" ,"= dollar", "% Day", "% Hour f", "% Hour instant","Eligible" ,"Variations","Prices"};
	 private Hashtable<String, PanelCanvasVariations> hCanvasVaritions = new Hashtable<>();
	 private Hashtable<String, PanelCanvasPrix> hCanvasPrix = new Hashtable<>();
	 private static final int nSelect = 6;
	 Balances balances ;
	AbstractTableModel tableModel = new AbstractTableModel() {
		private List<SessionCurrency> getList() {
		  return session.getListOrder_byHourlyChangePerCentByDay();
		}
		
		private static final long serialVersionUID = 1L;

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public int getRowCount() {
			return getList().size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		public boolean isCellEditable(int row, int col) {
			if(col == nSelect){
				return true;
			}
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			SessionCurrency sessionCurrency = (SessionCurrency) getList().get(rowIndex);
			String currencyStr = sessionCurrency.getShortName();
			Balance balance = null;
			if (balances != null){
				balance = balances.getBalance(currencyStr);
			}
			
			if (columnIndex == 0) {
				return sessionCurrency.getShortName();
			} else if (columnIndex == 1) {
				if (balance== null){
					return " - ";
				}else {
					double amount = balance.getAmount();
					return df3.format(amount);
					
				}
			} else if (columnIndex == 2) {
					if (balance == null) {
					return "-";
				}else if (balance.getAmountInDollar() <=0.01){
					return "0";
				} else {
					return df.format(balance.getAmountInDollar()) + " $";
				}

			} else if (columnIndex == 3) {
				double taux =sessionCurrency.getTicker_Z_1().getDaylyChangePerCent();				
				return taux;
			} else if (columnIndex == 4) {
				if(sessionCurrency.isInitializing()){
					return 0;
				}else {
					double taux = sessionCurrency.getHourlyChangePerCentByDay();			
					return  taux;
				}
			} else if (columnIndex == 5) {
				if(sessionCurrency.isInitializing()){
					return 0;
				}else {
					double taux = sessionCurrency.getHourlyChangePerCentByDayInstant();
					return taux;
				}
			}else if(columnIndex == nSelect){
				return sessionCurrency.isEligible();
			}else if(columnIndex == (nSelect+1)){
				String key =  sessionCurrency.getShortName();				
				return hCanvasVaritions.get(key).getImageIcon();
			}else if(columnIndex == (nSelect+2)){
				String key =  sessionCurrency.getShortName();
				
				return hCanvasPrix.get(key).getImageIcon();
			}
			return "";
		}
		
		public void setValueAt(Object value, int row, int col) {
			
			if (col == nSelect){
				SessionCurrency t = (SessionCurrency) getList().get(row);
				Boolean b = (Boolean) value;
				if (b != t.isEligible()){
					t.setEligible((Boolean) value);
					session.saveConfiguration();
				}
			} 			
		}

		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex){
				case 3:
				case 4:
				case 5 :
					return Double.class;
				case nSelect:
					return Boolean.class;
				case nSelect+1:
					return ImageIcon.class;
				case nSelect+2:
					return ImageIcon.class;
			}
			
			return Object.class;
		}

	};
	
	 public class DoubleTableCellRenderer extends DefaultTableCellRenderer {


		private static final long serialVersionUID = 1L;

		public DoubleTableCellRenderer() {
             setHorizontalAlignment(JLabel.RIGHT);
         }

         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
             if (value instanceof Number) {
                 value = df2.format(value);
             }
             return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         }

     }
	private JTable table;

	public PanelCurrencies(SessionCurrencies session) {
		
		super(new BorderLayout());
		HeartBeat.getInstance().add(this);
		this.session = session;
		initCanvas();
		table = new JTable(tableModel);
		table.getColumnModel().getColumn(3).setCellRenderer(new DoubleTableCellRenderer());
		table.getColumnModel().getColumn(4).setCellRenderer(new DoubleTableCellRenderer());
		table.getColumnModel().getColumn(5).setCellRenderer(new DoubleTableCellRenderer());
		table.setRowHeight(60);
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(0).setMaxWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(40);
		table.getColumnModel().getColumn(2).setPreferredWidth(80);//dollar
		table.getColumnModel().getColumn(3).setPreferredWidth(40);
		table.getColumnModel().getColumn(4).setPreferredWidth(80);
		table.getColumnModel().getColumn(5).setPreferredWidth(30);
		table.getColumnModel().getColumn(nSelect).setPreferredWidth(40);
		table.getColumnModel().getColumn(nSelect).setMaxWidth(40);
		table.getColumnModel().getColumn(nSelect+1).setPreferredWidth(300);
		table.getColumnModel().getColumn(nSelect+1).setMinWidth(300);
		table.getColumnModel().getColumn(nSelect+2).setPreferredWidth(300);
		table.getColumnModel().getColumn(nSelect+2).setMinWidth(300);
		
		table.setAutoCreateRowSorter(true);
		labelMontantTotal.setBorder(BorderFactory.createLineBorder(Color.RED));
		JPanel panelNorth = new JPanel(new BorderLayout());
		JPanel panelWest = new JPanel();
		panelWest.add(labelTitre_);
		panelWest.add(labelBest_);
		panelNorth.add(panelWest,BorderLayout.WEST);
		panelNorth.add(labelMontantTotal,BorderLayout.CENTER);
		panelNorth.add(checkBoxDisplayVariationPrice,BorderLayout.EAST);
		JScrollPane scrollPane = new JScrollPane(table);
		Dimension dim = new Dimension(1200, 400);
		scrollPane.setPreferredSize(dim);
		scrollPane.setMinimumSize(dim);
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(panelNorth, BorderLayout.NORTH);
		this.update(session);
	}
	
	void initCanvas(){
		try {
			for(SessionCurrency sc : this.session.getListOrder_byHourlyChangePerCentByDay()){	
						String currency = sc.getShortName();
						PanelCanvasVariations pcVariations = new PanelCanvasVariations(currency);
						PanelCanvasPrix pcPrix = new PanelCanvasPrix(currency);
						this.hCanvasVaritions.put(currency, pcVariations);
						this.hCanvasPrix.put(currency, pcPrix);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private long timeUpdate =0;
 	public void update(SessionCurrencies session) {
		this.session = session;
		timeUpdate = System.currentTimeMillis();
		Runnable runable = new Runnable() {
			public void run() {
				try {
					for(SessionCurrency sc : session.getlSessionCurrency()){
						String key = sc.getShortName();
						PanelCanvasVariations pcVariations = hCanvasVaritions.get(key);
						PanelCanvasPrix pcPrix = hCanvasPrix.get(key);
						Color color;
						String label = key;
						if (sc.getHourlyChangePerCentByDay() == SessionCurrency.D_default){
							color = Color.BLUE;
							label="Initializing "+key;
						}else if (sc.getHourlyChangePerCentByDay()>0){
							color = Color.GREEN;
						}else {
							color = Color.RED;
						}
						pcVariations.update(label,color, sc.getHistory());
						pcPrix.update(label,color, sc.getHistory());
					}
					table.updateUI();
					SessionCurrency best = session.getSessionCurrencyBestEligible();
					String bestEligible ;
					if(best== null){
						bestEligible=" - ";
					}else {
						bestEligible= best.getShortName();
					}
					long duree =System.currentTimeMillis() -  session.getTimeStart().getTime(); 
					String dureeStr = String.format("%02d h  %02d mn", 
						    TimeUnit.MILLISECONDS.toHours(duree),
						    TimeUnit.MILLISECONDS.toMinutes(duree) 	);
					labelTitre_.setText(" n :"+session.getNumero()+" | duree :"+dureeStr+"  | best :"+bestEligible);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//throw e;
				}
				
			}
		};
		SwingUtilities.invokeLater(runable);
	}

 	JDialog dialogAlertHearBeat;
	@Override
	public void checkIsAlive(long period) {
		if (this.timeUpdate ==0){
			return;
		}
		long d = System.currentTimeMillis()- this.timeUpdate;
		if(d> 2 * period) {
			System.err.println("Alert Heart Beat period:"+period+"  "+d);
			if (dialogAlertHearBeat == null){
				JFrame frame =(JFrame) SwingUtilities.getWindowAncestor(this);
				dialogAlertHearBeat = new JDialog(frame, "Heart Beat Alert", true);
			}
			dialogAlertHearBeat.setVisible(true);
			
		}
	}

	public void updateAlgo(AlgoProcessCurrencies algoProcess) {
		timeUpdate = System.currentTimeMillis();
		Runnable runable = new Runnable() {
			public void run() {
				try {					
					table.updateUI();
					String bestEligible =algoProcess.getTickerBest().getShortName();								
					labelBest_.setText(" zbest :"+bestEligible);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
			}
		};
		SwingUtilities.invokeLater(runable);
	}

	public void updateBalances(Balances balances) {
		System.out.println("updateBalances "+balances);
		this.balances = balances;
				Runnable runable = new Runnable() {
			public void run() {
				table.updateUI();
				labelMontantTotal.setText("Total :"+df.format(balances.getTotalAmountDollar())+" $  ");
			}
		};
		SwingUtilities.invokeLater(runable);
	}

}