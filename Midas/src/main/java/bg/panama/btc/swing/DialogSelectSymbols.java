package bg.panama.btc.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

public class DialogSelectSymbols extends JDialog implements ActionListener {

	String[] columnNames = { "Symbol", "comment", "selected" ,"max ($)", "Panic indicator"};
	
	private static final long serialVersionUID = 1L;

	AbstractTableModel tableModel = new AbstractTableModel() {
		
		private static final long serialVersionUID = 1L;
		SymbolsConfig symConf = SymbolsConfig.getInstance();
		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}
		@Override
		public int getRowCount() {
			return symConf.gethSymbols().size();
		}
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		public Object getValueAt(int row, int col) {
			SymbolConfig sy = symConf.getSymbolConfig(row);
			switch(col){
			case 0:
				return sy.getName();
			
			case 1:
				return sy.getComment();
				
			case 2: 
				return sy.isSelected();
			case 3:
				return sy.getMaxTrade();
			case 4:
				return sy.isIndicatorPanic();
			}
			return "";
		}

		public boolean isCellEditable(int row, int col) {
			if (col < 2) {
				return false;
			}
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			SymbolConfig sy = symConf.getSymbolConfig(row);
			switch(col) {
			case 2 :		
				sy.setSelected((Boolean) value);
				break;
			case 3 :
				sy.setMaxTrade((int) value);
				break;
			case 4 :
				sy.setIndicatorPanic((Boolean) value);
				break;
			}
			fireTableCellUpdated(row, col);
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 2) {
				return Boolean.class;
			}else if (columnIndex == 3){
				return Integer.class;
			}else if (columnIndex == 4){
				return Boolean.class;
			}
			return Object.class;
		}
	};

	public DialogSelectSymbols(JFrame parent, ActionListener okListener) {
		super(parent, "Symbols Config", true);
		
		
		if (parent != null) {
			Dimension parentSize = parent.getSize();
			Point p = parent.getLocation();
			setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
		}
		Font font = new Font ("Dialog", Font.BOLD, 18);
		UIManager.put("Table.font", font);
		UIManager.put("Table.foreground", Color.BLACK);
		
		JTable table = new JTable(tableModel);
		table.setRowHeight( 25 );
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		JButton button = new JButton("CANCEL");
		JButton buttonOk = new JButton("OK");
		buttonOk.addActionListener(this);
		buttonOk.addActionListener(okListener);
		buttonOk.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {				
					SymbolsConfig.getInstance().store();				
			}
		});
		JPanel buttonPane = new JPanel();
		buttonPane.add(button);
		buttonPane.add(buttonOk);
		button.addActionListener(this);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		dispose();
	}

}
