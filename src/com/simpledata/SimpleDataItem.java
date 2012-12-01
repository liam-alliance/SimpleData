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
import java.lang.reflect.Field;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * Super class for SimpleDataItems
 * 
 * Any class that extends this can be a row class used by a SimpleDataSet
 * Subclasses should use the custom annotations: DatabaseTable and DatabaseField to 
 * describe how their contents should be stored in the database.
 * 
 * @author Liam Svenson
 *
 */
abstract public class SimpleDataItem {

	/**
	 * Apply values from a db query cursor to this instance
	 * 
	 * @param cursor The cursor used to traverse/get db query result values
	 * @param columns The names of the columns selected by the query.  Null for all columns
	 */
	public void applyValues(Cursor cursor, String[] columns) {
		if (columns == null) {
			columns = getColumnNames();
		}
		int fieldType = -1;
		for (String columnName : cursor.getColumnNames()) {
			try {
				Field field = this.getClass().getDeclaredField(columnName);
				fieldType = field.getAnnotation(DatabaseField.class).type();
				
				switch (fieldType) {
					case SimpleDataField.FIELD_TYPE_INTEGER:
						field.set(this, cursor.getInt(cursor.getColumnIndex(columnName)));
						break;
					case SimpleDataField.FIELD_TYPE_NUMERIC:
						field.set(this, cursor.getFloat(cursor.getColumnIndex(columnName)));
						break;
					case SimpleDataField.FIELD_TYPE_TEXT:
						field.set(this, cursor.getString(cursor.getColumnIndex(columnName)));
						break;
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Inspect this instance, and return a String array of the names of all the columns in the db table
	 * @return The names of the columns in the db
	 */
	public String[] getColumnNames() {
		Field[] fields = this.getClass().getDeclaredFields();
		String[] columnNames = new String[fields.length];
		int i = 0;
		for (Field field : this.getClass().getDeclaredFields()) {
			columnNames[i++] = field.getName();
		}
		return columnNames;
	}

	/**
	 * Inspect this instance, then return all data values in a ContentValues object
	 * 
	 * @return ContentValues: The values of the fields for this instance
	 */
	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		// Loop over each declared field in the class
		for (Field field : this.getClass().getDeclaredFields()) {
			try {
				int fieldType = field.getAnnotation(DatabaseField.class).type();
				switch (fieldType) {
				case SimpleDataField.FIELD_TYPE_INTEGER:
					int val = field.getInt(this);
					if (val == 0) { 
						values.put(field.getName(), (String) null);
					} else {
						values.put(field.getName(), field.getInt(this));
					}
					break;
				case SimpleDataField.FIELD_TYPE_NUMERIC:
					values.put(field.getName(), field.getDouble(this));
					break;
				case SimpleDataField.FIELD_TYPE_TEXT:
					values.put(field.getName(), (String) field.get(this));
					break;
				}
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return values;
	}
	
	/**
	 * Get the int value of the primary key for this instance
	 *  
	 * @param String primaryKeyFieldName The name of the field that is the primary key 
	 * @return int The primary key value for this instance
	 */
	public int getPrimaryKeyValue(String primaryKeyFieldName) {
		try {
			Field field = this.getClass().getDeclaredField(primaryKeyFieldName);
			return field.getInt(this);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
