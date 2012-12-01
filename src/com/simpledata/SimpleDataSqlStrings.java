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

/**
 * Class that provides sql strings for various db table functions
 * 
 * @author Liam Svenson
 *
 */
public class SimpleDataSqlStrings {

	/**
	 * Get a create table sql string for the given table name and fields
	 * 
	 * @param String tableName the name of the table to create
	 * @param SimpleDataFieldSet fields Collection of fields for the table
	 * @return String Create table sql string
	 */
	public static String getCreateString(String tableName, SimpleDataFieldSet fields) {
		String statement = "CREATE TABLE "+tableName+ " (";
		int numFields = fields.size();
		int fieldNum = 0;
		
		for (SimpleDataField field : fields.values()) {
			
			fieldNum++;
			SimpleDataFieldAttributeSet attributes = field.attributes;
		    
		    statement += field.name + " " + field.getTypeName() + " ";
		    
		    if (attributes.containsKey("primaryKey") && attributes.get("primaryKey") != null) {
		    	statement += "primary key autoincrement ";
		    }
		    if (attributes.containsKey("required") && attributes.get("required") != null) {
		    	statement += "not null ";
		    }
		    //TODO: implement size and length constraints
		    if (fieldNum != numFields) {
				statement += ", ";
			}
		    
		}
		statement += ")";

		return statement;
	}
	
	/**
	 * Get sql string to drop the specified table
	 * 
	 * @param String tableName The name of the table to be dropped
	 * @return String Drop table sql string
	 */
	public static String getDropString(String tableName) {
		return "DROP TABLE IF EXISTS "+tableName;
	}
	
	/**
	 * Get sql string to reset the autonumber counts for a table
	 * 
	 * @param String tableName name of table for which autonumbers should be reset
	 * @return String Sql string to reset autonumbers
	 */
	public static String getResetAutonumberString(String tableName) {
		return "DELETE FROM sqlite_sequence where name='"+tableName+"'";
	}
	
}
