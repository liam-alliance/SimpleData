/**
 * 
 * Copyright (C) 2012 Liam Svenson
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 */

package com.simpledata;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple Data Set
 * 
 * Class that handles SQLite connections, executing queries and parsing results
 * 
 * @author Liam Svenson
 *
 */
public class SimpleDataSet {

	// Reference to the application context and data source
	protected Context context;
	private SimpleDataSource source;
	
	// Collection of data update listeners
	private ArrayList<SimpleDataSetUpdateListener> registeredListeners;
	
	// DB Table info 
	public String tableName; 
	public String primaryKeyFieldName;
	public SimpleDataFieldSet fields;
	public Class<?> rowClass;
	
	// Vars for storing results/result info
	public int resultCount;
	public Iterator<SimpleDataItem> results;
	
	// Flags for controlling open state and automatic notifications
	private boolean keepOpen;
	public boolean suppressUpdateNotifications;
	
	/**
	 * Create new Simple Data Set
	 * 
	 * @param applicationContext  The context that the data source will be opened in
	 * @param dataRowClass The type of the SimpleDataItem subclass that represents a row in the table
	 */
	public SimpleDataSet(Context applicationContext, Class<?> dataRowClass) {
		init(applicationContext, dataRowClass, false);
	}
	
	/**
	 * Create new Simple Data Set
	 * 
	 * @param applicationContext  The context that the data source will be opened in
	 * @param dataRowClass The type of the SimpleDataItem subclass that represents a row in the table
	 * @param boolean keepConnectionOpen If set to true, the data source will not be closed automatically
	 *                                   You will need to close the source manually by calling the close() method
	 */
	public SimpleDataSet(Context applicationContext, Class<?> dataRowClass, boolean keepConnectionOpen) {
		init(applicationContext, dataRowClass, keepConnectionOpen);
	}
	
	
	/**
	 * Initialize the data set
	 * 
	 * Set up connection helpers, data source, various flags
	 * 
	 * @param applicationContext  The context that the data source will be opened in
	 * @param dataRowClass        The type of the SimpleDataItem subclass that represents a row in the table 
	 * @param keepConnectionOpen  If set to true, the data source will not be closed automatically
	 *                            You will need to close the source manually by calling the close() method
	 */
	private void init(Context applicationContext, Class<?> dataRowClass, boolean keepConnectionOpen) {
		// Set the context and row class to be used
		context = applicationContext;
		rowClass = dataRowClass;
		
		// Create an empty ArrayList to store any data update listeners
		registeredListeners = new ArrayList<SimpleDataSetUpdateListener>();
		
		// Create an empty field set
		fields = new SimpleDataFieldSet(rowClass);
		
		// Inspect the dataRowClass to find out about the database table we'll be working on
		
		tableName = rowClass.getAnnotation(DatabaseTable.class).tableName();
		primaryKeyFieldName = rowClass.getAnnotation(DatabaseTable.class).primaryKeyFieldName();
		
		// Create and open the data source to query
		source = new SimpleDataSource(context);
		source.open();
		
		// Set whether or not the connection should be kept open after a data query
		keepOpen = keepConnectionOpen;
		
		// Set whether or not update listeners are automatically notified.
		// If you are making multiple updates to a dataset, and only want listeners to be notified
		// once all your updates are complete, set this flag to true, then manually trigger the 
		// update notifications when your updates have finished.
		suppressUpdateNotifications = false;
		
		// Set the result content to null.  It will be filled if a select is run
		results = null;
		resultCount = -1;
	}
	

	/**
	 * Register an Update Listener
	 * 
	 * @param JesterView view
	 */
	public void registerUpdateListener(SimpleDataSetUpdateListener listener) {
		if (!registeredListeners.contains(listener)) {
			registeredListeners.add(listener);
		}
	}
	
	/**
	 * Unregister an update listener
	 * @param String viewName the name of the JesterView
	 */
	public void unregisterUpdateListener(SimpleDataSetUpdateListener listener) {
		if (registeredListeners.contains(listener)) {
			registeredListeners.remove(listener);
		}
	}
	
	/**
	 * Run the onDataUpdate method on each update listener
	 * 
	 * This will be run automatically, unless suppressUpdateNotifications is set to false
	 * If you set suppressUpdateNotifications to false, you should manually call this
	 * method when you would like listeners to be notified.
	 */
	public void notifyUpdateListeners() {
		for (SimpleDataSetUpdateListener listener : registeredListeners) {
			listener.onDataUpdate();
		}
	}
	
	
	/**
	 * Close the data source
	 * 
	 * If the data source is not closed, you will get a read error when you
	 * next try to access the table.
	 * 
	 * This is called automatically after any query (select, insert, update or delete),
	 * unless the dataset was opened with keepConnectionOpen = true.
	 * 
	 * If keepConnectionOpen is set to true, you *MUST* manually call this at the
	 * appropriate time.
	 */
	public void close() {
		if (source != null) {
			source.close();
			source.database.close();
		}
	}
	
	/**
	 * Open the data set
	 * 
	 * The data set is opened automatically, this is used to reopen the connection
	 * after the application was paused
	 */
	public void open() {
		if (source != null) {
			source.open();
		}
	}
	
	
	/**
	 * Get an ArrayList of JesterDataItems from the relevant sqlite table
	 * 
	 * @param SimpleDataQuery query Query parameters for the select statement
	 * @return ArrayList<SimpleDataItem> A collection of the select results
	 */
	public ArrayList<SimpleDataItem> select(SimpleDataQuery query) {
		
		// Run the query, returning a results cursor
		Cursor cursor = source.database.query(
				tableName, 
				query.getColumns(), 
				query.whereClause, query.getWhereClauseParams(), 
				query.getGroupBy(), query.getHaving(), 
				query.getOrrderBy());
		
		cursor.moveToFirst();

		// Get the constructor for our SimpleDataItem subclass
		Class<?>[] types = { };
		Constructor<?> rowClassConstructor = null;
		try {
			rowClassConstructor = rowClass.getConstructor(types);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		// Create ArrayList to store our SimpleDataItems
		ArrayList<SimpleDataItem> resultItems = new ArrayList<SimpleDataItem>();
		
		// Loop through the result set, and create an instance of rowClass for each row
		while (!cursor.isAfterLast()) {
			try {
				SimpleDataItem item = (SimpleDataItem) rowClassConstructor.newInstance();
				// Set the values on the new instance
				item.applyValues(cursor, query.getColumns());
				resultItems.add(item);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
	
			cursor.moveToNext();
		}
		// Make sure to close the cursor, and data source if necessary
		cursor.close();
		if (!keepOpen) {
			source.close();
		}
		
		// Set the ArrayList iterator as this DataSets result object.
		results = resultItems.iterator();
		resultCount = resultItems.size();
		
		return resultItems;
	}

	/**
	 * Select all rows from the table
	 * @return ArrayList<SimpleDataItem> All rows from the table, as instances of SimpleDataItem
	 */
	public ArrayList<SimpleDataItem> selectAll() {
		return select(new SimpleDataQuery());
	}
	
	/**
	 * Select the first item in the table
	 * @return SimpleDataItem or null. 
	 */
	public SimpleDataItem selectFirst() {
		ArrayList<SimpleDataItem> items = select(new SimpleDataQuery().setLimit(1));
		if (items.size() == 0) {
			return null;
		}
		return items.get(0);
	}
	
	
	
	/**
	 * Delete specified rows from the table 
	 * 
	 * Delete rows in the table, according to the where components of a SimpleDataQuery
	 * 
	 * If the keepOpen flag is set to false, close the data source immediately after 
	 * deleting the specified rows
	 * 
	 * @param query The query that contains where params for selecting rows to delete.
	 */
	public void delete(SimpleDataQuery query) {
		source.database.delete(tableName, query.whereClause, query.getWhereClauseParams());
		if (!keepOpen) {
			source.close();
		}
		if (!suppressUpdateNotifications) {
			notifyUpdateListeners();
		}
	}
	
	/**
	 * Delete rows from the database for all given SimpleDataItem
	 * 
	 * Iterate through and delete each of the supplied SimpleDataItems.
	 * 
	 * Force the keepOpen flag to true while each delete runs, then set back to original value
	 * Suppress update notifications until all items have been deleted.
	 * 
	 * If the keepOpen flag is set to false, close the data source immediately after
	 * deleting the specified items.
	 * If the suppressUpdateNotifications is set to false, notify listeners that the data set has changed
	 * 
	 * @param ArrayList<SimpleDataItem> items  Items to be deleted from the db
	 */
	public void delete(ArrayList<SimpleDataItem> items) {
		boolean originalKeepOpen = keepOpen;
		boolean originalSupressUpdateNotifications = suppressUpdateNotifications;
		
		keepOpen = true;
		suppressUpdateNotifications = true;
		
		Iterator<SimpleDataItem> iterator = items.iterator();
		while (iterator.hasNext()) {
			delete(iterator.next());
		}
		
		keepOpen = originalKeepOpen;
		suppressUpdateNotifications = originalSupressUpdateNotifications;
		
		if (!keepOpen) {
			source.close();
		}
		if (!suppressUpdateNotifications) {
			notifyUpdateListeners();
		}
	}
	
	/**
	 * Delete the provided SimpleDataItem from the db
	 * 
	 * If the keepOpen flag is set to false, close the data source immediately after deleting the specified item
	 * If the suppressUpdateNotifications is set to false, notify listeners that the data set has changed
	 * 
	 * @param SimpleDataItem item Item to be deleted.
	 */
	public void delete(SimpleDataItem item) {
		SimpleDataQuery query = new SimpleDataQuery().where(primaryKeyFieldName+" = ?", item.getPrimaryKeyValue(primaryKeyFieldName));
		source.database.delete(tableName, query.whereClause, query.getWhereClauseParams());
		if (!keepOpen) {
			source.close();
		}
		if (!suppressUpdateNotifications) {
			notifyUpdateListeners();
		}
	}
	
	
	/**
	 * Empty the db table, and reset the autonumber index
	 * 
	 * If the keepOpen flag is set to false, close the data source immediately after emptying the table
	 */
	public void empty() {
		// empty the table by calling delete with no where params
		this.delete(new SimpleDataQuery());
		
		// reset the autonumber index
		source.database.execSQL(SimpleDataSqlStrings.getResetAutonumberString(tableName));
		
		if (!keepOpen) {
			source.close();
		}
	}
	
	
	
	/**
	 * Insert an ArrayList of SimpleDataItems into the database
	 * 
	 * Force the keepOpen flag to true while each insert runs, then set back to original value
	 * Suppress update notifications until all items have been inserted.
	 * 
	 * If the keepOpen flag is set to false, close the data source immediately after all inserts complete
	 * If the suppressUpdateNotifications is set to false, notify listeners that the data set has changed
	 * 
	 * @param items
	 */
	public void insert(ArrayList<SimpleDataItem> items) {
		boolean originalKeepOpen = keepOpen;
		boolean originalSupressUpdateNotifications = suppressUpdateNotifications;
		
		keepOpen = true;
		suppressUpdateNotifications = true;
		
		Iterator<SimpleDataItem> iterator = items.iterator();
		while (iterator.hasNext()) {
			insert((SimpleDataItem)iterator.next());
		}
		
		keepOpen = originalKeepOpen;
		suppressUpdateNotifications = originalSupressUpdateNotifications;
		
		if (!keepOpen) {
			source.close();
		}
		if (!suppressUpdateNotifications) {
			notifyUpdateListeners();
		}
	}
	
	/**
	 * Insert a row into the db table for the given SimpleDataItem
	 * 
	 * If the keepOpen flag is set to false, close the data source immediately after
	 * If the suppressUpdateNotifications is set to false, notify listeners that the data set has changed
	 * 
	 * @param item
	 * @return
	 */
	public int insert(SimpleDataItem item) {
		int newId = (int) source.database.insert(tableName, null, item.getValues());
		if (!keepOpen) {
			source.close();
		}
		if (!suppressUpdateNotifications) {
			notifyUpdateListeners();
		}
		return newId;
	}
	
	/**
	 * Perform an update on each of the given SimpleDataItems
	 * 
	 * Force the keepOpen flag to true while each update runs, then set back to original value
	 * Suppress update notifications until all items have been updated.
	 * 
	 * * If the keepOpen flag is set to false, close the data source immediately after
	 * If the suppressUpdateNotifications is set to false, notify listeners that the data set has changed
	 * 
	 * @param ArrayList<SimpleDataItem> items - Items to update in the db
	 */
	public void update(ArrayList<SimpleDataItem> items) {
		boolean originalKeepOpen = keepOpen;
		boolean originalSupressUpdateNotifications = suppressUpdateNotifications;
		
		keepOpen = true;
		suppressUpdateNotifications = true;
		
		Iterator<SimpleDataItem> iterator = items.iterator();
		while (iterator.hasNext()) {
			update(iterator.next());
		}
		
		keepOpen = originalKeepOpen;
		suppressUpdateNotifications = originalSupressUpdateNotifications;
		
		if (!keepOpen) {
			source.close();
		}
		if (!suppressUpdateNotifications) {
			notifyUpdateListeners();
		}
	}
	
	
	/**
	 * Perform update the sqlite row for the given SimpleDataItem
	 * 
	 * For the given item, create an update query and execute it.
	 * If the keepOpen flag is set to false, close the data source immediately after
	 * If the suppressUpdateNotifications is set to false, notify listeners that the data set has changed
	 * 
	 * @param SimpleDataItem item Item to update in the db
	 */
	public void update(SimpleDataItem item) {
		Field pkField;
		try {
			// Get the primary key field and its value for this item
			pkField = item.getClass().getDeclaredField(primaryKeyFieldName);
			int pkValue = pkField.getInt(item);
			
			// Create a data query to update the corresponding table row
			SimpleDataQuery query = new SimpleDataQuery(tableName).where(primaryKeyFieldName+" = ?", pkValue);
			source.database.update(tableName, item.getValues(), query.whereClause, query.getWhereClauseParams());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (!keepOpen) {
			source.close();
		}
		if (!suppressUpdateNotifications) {
			notifyUpdateListeners();
		}
	}
	
	/**
	 * Finalize
	 * If the developer has set the keepOpen flag to true, they must manually 
	 * close the data connection.  If they haven't closed it by the time 
	 * this method runs, throw an exception.
	 * 
	 * Note: This method isn't guaranteed to run.
	 * If it was guaranteed to run, we'd automatically close the source
	 * As it may take a long time to run, or not run at all, we should just throw 
	 * an exception to highlight to the dev that they haven't closed the source correctly
	 */
	@Override
	protected void finalize() throws Throwable {
		// 
	//	if (source.isOpen) {
	//		new Exception("Data Set for "+tableName+" was not closed properly").printStackTrace();
	//	}
		source.close();
		
		super.finalize();
	}

	
	/**
	 * Simple Data Source - Internal Data Source Class
	 * 
	 * Wraps SQLite database open and close functionality
	 * 
	 * @author Liam Svenson
	 *
	 */
	private class SimpleDataSource {
		
		private SQLiteDatabase database = null;
		private SimpleSQLiteOpenHelper dbHelper;
		
		public boolean isOpen;

		public SimpleDataSource(Context context) {
			dbHelper = new SimpleSQLiteOpenHelper(context);
			open();
		}

		public void open() throws SQLException {
			database = dbHelper.getWritableDatabase();
			isOpen = true;
		}

		public void close() {
			dbHelper.close();
			database.close();
			isOpen = false;
		}
		
	}
	
	/**
	 * Simple SQLite Open Helper
	 * 
	 * Helper class to handle opening an sqlite database connection
	 * @author Liam Svenson
	 *
	 */
	private class SimpleSQLiteOpenHelper  extends SQLiteOpenHelper {

		public SimpleSQLiteOpenHelper(Context context) {
			super(context, tableName, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SimpleDataSqlStrings.getCreateString(tableName, fields));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(SimpleDataSqlStrings.getDropString(tableName));
			onCreate(db);
		}
		
	}
	
}
