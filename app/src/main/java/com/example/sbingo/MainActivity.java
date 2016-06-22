package com.example.sbingo;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;




@SuppressLint("InflateParams") 
public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private int m_iELementTextSize = 0;
	private int m_iBingoView = 0;
	private int m_iBingoLine = 0;
	private int m_iBingLevel = Appdefine.INT_MINI_LEVEL;
	private int m_iBingNumber = 30;
	private boolean m_bGameMode = false;

	private ArrayList<BingoData> m_alBingoNumber = null;
	
	private ArrayList<Integer> m_alBingoNumAvailible = null;
	
	private AlertDialog.Builder m_dialinputBingoNum = null;
	private AlertDialog.Builder m_dialLevel = null;
	private LayoutInflater m_Inflater = null;
	private NumberListAdapter m_NumberListAdapter = null;

    @SuppressLint("InflateParams") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.bingo_name);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        metrics.widthPixels = metrics.widthPixels*17/19;

        m_iELementTextSize = metrics.widthPixels;

        m_Inflater = LayoutInflater.from(this);
        
        m_alBingoNumber = new ArrayList<MainActivity.BingoData>(); 

        initialActivity();
        getLayout();
        listNumber();
        setNumberInputDdialog();
        setLevelInputDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
	@Override
	protected void onPause() {
		super.onPause();

		SharedPreferences preferencesGame = getSharedPreferences(Appdefine.GAME_RESULT,MODE_PRIVATE);
		preferencesGame.edit().clear().commit();
		preferencesGame.edit().putInt(Appdefine.GAME_LEVEL, m_iBingLevel).commit();
		preferencesGame.edit().putInt(Appdefine.GAME_BINGO_NUMBER, m_iBingNumber).commit();
		preferencesGame.edit().putBoolean(Appdefine.GAME_MODE, m_bGameMode).commit();
		
		for (int i = 0;i<m_alBingoNumber.size();i++) {
			preferencesGame.edit().putInt(String.valueOf(i), m_alBingoNumber.get(i).m_iNum).commit();
			preferencesGame.edit().putBoolean(String.valueOf(m_alBingoNumber.size()+i), m_alBingoNumber.get(i).m_bState).commit();
		}
	}
	
	private DialogInterface.OnClickListener dioclNumInput = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			m_alBingoNumber.get(m_iBingoView).m_iNum = m_alBingoNumAvailible.get(which);

			TextView textElemNum = (TextView)findViewById(m_iBingoView).findViewById(R.id.textElement);
			
			if (m_alBingoNumber.get(m_iBingoView).m_iNum>=100) {
		    	textElemNum.setTextSize(m_iELementTextSize/(m_iBingLevel*9));
		    }else {
		    	textElemNum.setTextSize(m_iELementTextSize/m_iBingLevel/6);
			}		
			
  		    textElemNum.setText(String.valueOf(m_alBingoNumber.get(m_iBingoView).m_iNum));
  		    textElemNum.setBackgroundResource(R.drawable.bingonocircle);
		}
	};
	
	private DialogInterface.OnClickListener dioclLevel = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (true == m_bGameMode) {
				return;
			}
			
			TextView textExtra = (TextView)findViewById(R.id.textExtra);
			
			m_iBingLevel = which+Appdefine.INT_MINI_LEVEL;
			m_iBingNumber = Appdefine.INT_BINGO_NUMBER+m_iBingLevel*m_iBingLevel;			
			textExtra.setText(String.format(Locale.getDefault(), "%d%s", m_iBingNumber, getString(R.string.extra_number_of_bingo))); 
		    m_alBingoNumber.clear();
		   
	        for (int i = 1; i<=(m_iBingLevel*m_iBingLevel);i++) {
	        	m_alBingoNumber.add(new BingoData(0, true));
	        }		                
	        
	        getLayout();
	        listNumber();
		}
	};
	
	private void initialActivity() {
	 	TextView tvExtra = (TextView)findViewById(R.id.textExtra);
        TextView tvLine = (TextView)findViewById(R.id.textLine);
		TextView tvMode = (TextView)findViewById(R.id.textView2);
        ImageButton ibPre = (ImageButton)findViewById(R.id.imageButtonPre);
        ImageButton ibNext = (ImageButton)findViewById(R.id.imageButtonNext);        
        Button btnRandom = (Button)findViewById(R.id.button1);      
        Button btnClear = (Button)findViewById(R.id.button2);       
        Button btnLevel = (Button)findViewById(R.id.button3);
        Button btnMode = (Button)findViewById(R.id.buttonMode);        
        
        btnRandom.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnMode.setOnClickListener(this);
        btnLevel.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        ibPre.setOnClickListener(this);
        btnMode.setTag(true);
    
        SharedPreferences preferencesGame = getSharedPreferences(Appdefine.GAME_RESULT,MODE_PRIVATE);
        
        if (preferencesGame.contains(Appdefine.GAME_LEVEL) == false) { 
        	for (int i = 1; i<=(m_iBingLevel*m_iBingLevel);i++) { 
        		m_alBingoNumber.add(new BingoData(0, true));
            }
        }else {
        	m_iBingLevel = preferencesGame.getInt(Appdefine.GAME_LEVEL,3);
        	m_bGameMode = preferencesGame.getBoolean(Appdefine.GAME_MODE, true);
        	m_iBingNumber = preferencesGame.getInt(Appdefine.GAME_BINGO_NUMBER, m_iBingLevel*m_iBingLevel+Appdefine.INT_BINGO_NUMBER);
        	tvExtra.setText(String.valueOf(m_iBingNumber)+getString(R.string.extra_number_of_bingo)); 
        	
        	int iNum = 0;
        	boolean bState = true;
        	
        	for (int i = 0;i<m_iBingLevel*m_iBingLevel;i++) {
        		iNum = preferencesGame.getInt(String.valueOf(i), 0);
        		bState = preferencesGame.getBoolean(String.valueOf(i+m_iBingLevel*m_iBingLevel), true);
				m_alBingoNumber.add(new BingoData(iNum, bState));
			}
			countLine();
        	
        	if (m_bGameMode == true) {
    			tvLine.setText(String.valueOf(m_iBingoLine)+getString(R.string.point));
        		btnMode.setText(R.string.input);			
				ibPre.setVisibility(View.INVISIBLE);
				ibNext.setVisibility(View.INVISIBLE);
				btnRandom.setVisibility(View.INVISIBLE);
				btnClear.setVisibility(View.INVISIBLE);
				btnLevel.setVisibility(View.INVISIBLE);	
				tvExtra.setVisibility(View.INVISIBLE);
				tvMode.setText(getString(R.string.gamemode)+String.valueOf(m_iBingLevel));
				btnMode.setTag(false);
        	} 				
		}
        
	}
	
	private void getLayout() {
		int iViewNum = 0;
	    LinearLayout layout = (LinearLayout)findViewById(R.id.linearlayout);
	    layout.removeAllViews();
	    View viewRow = null;
	    View viewElement = null;
	    LinearLayout layoutElement = null;
	    TextView textElemNum = null;


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams( m_iELementTextSize/m_iBingLevel, m_iELementTextSize/m_iBingLevel);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(m_iELementTextSize/m_iBingLevel-Appdefine.INT_TEXT_MARGIN_SIZE,m_iELementTextSize/m_iBingLevel-Appdefine.INT_TEXT_MARGIN_SIZE);
        for (int i = 0;i<m_iBingLevel;i++) {
    	    viewRow = m_Inflater.inflate(R.layout.bingoiirow, null);
    	    layoutElement = (LinearLayout)viewRow.findViewById(R.id.bingorow);


    	    for (int j = 0;j<m_iBingLevel;j++) {
    		    viewElement = m_Inflater.inflate(R.layout.bingoiielement, null);
    		    viewElement.setId(iViewNum);
    		    viewElement.setLayoutParams(layoutParams);
    		    viewElement.setOnClickListener(this);
    		    textElemNum = (TextView)viewElement.findViewById(R.id.textElement);   		    
    		    textElemNum.setLayoutParams(layoutParams1);
                		    
    		    if (m_alBingoNumber.get(iViewNum).m_iNum >= 100) {
    		    	textElemNum.setTextSize(m_iELementTextSize/(m_iBingLevel*9));
    		    	if (true == m_alBingoNumber.get(iViewNum).m_bState) {
    		    		textElemNum.setBackgroundResource(R.drawable.bingonocircle);
    		    	}else {
						textElemNum.setBackgroundResource(R.drawable.bingocircle);
					}
    	    		textElemNum.setText(String.valueOf(m_alBingoNumber.get(iViewNum).m_iNum));
    		    }else if(0 != m_alBingoNumber.get(iViewNum).m_iNum) {
    		    		textElemNum.setTextSize(m_iELementTextSize/m_iBingLevel/6);
    		    	if (true == m_alBingoNumber.get(iViewNum).m_bState) {
    		    		textElemNum.setBackgroundResource(R.drawable.bingonocircle);
    		    	}else {
						textElemNum.setBackgroundResource(R.drawable.bingocircle);
					}
        		    textElemNum.setText(String.valueOf(m_alBingoNumber.get(iViewNum).m_iNum));
				}else {
					textElemNum.setBackgroundResource(R.drawable.bingoicon);
					textElemNum.setText("");
				}
    		    
    		    layoutElement.addView(viewElement);
    		    iViewNum = iViewNum+1;
    	    }	   
    	    layout.addView(viewRow);
       	}   
	};
	
	private void listNumber() {
		m_alBingoNumAvailible = new ArrayList<Integer>();
		boolean bsame = false;
		
	    for (int i = 1;i<=m_iBingNumber;i++) {
	    	bsame = false;
	    	int itime = 0;
	    	
	    	for (int j = 0;j<m_iBingLevel*m_iBingLevel;j++) {
	    		if (itime == m_iBingLevel*m_iBingLevel) {
					break;
				}
	    		
	    		if (i == m_alBingoNumber.get(j).m_iNum) {
	    			bsame = true;
	    			itime = itime+1;
	    			break;
	    		}
	    	}
	    	
	    	if (false == bsame) {
	           	m_alBingoNumAvailible.add(i);
	    	}
	    }
	}
	
	private void clearLayout() {
		m_alBingoNumber.clear();
		TextView tvElemNum = null;

		for (int i = 0; i<(m_iBingLevel*m_iBingLevel);i++) {
			m_alBingoNumber.add(new BingoData(0, true));

			tvElemNum = (TextView)findViewById(i).findViewById(R.id.textElement);
			tvElemNum.setText("");
			tvElemNum.setBackgroundResource(R.drawable.bingoicon);
		}			
		
		listNumber();
	}
	
	private void refreshLayout() {
		
		TextView tvElemNum = null;
		
		for (int i = 0; i<(m_iBingLevel*m_iBingLevel);i++) {
			m_alBingoNumber.get(i).m_bState = true;
			tvElemNum = (TextView) findViewById(i).findViewById(R.id.textElement);
			tvElemNum.setBackgroundResource(R.drawable.bingonocircle);
			
			if (100<=m_alBingoNumber.get(i).m_iNum) {
				tvElemNum.setTextSize(m_iELementTextSize/(m_iBingLevel*9));
		    }else {
		    	tvElemNum.setTextSize(m_iELementTextSize/m_iBingLevel/6);
			}		
			tvElemNum.setText(String.valueOf(m_alBingoNumber.get(i).m_iNum));
		}
	}
	
	private void changeModeLayout() {
	   for (int i = 0;i<m_iBingLevel*m_iBingLevel;i++) {
        	m_alBingoNumber.get(i).m_bState = true;
        	findViewById(i).findViewById(R.id.textElement).setBackgroundResource(R.drawable.bingonocircle);
        } 
	}
	
	private void getRandomNumber() {
		ArrayList<Integer> alTemp = new ArrayList<Integer>();
		int iPosition = 0;
		int iTemp = 0;
		int iRandomAdd = 0;
		boolean bSameNum = false;
		
		for(int i = 1;i<=m_iBingNumber;i++) {
			bSameNum = false;
			for (int j = 0;j<m_iBingLevel*m_iBingLevel;j++) {
				if (i == m_alBingoNumber.get(j).m_iNum) {
					bSameNum = true;
					break;							
				}
			}
			
			if (false == bSameNum) {
				alTemp.add(i);
			}					
		}
		
		for (int j = 0; j < alTemp.size(); j++) {
			iPosition = (int) (Math.random()*alTemp.size());
			iTemp = alTemp.get(j);					
			alTemp.set(j, alTemp.get(iPosition));
			alTemp.set(iPosition, iTemp);
		}
		
		for (int i = 0;i<m_iBingLevel*m_iBingLevel;i++) {
			
			if (0 == m_alBingoNumber.get(i).m_iNum) {
				m_alBingoNumber.get(i).m_iNum = alTemp.get(iRandomAdd);
				iRandomAdd = iRandomAdd + 1;
			}
		}	
	}
	
	private void getActivityView(View v) {
		
		Button btnRandom = (Button)findViewById(R.id.button1);
		TextView tvMode = (TextView)findViewById(R.id.textView2);
		Button btnClear = (Button)findViewById(R.id.button2);
		Button btnEnter = (Button)findViewById(R.id.button3);
		TextView tvLine = (TextView)findViewById(R.id.textLine);
		Button btnMode = (Button)findViewById(R.id.buttonMode);
		TextView tvExtra = (TextView)findViewById(R.id.textExtra);
		ImageButton ibPre = (ImageButton)findViewById(R.id.imageButtonPre);
		ImageButton ibNext = (ImageButton)findViewById(R.id.imageButtonNext);
		
		if (true == (Boolean)v.getTag()) {
			m_bGameMode = true;
			v.setTag(false);
			m_iBingoLine = 0;
			
			btnMode.setText(R.string.input);
			tvLine.setText(String.valueOf(m_iBingoLine)+getString(R.string.point));
			tvExtra.setVisibility(View.INVISIBLE);
			tvLine.setVisibility(View.VISIBLE);
			btnRandom.setVisibility(View.INVISIBLE);
			btnClear.setVisibility(View.INVISIBLE);
			btnEnter.setVisibility(View.INVISIBLE);
			ibPre.setVisibility(View.INVISIBLE);
			ibNext.setVisibility(View.INVISIBLE);
			tvMode.setText(getString(R.string.gamemode)+String.valueOf(m_iBingLevel));				
		} else {
			m_bGameMode = false;
			v.setTag(true);
			tvExtra.setVisibility(View.VISIBLE);
			ibPre.setVisibility(View.VISIBLE);
			ibNext.setVisibility(View.VISIBLE);
	        btnEnter.setVisibility(View.VISIBLE);
	        btnRandom.setVisibility(View.VISIBLE);
	        btnClear.setVisibility(View.VISIBLE);
	        tvLine.setVisibility(View.INVISIBLE);
	        tvMode.setText(R.string.inputmode);
	        btnMode.setText(R.string.game);
	        changeModeLayout();     
		}		
	}
	
	private void setNumberInputDdialog() {
		m_NumberListAdapter = new NumberListAdapter();
        m_dialinputBingoNum = new AlertDialog.Builder(this);
        
        m_dialinputBingoNum.setTitle(R.string.input_bingo_number);
        m_dialinputBingoNum.setCancelable(true);
        m_dialinputBingoNum.setAdapter(m_NumberListAdapter, dioclNumInput);
	}
	
	private void setLevelInputDialog() {
		m_dialLevel = new AlertDialog.Builder(this);
        m_dialLevel.setTitle(R.string.levelchoose);   
        m_dialLevel.setItems(Appdefine.STRINGS_LEVEL, dioclLevel);
        m_dialLevel.setCancelable(true);
	}
	

	private void countLine(View v,boolean bState) {
		int iViewID = v.getId();
		int iRow = iViewID/m_iBingLevel;
		int iColume = iViewID%m_iBingLevel;

		if (iColume == iRow) {		
			if(false == bState) {
				if (true == countLeftDia(iColume)) {
					m_iBingoLine = m_iBingoLine+1;
				}
			}else {
				m_alBingoNumber.get(iViewID).m_bState = false;
				if (true == countLeftDia(iColume)) {
					m_iBingoLine = m_iBingoLine-1;
				}
				m_alBingoNumber.get(iViewID).m_bState = true;
			}
			
		}
			
		if (iColume+iRow == m_iBingLevel-1) {	
			
			if (false == bState) {
				if (true == countRightDia(iRow)) {
					m_iBingoLine = m_iBingoLine+1;
				}
			}else {
				m_alBingoNumber.get(iViewID).m_bState = false;
				if (true == countRightDia(iRow)) {
					m_iBingoLine = m_iBingoLine-1;
				}
				m_alBingoNumber.get(iViewID).m_bState = true;
			}			
		
		}
		
		if (false == bState) {
			if (true == countRow(iColume)) {
				m_iBingoLine = m_iBingoLine+1;
			}
		}else {
			m_alBingoNumber.get(iViewID).m_bState = false;
			if (true == countRow(iColume)) {			
				m_iBingoLine = m_iBingoLine-1;	
			}
			m_alBingoNumber.get(iViewID).m_bState = true;
		}
		
		if (bState == false) {
			if (true == countColumn(iRow)) {
				m_iBingoLine = m_iBingoLine+1;
			}
		}else {
			m_alBingoNumber.get(iViewID).m_bState = false;
			if (true == countColumn(iRow)) {
				m_iBingoLine = m_iBingoLine-1;		
			}
			m_alBingoNumber.get(iViewID).m_bState = true;
		}
	}
	
	private void countLine() {
		m_iBingoLine = 0;
		for (int i=0;i<m_iBingLevel;i++) {
			if (true == countColumn(i)) {
				m_iBingoLine = m_iBingoLine+1;
			}
			
			if (true == countRow(i)) {
				m_iBingoLine = m_iBingoLine+1;
			}	
		}
		
		if (m_iBingLevel%2 == 1) {
			if (true == countRightDia(m_iBingLevel/2)) {
				m_iBingoLine = m_iBingoLine +1;
			}
			
			if (true == countLeftDia(m_iBingLevel/2)) {
				m_iBingoLine = m_iBingoLine+1;
			}
		}
	}
	
	private boolean countRow(int iColume) {
		boolean bRowLine = true;
		
		for (int i = 0;i<m_iBingLevel;i++) {
			if (true == m_alBingoNumber.get(i*m_iBingLevel+iColume).m_bState) {
				bRowLine = false;
				break;
			}
		}
		return bRowLine;
	}
	
	private boolean countColumn(int iRow) {
		boolean bColumnLine = true;
	
		for (int i = 0;i<m_iBingLevel;i++) {
			if (true == m_alBingoNumber.get(iRow*m_iBingLevel+i).m_bState ) {
				bColumnLine = false;
				break;
			}
		}
		return bColumnLine;
	}
	
	private boolean countRightDia(int iRow) {
		boolean bDial = true;	
		
		for (int i = 1;i<m_iBingLevel+1;i++ ) {				
			if (true == m_alBingoNumber.get(i*m_iBingLevel-i).m_bState) {
				bDial = false;
				break;
			}	
		}
		
		return bDial;
	}
	
	private boolean countLeftDia(int iColume) {
		boolean bDial = true;
		
		for (int i = 0;i<m_iBingLevel;i++) {				
			if(true == m_alBingoNumber.get(i*(m_iBingLevel+1)).m_bState) {
				bDial = false;
				break;
			}
		}
		
		return bDial;
	}
	
	@SuppressLint("ViewHolder") 
	class NumberListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			
			return m_alBingoNumAvailible.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			convertView = m_Inflater.inflate(R.layout.bingo_input_number_dial, null);
			TextView textView = (TextView)convertView.findViewById(R.id.textDialNum);
			textView.setText(String.valueOf(m_alBingoNumAvailible.get(position)));
		
			return convertView;
		}		
	}

	@Override
	public void onClick(View v) {
		if (v.getId()<m_iBingLevel*m_iBingLevel) {
			if (m_bGameMode == true) {
				BingoData bingoData = m_alBingoNumber.get(v.getId());
				TextView tvElemNum = (TextView) v.findViewById(R.id.textElement);
				
				if (bingoData.m_bState == true) {
					tvElemNum.setBackgroundResource(R.drawable.bingocircle);
				} else {
					tvElemNum.setBackgroundResource(R.drawable.bingonocircle);					
				}

				bingoData.m_bState = !bingoData.m_bState;

				countLine(v, bingoData.m_bState);					
//				countLine();
				TextView textLine = (TextView)findViewById(R.id.textLine);
				textLine.setText(String.valueOf(m_iBingoLine)+getString(R.string.point));
			}else {
				m_iBingoView = v.getId();
				listNumber();
				m_dialinputBingoNum.show();
			}
		}
		
		if (v.getId() == R.id.button1) {		
			if (false == m_bGameMode) {
				getRandomNumber();			
				refreshLayout();		    											        
		    }
		}
		
		if (v.getId() == R.id.button2) {
			if (false == m_bGameMode ) {			
				clearLayout();	        	        
			}
		}
		
		if (v.getId() == R.id.button3) {
			m_dialLevel.show();	
		}
		
		if (v.getId() == R.id.buttonMode) {
			for (int i = 0;i<(m_iBingLevel*m_iBingLevel);i++) {
				if (m_alBingoNumber.get(i).m_iNum == 0) {
					return;
				}
			}			
			getActivityView(v);	
		}
		
		if (v.getId() == R.id.imageButtonPre) {
			if (m_iBingNumber>m_iBingLevel*m_iBingLevel) {
				TextView textExtra = (TextView)findViewById(R.id.textExtra);
				
				m_iBingNumber = m_iBingNumber -1;
				clearLayout();
				textExtra.setText(String.valueOf(m_iBingNumber)+getString(R.string.extra_number_of_bingo));
			}
		}
		
		if (v.getId() == R.id.imageButtonNext) {
			if (m_iBingNumber<Appdefine.INT_BINGO_NUMBER_LIMIT+m_iBingLevel*m_iBingLevel) {
				TextView textExtra = (TextView)findViewById(R.id.textExtra);
				
				m_iBingNumber = m_iBingNumber +1;
				clearLayout();			
				textExtra.setText(String.valueOf(m_iBingNumber)+getString(R.string.extra_number_of_bingo));
			}	
		}
	}
	
	class BingoData {
		public boolean m_bState = true;
		public int m_iNum = -1;
		
		public BingoData(int iNum, boolean bState) {
			m_iNum = iNum;
			m_bState = bState;
		}
	}
}
