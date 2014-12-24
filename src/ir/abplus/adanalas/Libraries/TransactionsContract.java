package ir.abplus.adanalas.Libraries;

import android.provider.BaseColumns;

public final class TransactionsContract
{
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public TransactionsContract()
	{
	}

	/* Inner class that defines the table contents */
	public static abstract class TransactionEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "transactions";
		public static final String COLUMN_NAME_TRANSACTION_ID = "transaction_id";
		public static final String COLUMN_NAME_DATE_TIME = "transaction_date_time";
		public static final String COLUMN_NAME_AMOUNT = "transaction_amount";
		public static final String COLUMN_NAME_IS_EXPENSE = "transaction_is_expense";
		public static final String COLUMN_NAME_CATEGORY = "transaction_category";
		public static final String COLUMN_NAME_DESCRIPTION = "transaction_description";
		public static final String COLUMN_NAME_ACCOUNT_NAME = "transaction_account_name";
	}
	
	public static abstract class TagsEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "tags";
		public static final String COLUMN_NAME_TRANSACTION_ID = "transaction_id";
		public static final String COLUMN_NAME_TAG = "tag_name";
	}

    public static abstract class Accounts implements BaseColumns
    {
        public static final String TABLE_NAME = "accounts";
        public static final String COLUMN_NAME_Account_ID = "Account_ID";
        public static final String COLUMN_NAME_Account_Name = "Account_Name";
        public static final String COLUMN_NAME_Account_Type = "Account_Type";
        //account type will be 1 if it's editable or 0 if it's not!
    }

	private static final String INTEGER_TYPE = " INTEGER";
	private static final String DATE_TIME_TYPE = " TEXT";
	private static final String FLOAT_TYPE = " REAL";
	private static final String BOOLEAN_TYPE = " INTEGER";
	private static final String TEXT_TYPE = " TEXT";

    public static final String SQL_CREATE_TRANSACTIONS =
			"CREATE TABLE IF NOT EXISTS " + TransactionEntry.TABLE_NAME + " (\n" +
					TransactionEntry._ID + " INTEGER PRIMARY KEY,\n" +
					TransactionEntry.COLUMN_NAME_TRANSACTION_ID + INTEGER_TYPE + ",\n" +
					TransactionEntry.COLUMN_NAME_DATE_TIME + DATE_TIME_TYPE + ",\n" +
					TransactionEntry.COLUMN_NAME_AMOUNT + FLOAT_TYPE + ",\n" +
					TransactionEntry.COLUMN_NAME_IS_EXPENSE + BOOLEAN_TYPE + ",\n" +
					TransactionEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + ",\n" +
					TransactionEntry.COLUMN_NAME_ACCOUNT_NAME+ TEXT_TYPE + ",\n" +
					TransactionEntry.COLUMN_NAME_CATEGORY + INTEGER_TYPE +
					" )";

	public static final String SQL_DELETE_TRANSACTIONS =
			"DROP TABLE IF EXISTS " + TransactionEntry.TABLE_NAME;
	
	public static final String SQL_CREATE_TAGS =
			"CREATE TABLE IF NOT EXISTS " + TagsEntry.TABLE_NAME + " (\n" +
					TagsEntry._ID + " INTEGER PRIMARY KEY,\n" +
					TagsEntry.COLUMN_NAME_TRANSACTION_ID + INTEGER_TYPE + ",\n" +
					TagsEntry.COLUMN_NAME_TAG + TEXT_TYPE +
					" )";

    public static final String SQL_CREATE_ACCOUNTS =
            "CREATE TABLE IF NOT EXISTS " + Accounts.TABLE_NAME + " (\n" +
                    Accounts._ID + " INTEGER PRIMARY KEY,\n" +
                    Accounts.COLUMN_NAME_Account_ID + INTEGER_TYPE + ",\n" +
                    Accounts.COLUMN_NAME_Account_Name + TEXT_TYPE + ",\n" +
                    Accounts.COLUMN_NAME_Account_Type + TEXT_TYPE + ",\n" +
                    " UNIQUE( "+Accounts.COLUMN_NAME_Account_Name+" ) "+
                    " )";


    public static final String SQL_DELETE_TAGS =
			"DROP TABLE IF EXISTS " + TagsEntry.TABLE_NAME;

    public static final String SQL_DELETE_ACCOUNTS =
            "DROP TABLE IF EXISTS " + Accounts.TABLE_NAME;
}
