package ir.abplus.adanalas.Timeline;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.GridLayout.Spec;
import com.fourmob.datetimepicker.Utils;
import com.fourmob.datetimepicker.date.PersianCalendar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnPullEventListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshPinnedHeaderListView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import ir.abplus.adanalas.AddEditTransactions.AddPage1;
import ir.abplus.adanalas.Charts.ChartActivity;
import ir.abplus.adanalas.Libraries.*;
import ir.abplus.adanalas.Libraries.TransactionsContract.TransactionEntry;
import ir.abplus.adanalas.R;
import ir.abplus.adanalas.Setting.SettingActivity;
import ir.abplus.adanalas.SyncCloud.LoginActivity2;
import ir.abplus.adanalas.Uncategoried.UncategoriedActivity;
import ir.abplus.adanalas.databaseConnections.LocalDBServices;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TimelineActivity extends Activity implements OnClickListener, OnPullEventListener<PinnedHeaderListView>, OnRefreshListener2<PinnedHeaderListView>
{
	public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int MONTHLY = 2;
    public static final int YEARLY = 3;
	
	private static String[] ordinal = {"", "اول", "دوم", "سوم", "چهارم", "پنجم", "ششم", "هفتم"};
	

	private static final String TYPEFACE_NAME = "nassim-regular.ttf";
	private static final String BOLD_TYPEFACE_NAME = "nassim-bold.ttf";
	public static final int HALF_TRANSPARENT = 50;
	public static final int FULLY_OPAQUE = 255;
	public static Typeface persianTypeface;
	public static Typeface persianBoldTypeface;
	// 	public static int screenWidth;
	//	public static int screenHeight;
	boolean firstTime = true;


	public static boolean[] expenseSelection = new boolean[Category.EXPENSE_SIZE];
	public static boolean[] incomeSelection = new boolean[Category.INCOME_SIZE];
	public static boolean[] accountSelection=  new boolean[4];

	String totalIncomeString;
	String totalExpenseString;
	
	ImageButton addButton;
	LinearLayout totalExpenseLayout;
	TextView totalIncome;
	LinearLayout totalIncomeLayout;
	TextView totalExpense;
	ArrayList<TimelineItem> listItems = new ArrayList<TimelineItem>();
	ArrayList<FilterMenuItem> accountsAndTimeFilter = new ArrayList<FilterMenuItem>();

	PullToRefreshPinnedHeaderListView phList;
	TimelinePinnedHeaderAdapter pHAdapter;
	public static FilterMenuAdapter accountMenuAdapter;

	String titleText;
	TextView title;
	PersianCalendar filterDate = new PersianCalendar();

	DisplayMetrics displayMetrics;
	float screenDpWidth;
	private String lastUpdateString = "";
    private Cursor c;
    private PersianDate date = null;
    private Time time = null;
    private String query;
    private String where;
    private LinearLayout totalIncomeCurrencyLayout;
    private LinearLayout totalExpenseCurrencyLayout;
    private String PREFRENCES_FILE ="preferences";

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{


		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline_layout);
//        System.out.println("@@@"+Currency.thousandToRial("1123.03"));

//		TextView title = (TextView) findViewById(getResources().getIdentifier("action_bar_title", "id","android"));
//		title.setTypeface(persianTypeface);
//		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, TITLE_SIZE);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		getActionBar().setCustomView(R.layout.timeline_actionbar_layout);



		Currency.setCurrency(Currency.TOMAN);
        SettingActivity.defaultAccount="نقدی 1";
		persianTypeface = Typeface.createFromAsset(this.getAssets(), TYPEFACE_NAME);
		persianBoldTypeface = Typeface.createFromAsset(this.getAssets(), BOLD_TYPEFACE_NAME);
		Category.initialize();

		title = (TextView) findViewById(R.id.timeline_title);
		title.setTypeface(persianTypeface);
//		final SearchView searchView = (SearchView) findViewById(R.id.search_view);
//		searchView.setQueryHint("جست‌وجو");
//		searchView.setOnSearchClickListener(new OnClickListener()
//		{
//			@Override
//			public void onClick(View search)
//			{
//				title.setText("");
//				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
//						RelativeLayout.LayoutParams.MATCH_PARENT,
//						RelativeLayout.LayoutParams.MATCH_PARENT);
//				search.setLayoutParams(p);
//				Toast.makeText(context, "listener", Toast.LENGTH_SHORT).show();
//			}
//		});
//		
//		searchView.setOnCloseListener(new SearchView.OnCloseListener()
//		{
//			@Override
//			public boolean onClose()
//			{
//				setTitleText();
//				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
//						RelativeLayout.LayoutParams.WRAP_CONTENT,
//						RelativeLayout.LayoutParams.MATCH_PARENT);
//				p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//				searchView.setLayoutParams(p);
//				return false;
//			}
//		});
//		
//		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
//		{
//			@Override
//			public boolean onQueryTextSubmit(String arg0)
//			{
//				String[] tokens = arg0.split(TAG_SEPARATOR);
//				return false;
//			}
//			
//			@Override
//			public boolean onQueryTextChange(String arg0)
//			{
//				SQLiteDatabase db = trHelper.getReadableDatabase();
//				
//				String query;
//				Cursor c;
//				
//				query = "SELECT DISTINCT(" + TagsEntry.COLUMN_NAME_TAG + ")"+
//						" FROM "+TagsEntry.TABLE_NAME;
//				
//				c = db.rawQuery(query, null);
//				c.moveToFirst();
//				ArrayList<String> tags = new ArrayList<String>();
//				
//				do
//					tags.add(c.getString(c.getColumnIndexOrThrow(TagsEntry.COLUMN_NAME_TAG)));
//				while(c.moveToNext());
//
//				SearchableInfo si = 
//				searchView.setSuggestionsAdapter(adapter);
//				return false;
//			}
//		});

		for(int i = 0; i < Category.EXPENSE_SIZE; i++)
			expenseSelection[i] = true;
		
		for(int i = 0; i < Category.INCOME_SIZE; i++)
			incomeSelection[i] = true;
		
		totalExpenseLayout = (LinearLayout) findViewById(R.id.total_expense_layout);
		totalExpense = (TextView) totalExpenseLayout.findViewById(R.id.total_expense_text);
		totalExpense.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
		totalExpenseCurrencyLayout = (LinearLayout) totalExpenseLayout.findViewById(R.id.currency_layout_exp);
		Currency.setCurrencyLayout(totalExpenseCurrencyLayout, this, getResources().getColor(R.color.red), persianTypeface, Currency.SMALL_TEXT_SIZE);
		totalExpense.setTypeface(persianTypeface);
		totalExpense.setTextColor(getResources().getColor(R.color.red));

		totalIncomeLayout = (LinearLayout) findViewById(R.id.total_income_layout);
		totalIncome = (TextView) totalIncomeLayout.findViewById(R.id.total_income_text);
		totalIncome.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        totalIncomeCurrencyLayout = (LinearLayout) totalIncomeLayout.findViewById(R.id.currency_layout_inc);
		Currency.setCurrencyLayout(totalIncomeCurrencyLayout , this, getResources().getColor(R.color.green), persianTypeface, Currency.SMALL_TEXT_SIZE);
		totalIncome.setTypeface(persianTypeface);
		totalIncome.setTextColor(getResources().getColor(R.color.green));



		displayMetrics = getResources().getDisplayMetrics();

		screenDpWidth = displayMetrics.widthPixels / displayMetrics.density;

        addAccountsAndTimesToList();



		// timeline listview
		phList = (PullToRefreshPinnedHeaderListView) findViewById(R.id.pinnedHeaderListView1);
		pHAdapter = new TimelinePinnedHeaderAdapter(this,getResources(),(int)(screenDpWidth/2)-50);
		pHAdapter.setItems(listItems);
		phList.setPullToRefreshOverScrollEnabled(false);
		phList.setMode(Mode.BOTH);
		phList.setAdapter(pHAdapter);
		phList.setScrollingWhileRefreshingEnabled(true);
		phList.setScrollContainer(false);
		phList.getLoadingLayoutProxy().setTextTypeface(persianBoldTypeface);
		phList.getLoadingLayoutProxy().setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
		phList.getLoadingLayoutProxy().setTextInclueFontPadding(false);
		phList.getLoadingLayoutProxy().setSubTextTypeface(persianTypeface);
		phList.getLoadingLayoutProxy().setSubTextInclueFontPadding(false);
		phList.getLoadingLayoutProxy().setSubTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
		phList.setOnPullEventListener(this);
		phList.setOnRefreshListener(this);


        phList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {



                OnClickedDialog onClickedDialog=new OnClickedDialog(TimelineActivity.this,((TimelineItem)adapterView.getAdapter().getItem(i)),getResources());
                onClickedDialog.show();

            }
        });


        addSlidingMenu();

		setTitleText();

		addButton = (ImageButton) findViewById(R.id.add_button);
		addButton.setOnClickListener(this);

//		try {
//			immediateRefreshTimeline();
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		setPullToRefreshLabels();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });

        getSharedPreferences();
	}

    private void addAccountsAndTimesToList() {
        accountsAndTimeFilter = new ArrayList<FilterMenuItem>();
        Cursor c2;
        c2= LocalDBServices.getAccountList(this);
        c2.moveToFirst();
        if(c2.getCount() != 0)
        {
            do
            {

                String accountName=c2.getString(c2.getColumnIndexOrThrow(TransactionsContract.Accounts.COLUMN_NAME_Account_Name));
                if(c2!=null){
                    if(c2.isFirst())
                        accountsAndTimeFilter.add(new FilterMenuItem("حساب‌ها", true,accountName, false, R.drawable.vaam_raw));
                    else
                        accountsAndTimeFilter.add(new FilterMenuItem("", true,accountName, false, R.drawable.vaam_raw));
                }
            }while(c2.moveToNext());
        }
        c2.close();

        accountsAndTimeFilter.add(new FilterMenuItem(getResources().getString(R.string.time_interval),
                false, getResources().getString(R.string.daily), true, 0));
        accountsAndTimeFilter.add(new FilterMenuItem("", false,
                getResources().getString(R.string.weekly), true, 0));
        accountsAndTimeFilter.add(new FilterMenuItem("", false,
                getResources().getString(R.string.monthly), true, 0));
        accountsAndTimeFilter.add(new FilterMenuItem("", true,
                getResources().getString(R.string.yearly), true, 0));
    }



    private void setTitleText()
	{
		switch(accountMenuAdapter.getRadioSelected())
		{
		case DAILY:
			titleText = new PersianDate((short)filterDate.get(PersianCalendar.DATE),
					(short)filterDate.get(PersianCalendar.MONTH),
					(short)filterDate.get(PersianCalendar.YEAR),
					PersianCalendar.weekdayFullNames[filterDate.get(PersianCalendar.DAY_OF_WEEK)])
			.toString();
			break;
		case WEEKLY:
			titleText = "هفته" + " " + ordinal[filterDate.get(PersianCalendar.WEEK_OF_MONTH)]
					+ " " + PersianCalendar.months[filterDate.get(PersianCalendar.MONTH)];
			break;
		case MONTHLY:
			titleText = PersianCalendar.months[filterDate.get(PersianCalendar.MONTH)] + " " +
					filterDate.get(PersianCalendar.YEAR);
			break;
		case YEARLY:
			titleText = filterDate.get(PersianCalendar.YEAR)+"";
			break;
		}
		title.setText(Utils.toPersianNumbers(titleText));
	}

	private void refreshTimeline() throws ParseException{

//        pHAdapter.notifyDataSetChanged();
//        new ReceiverThread().run();
        if(isDatabaseChanged()){
//            SQLiteDatabase db = trHelper.getReadableDatabase();

//			setCursor();
            System.out.println("refreshTimeline called");
            makeItemFromCursor(LocalDBServices.getTransactionsFromDB(this,expenseSelection,incomeSelection,accountMenuAdapter.getRadioSelected(),accountMenuAdapter,filterDate));
//            pHAdapter.notifyDataSetChanged();
//            new ReceiverThread().run();
            String colName = "totalExpense";
            Cursor c=LocalDBServices.getTotalExpense(this);
            c.moveToFirst();
            final Long totalExp = c.getLong(c.getColumnIndexOrThrow(colName));
            Long totalst=c.getLong(c.getColumnIndexOrThrow(colName));
            Log.e("debug","total expense: "+totalExp);
            Log.e("debug","total!!expense: "+totalst);
            c.close();
//            totalExpense.setText("  "+Utils.toPersianNumbers(Currency.getStandardAmount(totalExp)));

            colName = "totalIncome";
//			query = "SELECT SUM("+TransactionEntry.COLUMN_NAME_AMOUNT+") AS "+colName+
//					" FROM "+TransactionEntry.TABLE_NAME+
//					" WHERE "+TransactionEntry.COLUMN_NAME_IS_EXPENSE+"=0 and" + where;
//			c = db.rawQuery(query, null);
            c=LocalDBServices.getTotalIncome(this);
            c.moveToFirst();
            final Long totalInc = c.getLong(c.getColumnIndexOrThrow(colName));
            c.close();
//            totalIncome.setText("  "+Utils.toPersianNumbers(Currency.getStandardAmount(totalInc)));

//            ReceiverThread rt=new ReceiverThread();
//            rt.totalExp=totalExp;
//            rt.totalInc=totalInc;
//            rt.run();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    totalExpense.setText("  "+Utils.toPersianNumbers(Currency.getStdAmount(totalExp)));
                    totalIncome.setText("  "+Utils.toPersianNumbers(Currency.getStdAmount(totalInc)));
                }
            });

        }



	}



	public void immediateRefreshTimeline() throws IllegalArgumentException, ParseException{
        System.arraycopy(accountMenuAdapter.getAccountsSelection(),0,accountSelection,0,accountMenuAdapter.getAccountsSelection().length);
        pHAdapter.notifyDataSetChanged();
        if(isDatabaseChanged()){
//			SQLiteDatabase db = trHelper.getReadableDatabase();
			
//			setCursor();
            System.out.println("immediateRefreshTimeline called");
            makeItemFromCursor(LocalDBServices.getTransactionsFromDB(this,expenseSelection,incomeSelection,accountMenuAdapter.getRadioSelected(),accountMenuAdapter,filterDate));

            pHAdapter.notifyDataSetChanged();
			String colName = "totalExpense";
            Cursor c=LocalDBServices.getTotalExpense(this);
			c.moveToFirst();
			Long total = c.getLong(c.getColumnIndexOrThrow(colName));
            c.close();
            totalExpense.setText("  "+Utils.toPersianNumbers(Currency.getStdAmount(total)));
            Currency.setCurrencyLayout(totalExpenseCurrencyLayout, this, getResources().getColor(R.color.red), persianTypeface, Currency.SMALL_TEXT_SIZE);

			colName = "totalIncome";
            c=LocalDBServices.getTotalIncome(this);
			c.moveToFirst();
			total = c.getLong(c.getColumnIndexOrThrow(colName));
            c.close();
			totalIncome.setText("  "+Utils.toPersianNumbers(Currency.getStdAmount(total)));
            Currency.setCurrencyLayout(totalIncomeCurrencyLayout, this, getResources().getColor(R.color.green), persianTypeface, Currency.SMALL_TEXT_SIZE);
		}
	}

	private boolean isDatabaseChanged() throws ParseException
	{
		return true;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onResume()
	{




//		//TODO change to dip
		GridLayout expenses = (GridLayout) findViewById(R.id.expense_grid);

		int count = expenses.getChildCount();
		for(int i = 0; i < count; i++)
			if(expenseSelection[i])
				expenses.getChildAt(i).getBackground().setAlpha(FULLY_OPAQUE);
			else
				expenses.getChildAt(i).getBackground().setAlpha(HALF_TRANSPARENT);
		
		GridLayout incomes = (GridLayout) findViewById(R.id.incomes_grid);

		count = incomes.getChildCount();
		for(int i = 0; i < count; i++)
			if(incomeSelection[i])
				incomes.getChildAt(i).getBackground().setAlpha(FULLY_OPAQUE);
			else
				incomes.getChildAt(i).getBackground().setAlpha(HALF_TRANSPARENT);
		
		super.onResume();
		((ImageButton)(findViewById(R.id.add_button))).getDrawable().setAlpha(FULLY_OPAQUE);
//		new RefreshTask().execute();
		try {
			immediateRefreshTimeline();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

        addAccountsAndTimesToList();
        addSlidingMenu();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
        SharedPreferences settings = getSharedPreferences(PREFRENCES_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();

//        editor.putString("pfmtoken",ConnectionManager.pfmToken);
//        editor.putString("pfmcookie",ConnectionManager.pfmCookie);

        for(int i=0;i<accountsAndTimeFilter.size();i++){
            if (!accountsAndTimeFilter.get(i).isRadioButton)
            editor.putBoolean("AccountsAndTime:"+accountsAndTimeFilter.get(i).text,accountsAndTimeFilter.get(i).isChecked);
            else
            editor.putBoolean("AccountsAndTime:"+accountsAndTimeFilter.get(i).text,false);
        }
        editor.putBoolean("AccountsAndTime:"+getSelectedRadioTimeString(),true);
        for(int i=0;i<expenseSelection.length;i++){
            editor.putBoolean("expenseCatId:"+i,expenseSelection[i]);
        }
        for(int i=0;i<incomeSelection.length;i++){
            editor.putBoolean("incomeCatId:"+i,incomeSelection[i]);
        }
        // Commit the edits!
        editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_timeline, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        Intent intent = null;
        boolean doNothig=false;
        if(item.toString().equals(getResources().getString(R.string.action_uncategorized))){
            intent=new Intent(TimelineActivity.this, UncategoriedActivity.class);
        }
        else if(item.toString().equals(getResources().getString(R.string.action_settings))){
            intent=new Intent(TimelineActivity.this, SettingActivity.class);
        }
        else if(item.toString().equals(getResources().getString(R.string.action_charts))){
            intent=new Intent(TimelineActivity.this, ChartActivity.class);
        }
        else if(item.toString().equals(getResources().getString(R.string.action_timeline))){
         doNothig=true;
        }
        if(!doNothig){
        if(intent!=null){
//            finish();
            startActivityForResult(intent,100);
            overridePendingTransition(0, 0);
        }
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private int getSlidingMenuRightPaddding(int column, int resID)
	{
		BitmapFactory.Options o = new BitmapFactory.Options();
		switch((int)(4*(getResources().getDisplayMetrics().density)))
		{
		case 3:
			o.inTargetDensity = DisplayMetrics.DENSITY_LOW;
			break;
		case 4:
			o.inTargetDensity = DisplayMetrics.DENSITY_MEDIUM;
			break;
		case 6:
			o.inTargetDensity = DisplayMetrics.DENSITY_HIGH;
			break;
		case 8:
			o.inTargetDensity = DisplayMetrics.DENSITY_XHIGH;
			break;
		case 12:
			o.inTargetDensity = DisplayMetrics.DENSITY_XXHIGH;
			break;
		}
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), resID, o);
		int w = (int) (bmp.getWidth()/getResources().getDisplayMetrics().density);
		int dp = (int) (getResources().getDimension(R.dimen.sliding_menu_behid_offset) / getResources().getDisplayMetrics().density);
		return (int)(((screenDpWidth-dp)-(column*w)));
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private int getSlidingMenuNumberOfColumns(GridLayout gridLayout, int resourceID)
	{
		BitmapFactory.Options o = new BitmapFactory.Options();
		switch((int)(4*(getResources().getDisplayMetrics().density)))
		{
		case 3:
			o.inTargetDensity = DisplayMetrics.DENSITY_LOW;
			break;
		case 4:
			o.inTargetDensity = DisplayMetrics.DENSITY_MEDIUM;
			break;
		case 6:
			o.inTargetDensity = DisplayMetrics.DENSITY_HIGH;
			break;
		case 8:
			o.inTargetDensity = DisplayMetrics.DENSITY_XHIGH;
			break;
		case 12:
			o.inTargetDensity = DisplayMetrics.DENSITY_XXHIGH;
			break;
		}

		Bitmap bmp = BitmapFactory.decodeResource(getResources(), resourceID, o);
		int w = (int) (bmp.getWidth()/getResources().getDisplayMetrics().density);
		int dp = (int) (getResources().getDimension(R.dimen.sliding_menu_behid_offset) / getResources().getDisplayMetrics().density);
		int leftPadding = (int) (gridLayout.getPaddingLeft()/getResources().getDisplayMetrics().density);
		int rightPadding = (int) (gridLayout.getPaddingRight()/getResources().getDisplayMetrics().density);
		int column = (int)((screenDpWidth-dp-leftPadding-rightPadding)/(w));
            if(column==0)
                column=1;
		return column;
	}

	@Override
	public void onClick(View button)
	{
		if(button.getId() == addButton.getId())
		{
			//Set the button's appearance
//			((ImageButton)button).getDrawable().setAlpha(HALF_TRANSPARENT);
//			Drawable mDrawable = getResources().getDrawable(R.drawable.big_add_button);
//			mDrawable.setColorFilter(new PorterDuffColorFilter(0x00ff00,android.graphics.PorterDuff.Mode.MULTIPLY));
			// Assuming "color" is your target color
//			int color = 0xFFFFFF;
//	        float r = Color.red(color) / 255f;
//	        float g = Color.green(color) / 255f;
//	        float b = Color.blue(color) / 255f;
//
//	        ColorMatrix cm = new ColorMatrix(new float[] {
//	                // Change red channel
//	                r, 0, 0, 0, 0,
//	                // Change green channel
//	                0, g, 0, 0, 0,
//	                // Change blue channel
//	                0, 0, b, 0, 0,
//	                // Keep alpha channel
//	                0, 0, 0, 1, 0,
//	        });
//	        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
//	        Drawable myDrawable = getResources().getDrawable(R.drawable.big_add_button);
//			myDrawable .setColorFilter(cf);
			button.setSelected(!button.isSelected());
			Intent intent = new Intent(TimelineActivity.this, AddPage1.class);
			TimelineActivity.this.startActivity(intent);
		}
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		finish();
	}
	
	private class SyncTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params)
		{
			// Simulates a background job.
			try
			{
				refreshTimeline();
            }
			catch (ParseException e1)
			{
				e1.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
//			totalIncome.setText(totalIncomeString);
//			totalExpense.setText(totalExpenseString);
//            totalExpense.setText("##");
			pHAdapter.setItems(listItems);
			pHAdapter.notifyDataSetChanged();
			phList.onRefreshComplete();
			setPullToRefreshLabels();
			setTitleText();
			super.onPostExecute(result);
		}
	}

	private class RefreshTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params)
		{
			// Simulates a background job.
			try
			{
				refreshTimeline();
			}
			catch (ParseException e1)
			{
				e1.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
//			totalIncome.setText(totalIncomeString);
//			totalExpense.setText(totalExpenseString);
			pHAdapter.setItems(listItems);
			pHAdapter.notifyDataSetChanged();
			phList.onRefreshComplete();
			setTitleText();
			//TODO scroll up
			super.onPostExecute(result);
		}
	}
	
	private boolean shouldSync()
	{
		// TODO set to greatest date
		PersianCalendar tmpCal = new PersianCalendar();
		switch(accountMenuAdapter.getRadioSelected())
		{
		case DAILY:
			return tmpCal.equalDay(filterDate);
		case WEEKLY:
			return tmpCal.equalWeek(filterDate);
		case MONTHLY:
			return tmpCal.equalMonth(filterDate);
		case YEARLY:
			return tmpCal.equalYear(filterDate);
		}

		return false;
	}


	int counter = 0;
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<PinnedHeaderListView> refreshView)
	{
		if(shouldSync())
		{
			// Do work to refresh the list here.
			new SyncTask().execute();
			return;
		}
		
		switch(accountMenuAdapter.getRadioSelected())
		{
		case DAILY:
			filterDate.add(PersianCalendar.DAY_OF_MONTH, +1);
			break;
		case WEEKLY:
			filterDate.add(PersianCalendar.DAY_OF_MONTH, +7);
			break;
		case MONTHLY:
			filterDate.add(PersianCalendar.MONTH, +1);
			break;
		case YEARLY:
			filterDate.add(PersianCalendar.YEAR, +1);
			break;
		}

//		caltext.setText(getFilterDate()+"\n"+counter);
		counter++;
		{
			System.out.println("refresh");
			// Do work to refresh the list here.
			new RefreshTask().execute();
		}
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<PinnedHeaderListView> refreshView)
	{
		switch(accountMenuAdapter.getRadioSelected())
		{
		case DAILY:
			filterDate.add(PersianCalendar.DAY_OF_MONTH, -1);
			break;
		case WEEKLY:
			filterDate.add(PersianCalendar.DAY_OF_MONTH, -7);
			break;
		case MONTHLY:
			filterDate.add(PersianCalendar.MONTH, -1);
			break;
		case YEARLY:
			filterDate.add(PersianCalendar.YEAR, -1);
			break;
		}

//		caltext.setText(getFilterDate()+"\n"+counter);
		counter++;
		// Do work to refresh the list here.
		new RefreshTask().execute();
	}

	@Override
	public void onPullEvent(PullToRefreshBase<PinnedHeaderListView> refreshView,
			State state,Mode direction)
	{
		if(state == State.PULL_TO_REFRESH && direction == Mode.PULL_FROM_START)
		{
			if(shouldSync())
			{
				PersianCalendar tmpCal = new PersianCalendar();
				Calendar tmpTime = Calendar.getInstance();

				int dayOfWeek = tmpCal.get(PersianCalendar.DAY_OF_WEEK);
				int dayOfMonth = tmpCal.get(PersianCalendar.DAY_OF_MONTH);
				int month = tmpCal.get(PersianCalendar.MONTH);
				String date = PersianCalendar.weekdayFullNames[dayOfWeek]+" "+dayOfMonth+" "+PersianCalendar.months[month];

				int minute = tmpTime.get(Calendar.MINUTE);
				int hour = tmpTime.get(Calendar.HOUR_OF_DAY);
				String time = hour+":"+String.format("%02d", minute);

				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(lastUpdateString);
				// Update the lastUpdatedString
				lastUpdateString = Utils.toPersianNumbers(time+" - "+date);

				refreshView.getLoadingLayoutProxy().setPullLabelUp(
						getResources().getString(R.string.sync_string));

				refreshView.getLoadingLayoutProxy().setReleaseLabelUp(
						getResources().getString(R.string.sync_string));
			}
			else
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("");

		}
		else if(state == State.PULL_TO_REFRESH && direction == Mode.PULL_FROM_END)
			refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("");

	}
	
	private void setPullToRefreshLabels()
	{
		String upLabel = "";
		String bottomLabel = "";
		switch (accountMenuAdapter.getRadioSelected())
		{
		case DAILY:
			upLabel = "روز بعد";
			bottomLabel = "روز قبل";
			break;
		case WEEKLY:
			upLabel = "هفته‌ی بعد";
			bottomLabel = "هفته‌ی قبل";
			break;
		case MONTHLY:
			upLabel = "ماه بعد";
			bottomLabel = "ماه قبل";
			break;
		case YEARLY:
			upLabel = "سال بعد";
			bottomLabel = "سال قبل";
			break;
		}
		phList.getLoadingLayoutProxy().setPullLabelUp(upLabel);
		phList.getLoadingLayoutProxy().setPullLabelBottom(bottomLabel);
		phList.getLoadingLayoutProxy().setReleaseLabelUp(upLabel);
		phList.getLoadingLayoutProxy().setReleaseLabelBottom(bottomLabel);
		phList.getLoadingLayoutProxy().setLastUpdatedLabel(" ");
	}
    public void setCursor(){
//        SQLiteDatabase db = trHelper.getReadableDatabase();
//
//
//
//        where = "(";
//        where += "("+TransactionEntry.COLUMN_NAME_IS_EXPENSE + "=" + 1 + ") and (";
//        for(int i = 0; i < Category.EXPENSE_SIZE; i++)
//        {
//            if(expenseSelection[i])
//            {
//                where += TransactionEntry.COLUMN_NAME_CATEGORY + "=" + i + " or ";
//            }
//        }
//        if(where.substring(where.length()-4, where.length()).equals(" or "))
//        {
//            where = where.substring(0, where.length()-4);
//            where += ")";
//        }
//        else
//        {
//            where += "1=0)";
//        }
//
//        where += " or ("+TransactionEntry.COLUMN_NAME_IS_EXPENSE + "=" + 0 + ") and (";
//        for(int i = 0; i < Category.INCOME_SIZE; i++)
//        {
//            if(incomeSelection[i])
//            {
//                where += TransactionEntry.COLUMN_NAME_CATEGORY + "=" + i + " or ";
//            }
//        }
//        if(where.substring(where.length()-4, where.length()).equals(" or "))
//        {
//            where = where.substring(0, where.length()-4);
//            where += ")";
//        }
//        else
//        {
//            where += "1=0)";
//        }
//
//        where += ")";
//
//        String startDate = "";
//        String endDate = "";
//        //TODO set cal to a custom date
////			filterDate = new PersianCalendar();
//        PersianDate date = null;
//        Time time = null;
//        int tmp;
//        switch(accountMenuAdapter.getRadioSelected())
//        {
//            case DAILY:
//                date = new PersianDate((short)filterDate.get(PersianCalendar.DAY_OF_MONTH),
//                        (short)filterDate.get(PersianCalendar.MONTH),
//                        (short)filterDate.get(PersianCalendar.YEAR), "");
//                time = new Time((short)0, (short)0);
//                startDate = date.getSTDString()+time.getSTDString();
//                time = new Time((short)99, (short)99);
//                endDate = date.getSTDString()+time.getSTDString();
//                break;
//            case WEEKLY:
//                int amount = -filterDate.get(PersianCalendar.DAY_OF_WEEK);
//                if(amount == -7)
//                    amount = 0;
//
//                filterDate.add(PersianCalendar.DATE, amount);
//                date = new PersianDate((short)filterDate.get(PersianCalendar.DAY_OF_MONTH),
//                        (short)filterDate.get(PersianCalendar.MONTH),
//                        (short)filterDate.get(PersianCalendar.YEAR), "");
//                time = new Time((short)0, (short)0);
//                startDate = date.getSTDString()+time.getSTDString();
//
//                filterDate.add(PersianCalendar.DATE, +6);
//                date = new PersianDate((short)filterDate.get(PersianCalendar.DAY_OF_MONTH),
//                        (short)filterDate.get(PersianCalendar.MONTH),
//                        (short)filterDate.get(PersianCalendar.YEAR), "");
//                time = new Time((short)99, (short)99);
//                endDate = date.getSTDString()+time.getSTDString();
//                filterDate.add(PersianCalendar.DATE, -6-amount);
//                break;
//            case MONTHLY:
//                tmp = filterDate.get(PersianCalendar.DAY_OF_MONTH);
//                filterDate.set(PersianCalendar.DAY_OF_MONTH, 1);
//                date = new PersianDate((short)filterDate.get(PersianCalendar.DAY_OF_MONTH),
//                        (short)filterDate.get(PersianCalendar.MONTH),
//                        (short)filterDate.get(PersianCalendar.YEAR), "");
//                time = new Time((short)0, (short)0);
//                startDate = date.getSTDString()+time.getSTDString();
//
//                filterDate.set(PersianCalendar.DAY_OF_MONTH,
//                        PersianCalendar.jalaliDaysInMonth[filterDate.get(PersianCalendar.MONTH)]);
//                date = new PersianDate((short)filterDate.get(PersianCalendar.DAY_OF_MONTH),
//                        (short)filterDate.get(PersianCalendar.MONTH),
//                        (short)filterDate.get(PersianCalendar.YEAR), "");
//                time = new Time((short)99, (short)99);
//                endDate = date.getSTDString()+time.getSTDString();
//                filterDate.set(PersianCalendar.DAY_OF_MONTH, tmp);
//                break;
//            case YEARLY:
//                tmp = filterDate.get(PersianCalendar.DAY_OF_YEAR);
//                filterDate.set(PersianCalendar.DAY_OF_YEAR, 1);
//                date = new PersianDate((short)filterDate.get(PersianCalendar.DAY_OF_MONTH),
//                        (short)filterDate.get(PersianCalendar.MONTH),
//                        (short)filterDate.get(PersianCalendar.YEAR), "");
//                time = new Time((short)0, (short)0);
//                startDate = date.getSTDString()+time.getSTDString();
//
//                filterDate.set(PersianCalendar.DAY_OF_YEAR,
//                        PersianCalendar.isLeapYear(filterDate.get(PersianCalendar.YEAR))? 366: 365);
//                date = new PersianDate((short)filterDate.get(PersianCalendar.DAY_OF_MONTH),
//                        (short)filterDate.get(PersianCalendar.MONTH),
//                        (short)filterDate.get(PersianCalendar.YEAR), "");
//                time = new Time((short)99, (short)99);
//                endDate = date.getSTDString()+time.getSTDString();
//                filterDate.set(PersianCalendar.DAY_OF_YEAR, tmp);
//                break;
//        }
//
//
//        where += " and "+TransactionEntry.COLUMN_NAME_DATE_TIME+">="+startDate;
//        where += " and "+TransactionEntry.COLUMN_NAME_DATE_TIME+"<="+endDate;
//
//
//        ArrayList<String> ar=new ArrayList<String>();
//        for (int i=0;i<accountMenuAdapter.getAccountsSelection().length;i++){
//            if(accountMenuAdapter.getAccountsSelection()[i])
//                ar.add(accountMenuAdapter.getSelectedAccountString(i));
//        }
//
        System.arraycopy(accountMenuAdapter.getAccountsSelection(),0,accountSelection,0,accountMenuAdapter.getAccountsSelection().length);
////        accountSelection=accountMenuAdapter.getAccountsSelection();
//
//        where += " and (";
//        for(int i = 0; i < ar.size(); i++)
//        {
//         where += "( "+TransactionEntry.COLUMN_NAME_ACCOUNT_NAME+ " = '" + ar.get(i) +"' ) "+ " or ";
//        }
//        if(where.substring(where.length()-4, where.length()).equals(" or "))
//        {
//            where = where.substring(0, where.length()-4);
//            where += ")";
//        }
//        else
//        {
//            where += "1=0)";
//        }
//
//
//
//        query = "SELECT " +TransactionEntry.TABLE_NAME+"."+TransactionEntry._ID+
//                " , "+TransactionEntry.TABLE_NAME+"."+TransactionEntry.COLUMN_NAME_TRANSACTION_ID+
//                " , "+TransactionEntry.COLUMN_NAME_DATE_TIME+
//                " , "+TransactionEntry.COLUMN_NAME_AMOUNT+
//                " , "+TransactionEntry.COLUMN_NAME_IS_EXPENSE+
//                " , "+TransactionEntry.COLUMN_NAME_DESCRIPTION+
//                " , "+TransactionEntry.COLUMN_NAME_CATEGORY+
//                " , "+TransactionEntry.COLUMN_NAME_ACCOUNT_NAME+
//                " , "+ TransactionsContract.TagsEntry.COLUMN_NAME_TAG+
//                " FROM "+TransactionEntry.TABLE_NAME+
//                " LEFT JOIN "+ TransactionsContract.TagsEntry.TABLE_NAME+
//                " ON "+TransactionEntry.TABLE_NAME+"."+TransactionEntry._ID+
//                "="+ TransactionsContract.TagsEntry.TABLE_NAME+"."+
//                TransactionsContract.TagsEntry.COLUMN_NAME_TRANSACTION_ID+
//                " WHERE "+where+
//                " ORDER BY "+TransactionEntry.COLUMN_NAME_DATE_TIME+" DESC";
//        c = db.rawQuery(query, null);
        LocalDBServices.getTransactionsFromDB(this,expenseSelection,incomeSelection,accountMenuAdapter.getRadioSelected(),accountMenuAdapter,filterDate);
    }
   private void makeItemFromCursor(Cursor c){



       c.moveToFirst();
       listItems.clear();
       if(c.getCount() == 0)
       {
           listItems = new ArrayList<TimelineItem>();
           pHAdapter.setItems(listItems);
           totalExpenseString = "  "+Utils.toPersianNumbers(Currency.getStdAmount(0.0));
           totalIncomeString = "  "+Utils.toPersianNumbers(Currency.getStdAmount(0.0));
           return;
       }

       PersianCalendar tmpCal;
       String lastTransID="null";
       ArrayList<String> tags=new ArrayList<String>();
       String id="null";
       String dateTime;
       double amount=-1;
       boolean isExpense=false;
       String descp="";
       String accountName="";
       int category_index = -1;
       int year;
       int month;
       int day;
       int hour;
       int minute;
       do
       {
           String transID=c.getString(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_TRANSACTION_ID));
           if(c.isLast()){
               if(!lastTransID.equals(transID)){
                   if(!lastTransID.equals("null"))
                   listItems.add(new TimelineItem(id,isExpense,amount,date,time,category_index,tags,false,descp,accountName));
                   tags=new ArrayList<String>();
               }
               String tagName=c.getString(c.getColumnIndexOrThrow(TransactionsContract.TagsEntry.COLUMN_NAME_TAG));
               tags.add(tagName);
               id = c.getString(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_TRANSACTION_ID));
               dateTime = c.getString(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_DATE_TIME));
               amount = c.getDouble(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_AMOUNT));
               isExpense = c.getInt(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_IS_EXPENSE))==0? false: true;
               category_index = c.getInt(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_CATEGORY));
               descp=c.getString(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_DESCRIPTION));
               accountName=c.getString(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_ACCOUNT_NAME));

               year = Integer.parseInt(dateTime.substring(0, 4));
               month = Integer.parseInt(dateTime.substring(4, 6));
               day = Integer.parseInt(dateTime.substring(6, 8));
               hour = Integer.parseInt(dateTime.substring(8, 10));
               minute = Integer.parseInt(dateTime.substring(10, 12));

               tmpCal = new PersianCalendar(year, month, day);
               date = new PersianDate((short)day, (short)(month+1), (short)year, PersianCalendar.weekdayFullNames[tmpCal.get(PersianCalendar.DAY_OF_WEEK)]);
               time = new Time((short)hour, (short)minute);

               listItems.add(new TimelineItem(id,isExpense,amount,date,time,category_index,tags,false,descp,accountName));}
           else {
               if(!lastTransID.equals(transID)){
                   if(!lastTransID.equals("null")){
                       listItems.add(new TimelineItem(id,isExpense,amount,date,time,category_index,tags,false,descp,accountName));
                   }
                   tags=new ArrayList<String>();
                   String tagName=c.getString(c.getColumnIndexOrThrow(TransactionsContract.TagsEntry.COLUMN_NAME_TAG));
                   tags.add(tagName);
                   id = c.getString(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_TRANSACTION_ID));
                   dateTime = c.getString(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_DATE_TIME));
                   amount = c.getDouble(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_AMOUNT));
                   isExpense = c.getInt(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_IS_EXPENSE))==0? false: true;
                   category_index = c.getInt(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_CATEGORY));
                   descp=c.getString(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_DESCRIPTION));
                   accountName=c.getString(c.getColumnIndexOrThrow(TransactionEntry.COLUMN_NAME_ACCOUNT_NAME));

                   year = Integer.parseInt(dateTime.substring(0, 4));
                   month = Integer.parseInt(dateTime.substring(4, 6));
                   day = Integer.parseInt(dateTime.substring(6, 8));
                   hour = Integer.parseInt(dateTime.substring(8, 10));
                   minute = Integer.parseInt(dateTime.substring(10, 12));

                   tmpCal = new PersianCalendar(year, month, day);
                   date = new PersianDate((short)day, (short)(month+1), (short)year, PersianCalendar.weekdayFullNames[tmpCal.get(PersianCalendar.DAY_OF_WEEK)]);
                   time = new Time((short)hour, (short)minute);
                   lastTransID=transID;
               }
               else {
                   String tagName=c.getString(c.getColumnIndexOrThrow(TransactionsContract.TagsEntry.COLUMN_NAME_TAG));
                   tags.add(tagName);
               }
           }
       }while(c.moveToNext());
       c.close();
       System.out.println("cursor is closed");
       pHAdapter.setItems(listItems);
   }


   private void addSlidingMenu(){

       // configure the SlidingMenu
       SlidingMenu menu = new SlidingMenu(this);
       menu.setMode(SlidingMenu.LEFT_RIGHT);
       menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
       menu.setShadowWidthRes(R.dimen.shadow_width);
       menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
       menu.setBehindOffsetRes(R.dimen.sliding_menu_behid_offset);
       menu.setOnCloseListener(new SlidingMenu.OnCloseListener()
       {
           @Override
           public void onClose()
           {
               setPullToRefreshLabels();
               try {
                   immediateRefreshTimeline();
               } catch (NumberFormatException e) {
                   e.printStackTrace();
               } catch (IllegalArgumentException e) {
                   e.printStackTrace();
               } catch (ParseException e) {
                   e.printStackTrace();
               }
               setTitleText();
           }
       });

       // left menu:
       menu.setMenu(R.layout.left_menu_layout);
       menu.setShadowDrawable(R.drawable.left_shadow);
       PinnedHeaderListView accountMenu = (PinnedHeaderListView) findViewById(R.id.account_time_filter_listview);
       accountMenuAdapter = new FilterMenuAdapter(this, null);
       accountMenuAdapter.setItems(accountsAndTimeFilter);
       accountMenu.setAdapter(accountMenuAdapter);

       // right menu:
       menu.setSecondaryMenu(R.layout.right_menu_layout);
       menu.setSecondaryShadowDrawable(R.drawable.right_shadow);

       RelativeLayout header = (RelativeLayout) findViewById(R.id.expense_header);
       ((TextView)header.findViewById(R.id.side_menu_header)).setTypeface(persianTypeface);
       ((TextView)header.findViewById(R.id.side_menu_header))
               .setText(getResources().getString(R.string.expenses));

       header = (RelativeLayout) findViewById(R.id.income_header);
       ((TextView)header.findViewById(R.id.side_menu_header)).setTypeface(persianTypeface);
       ((TextView)header.findViewById(R.id.side_menu_header))
               .setText(getResources().getString(R.string.incomes));

       GridLayout expenses = (GridLayout) findViewById(R.id.expense_grid);
       expenses.setPadding((int)(getResources().getDimension(R.dimen.shadow_width)/getResources().getDisplayMetrics().density)+5, 0,
               (int)(getResources().getDimension(R.dimen.shadow_width)/getResources().getDisplayMetrics().density)+5, 0);
       int column = getSlidingMenuNumberOfColumns(expenses, Category.getExpenseIconID(Category.EXPENSE_UNCATEGORIZED));
       expenses.setColumnCount(column);
       expenses.setPadding(getSlidingMenuRightPaddding(column, Category.getExpenseIconID(Category.EXPENSE_UNCATEGORIZED)), 0, getSlidingMenuRightPaddding(column, Category.getExpenseIconID(Category.EXPENSE_UNCATEGORIZED)), 0);
       for(int i = 0; i < Category.EXPENSE_SIZE; i++)
       {
           Spec row = GridLayout.spec(i/column);
           Spec col = GridLayout.spec(column-(i%column)-1);
           ImageButton imageButton = new ImageButton(this);
           final int tmpIndex = i;
           imageButton.setOnClickListener(new OnClickListener()
           {
               @Override
               public void onClick(View button)
               {
                   expenseSelection[tmpIndex] = !expenseSelection[tmpIndex];
                   if(!expenseSelection[tmpIndex])
                   {
                       button.getBackground().setAlpha(HALF_TRANSPARENT);
                   }
                   else
                   {
                       button.getBackground().setAlpha(FULLY_OPAQUE);
                   }
               }
           });
           imageButton.setLayoutParams(new GridLayout.LayoutParams(row, col));
           imageButton.setBackgroundResource(Category.getExpenseIconID(i));
           expenses.addView(imageButton);
       }

       GridLayout incomes = (GridLayout) findViewById(R.id.incomes_grid);
       incomes.setPadding((int)(getResources().getDimension(R.dimen.shadow_width)/getResources().getDisplayMetrics().density)+5, 0,
               (int)(getResources().getDimension(R.dimen.shadow_width)/getResources().getDisplayMetrics().density)+5, 0);
       column = getSlidingMenuNumberOfColumns(incomes, Category.getExpenseIconID(Category.EXPENSE_UNCATEGORIZED));
       incomes.setColumnCount(column);
       incomes.setPadding(getSlidingMenuRightPaddding(column, Category.getExpenseIconID(Category.EXPENSE_UNCATEGORIZED)), 0, getSlidingMenuRightPaddding(column, Category.getExpenseIconID(Category.EXPENSE_UNCATEGORIZED)), 0);
       for(int i = 0; i < Category.INCOME_SIZE; i++)
       {
           Spec row = GridLayout.spec(i/column);
           Spec col = GridLayout.spec(column-(i%column)-1);
           ImageButton imageButton = new ImageButton(this);
           final int tmpIndex = i;
           imageButton.setOnClickListener(new OnClickListener()
           {
               @Override
               public void onClick(View button)
               {
                   incomeSelection[tmpIndex] = !incomeSelection[tmpIndex];
//                   button.setSelected(!button.isSelected());
                   if(!incomeSelection[tmpIndex])
                   {
                       button.getBackground().setAlpha(HALF_TRANSPARENT);
                   }
                   else
                   {
                       button.getBackground().setAlpha(FULLY_OPAQUE);
                   }
               }
           });
           imageButton.setLayoutParams(new GridLayout.LayoutParams(row, col));
           imageButton.setBackgroundResource(Category.getIncomeIconID(i));
           incomes.addView(imageButton);
       }

   }

private String getSelectedRadioTimeString(){
    if(accountMenuAdapter.getRadioSelected()==YEARLY)
        return getResources().getString(R.string.yearly);
    else if(accountMenuAdapter.getRadioSelected()==MONTHLY)
        return getResources().getString(R.string.monthly);
    else if(accountMenuAdapter.getRadioSelected()==WEEKLY)
        return getResources().getString(R.string.weekly);
    else
        return getResources().getString(R.string.daily);
}
private  void getSharedPreferences(){
    SharedPreferences preferences=getSharedPreferences(PREFRENCES_FILE,0);
    getTokensFromDB();
//    String pfmcookie = preferences.getString("pfmcookie","");
//    String pfmtoken = preferences.getString("pfmtoken","");
//    ConnectionManager.pfmCookie=pfmcookie;
//    ConnectionManager.pfmToken=pfmtoken;
//    if(ConnectionManager.pfmCookie.equals("")){
//        SharedPreferences.Editor edit = preferences.edit();
//        edit.putBoolean("ProgramIsPreviouslyStarted", Boolean.TRUE);
//        edit.commit();
//        Intent intent = new Intent(TimelineActivity.this, LoginActivity2.class);
//        finish();
//        startActivity(intent);
//        return;
//    }
//    if(!previouslyStarted) {
//
//    }

    for(int i=0;i<accountsAndTimeFilter.size();i++){
        accountsAndTimeFilter.get(i).isChecked=preferences.getBoolean("AccountsAndTime:"+accountsAndTimeFilter.get(i).text,true);
    }
    accountMenuAdapter.setRadioSelected();
    for(int i=0;i<expenseSelection.length;i++){
        expenseSelection[i]=preferences.getBoolean("expenseCatId:"+i,true);
    }
    for(int i=0;i<incomeSelection.length;i++){
        incomeSelection[i]=preferences.getBoolean("incomeCatId:"+i,true);
    }

}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("on result called");
        if(resultCode==SettingActivity.LOGOUT_CODE)
        {
                LocalDBServices.invalidTokens(this);
                Intent intent= new Intent(TimelineActivity.this,LoginActivity2.class);
                finish();
                startActivity(intent);
                return;
        }
    }

    private void getTokensFromDB(){
        Boolean res=LocalDBServices.getTokens(this);
        if(res){
            Log.e("debug","result is ok, added to db ");
        }
        else {
            LocalDBServices.invalidTokens(this);
            Intent intent= new Intent(TimelineActivity.this,LoginActivity2.class);
            finish();
            startActivity(intent);
            return;
        }

    }
}
