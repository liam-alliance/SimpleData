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
import java.util.ArrayList;

/**
 * Helper class that represents all the options that could be used by an sql query
 * 
 * Creating an empty instance is the equivalent of a select all for the given table.
 * Calling other methods will apply relevant clauses to the select statement.
 * These other methods are chainable, see below for some examples:
 * 
 * Examples:
 * SimpleDataQuery query = new SimpleDataQuery().where("somefield = ?", 36).orderBy("someOtherField");
 * SimpleDataQuery query = new SimpleDataQuery().where("somefield = ?", "Some value").orderBy("someOtherField").setLimit(12);
 * SimpleDataQuery query = new SimpleDataQuery().where("somefield = ? and otherField = ?", {"val 1", "val 2"}).setOffset(10);
 * SimpleDataQuery query = new SimpleDataQuery().where("somefield = ?", 36).setGroupBy("someOtherField").addHaving("someOtherField > 5").addHaving("differentField < 5");
 * 
 * @TODO Need to add some further methods to this to give more control over the query
 * @author Liam Svenson
 *
 */
public class SimpleDataQuery {

	public String tableName;
	
	public ArrayList<String> fieldsToSelect;
	
	public String whereClause;
	public ArrayList<String> whereParams;
	
	public String groupByClause;
	
	public ArrayList<String> havingClauses;
	
	public ArrayList<String> orderByClauses;
	
	public int limit;
	
	public int offset;
	
	/**
	 * Create a simple data query
	 * 
	 * @param selectFromTableName Optional table name override.  
	 *                            Defaults to the table name of the calling SimpleDataSet 
	 */
	public SimpleDataQuery(String selectFromTableName) {
		tableName = selectFromTableName;
		init();
	}
	
	/**
	 * Create a simple data query
	 */
	public SimpleDataQuery() {
		tableName = null;
		init();		
	}
	
	/**
	 * Initialize default values for the query
	 */
	private void init() {
		fieldsToSelect = null; // defaults to *
		
		whereClause = null;
		whereParams = null;
		
		groupByClause = null;
		
		havingClauses = null;
		
		orderByClauses = null;
		
		limit = 0;
		offset = 0;

	}
	
	/**
	 * Add a where clause to the query
	 * 
	 * If you have parameters, you should call one of the other where methods, instead
	 * of putting your parameter value in this where clause string.
	 * 
	 * @param whereClauseStr
	 * @return This SimpleDataQuery instance
	 */
	public SimpleDataQuery where(String whereClauseStr) {
		whereClause = whereClauseStr;
		whereParams = new ArrayList<String>();
		return this;
	}
	
	/**
	 * Add a where clause to the query
	 * 
	 * Use this if you have a single parameter in your where clause string (parameters denoted by '?')
	 * AND if the type of the value is a string.
	 * 
	 * @param whereClauseStr    The where clause, eg "someStringValue = ?"
	 * @param whereClauseParam  The string value of the single parameter in the where clause string
	 * @return This SimpleDataQuery instance
	 */
	public SimpleDataQuery where(String whereClauseStr, String whereClauseParam) {
		if (whereParams == null) {
			whereParams = new ArrayList<String>();
		}
		whereClause = whereClauseStr;
		whereParams.add(whereClauseParam);
		return this;
	}
	
	/**
	 * Add a where clause to the query
	 * 
	 * Use this if you have a single parameter in your where clause string (parameters denoted by '?')
	 * AND if the type of the value is a int.
	 * 
	 * @param whereClauseStr    The where clause, eg "someIntValue = ?"
	 * @param whereClauseParam  The int value of the single parameter in the where clause string
	 * @return This SimpleDataQuery instance
	 */
	public SimpleDataQuery where(String whereClauseStr, int whereClauseParam) {
		if (whereParams == null) {
			whereParams = new ArrayList<String>();
		}
		whereClause = whereClauseStr;
		whereParams.add(String.valueOf(whereClauseParam));
		return this;
	}
	
	/**
	 * Add a where clause to the query
	 * 
	 * Use this if you have many parameters in your where clause string (parameters denoted by '?')
	 * AND if the type of each of the parameters is a string.
	 * 
	 * @TODO:  Update this so it can handle varying types of params 
	 * 
	 * @param whereClauseStr    The where clause, eg "someIntValue = ?"
	 * @param whereClauseParam  The int value of the single parameter in the where clause string
	 * @return This SimpleDataQuery instance
	 */
	public SimpleDataQuery where(String whereClauseStr, ArrayList<String> whereClauseParams) {
		whereClause = whereClauseStr;
		whereParams = whereClauseParams;
		return this;
	}
	
	/**
	 * Set the group by clause to be used in the query
	 * 
	 * @param groupBy A string to define the group by clause
	 * @returnThis SimpleDataQuery instance
	 */
	public SimpleDataQuery setGroupBy(String groupBy) {
		groupByClause = groupBy;
		return this;
	}
	
	/**
	 * Add a having clause to the query
	 * 
	 * @param havingStr The having clause to be used in the query
	 * @return This SimpleDataQuery instance
	 */
	public SimpleDataQuery addHaving(String havingStr) {
		if (havingClauses == null) {
			havingClauses = new ArrayList<String>();
		}
		havingClauses.add(havingStr);
		return this;
	}
	
	/**
	 * Remove any existing having clauses, and add the provided clause
	 * 
	 * @param havingStr The new having clause to replace any existing having clauses
	 * @return This SimpleDataQuery instance
	 */
	public SimpleDataQuery setHaving(String havingStr) {
		if (havingClauses == null) {
			havingClauses = new ArrayList<String>();
		} else {
			havingClauses.clear();
		}
		havingClauses.add(havingStr);
		return this;
	}
	
	/**
	 * Add an order by clause to the query
	 * 
	 * @param orderByStr  The order by clause, eg "someField" or "someField DESC"
	 * @return This SimpleDataQuery instance
	 */
	public SimpleDataQuery addOrderBy(String orderByStr) {
		if (orderByClauses == null) {
			orderByClauses = new ArrayList<String>();
		}
		orderByClauses.add(orderByStr);
		return this;
	}
	
	/**
	 * Remove any existing order by clauses, and add the provided clause
	 *  
	 * @param orderByStr The new order by clause to replace any existing order by clauses
	 * @return This SimpleDataQuery instance
	 */
	public SimpleDataQuery setOrderBy(String orderByStr) {
		if (orderByClauses == null) {
			orderByClauses = new ArrayList<String>();
		} else {
			orderByClauses.clear();
		}
		orderByClauses.add(orderByStr);
		return this;
	}
	
	/**
	 * Set the limit of number of rows to return in the query
	 * 
	 * @param newLimit
	 * @return This SimpleDataQuery instance
	 */
	public SimpleDataQuery setLimit(int newLimit) {
		limit = newLimit;
		return this;
	}
	
	/**
	 * Set the offset for the query
	 * @param newOffset
	 * @return This SimpleDataQuery instance
	 */
	public SimpleDataQuery setOffset(int newOffset) {
		offset = newOffset;
		return this;
	}
	

	// Below are methods used when executing the query
	
	/**
	 * Get the required columns for the query
	 * @return String[] column names
	 */
	public String[] getColumns() {
		if (fieldsToSelect == null) {
			return null;
		}
		
		return (String[]) fieldsToSelect.toArray();
	}

	/**
	 * Get the array of where clause params
	 * @TODO Update this to work with types other than String
	 * @return Array of where clause parameters
	 */
	public String[] getWhereClauseParams() {
		if (whereParams == null) {
			return null;
		}
		String[] params = new String[whereParams.size()];
		params = whereParams.toArray(params);
		return params;
	}
	
	/**
	 * Get the group by clause to be used in the query
	 * @return The group by clause
	 */
	public String getGroupBy() {
		if (groupByClause == null) {
			return null;
		}
		return groupByClause;
	}
	
	/**
	 * Get the having clauses to be used in the query
	 * @return
	 */
	public String getHaving() {
		if (havingClauses == null) {
			return null;
		}
		return havingClauses.toString();
	}
	
	/**
	 * Get the order by clause to be used in the query
	 *
	 * @return
	 */
	public String getOrrderBy() {
		if (orderByClauses == null) {
			return null;
		}
		return orderByClauses.toString();
	}

	
	

}
