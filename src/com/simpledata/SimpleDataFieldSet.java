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
import java.util.HashMap;
import com.simpledata.exception.UnknownFieldTypeException;


/**
 * Collection representing the fields in a database table
 * 
 * @author Liam Svenson
 *
 */
public class SimpleDataFieldSet extends HashMap<String, SimpleDataField> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Get a field set for a given row class
	 * 
	 * Create an empty field set, then fill it by inspecting the given row class
	 * 
	 * @param rowClass The row class that specifies the required fields
	 */
	public SimpleDataFieldSet(Class<?> rowClass) {
		Field[] fields = rowClass.getDeclaredFields();
		
		// For each declared field on the row class object, add a SimpleDataField to the collection
		for (int i  = 0; i < fields.length; i++) {
			DatabaseField field = fields[i].getAnnotation(DatabaseField.class);
			SimpleDataFieldAttributeSet attributes = new SimpleDataFieldAttributeSet();
			
			attributes.add("type", field.type());
			
			if (field.primaryKey()) {
				attributes.add("primaryKey", true);
				attributes.add("autoNumber", true);
			}
			
			//TODO: Implement other attributes
			
			
			add(fields[i].getName(), field.type(), attributes);
			
			
		}
		
	}

	/**
	 * Add a new field to the field set
	 * 
	 * @param fieldName The name of the db field
	 * @param fieldType The type of db field
	 */
	public void add(String fieldName, int fieldType) {
		try {
			put(fieldName, new SimpleDataField(fieldName, fieldType, null));
		} catch (UnknownFieldTypeException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a new field to the field set
	 * 
	 * @param fieldName The name of the db field
	 * @param fieldType The type of db field
	 * @param attributes The custom attributes of the db field
	 */
	public void add(String fieldName, int fieldType, SimpleDataFieldAttributeSet attributes) {
		try {
			put(fieldName, new SimpleDataField(fieldName, fieldType, attributes));
		} catch (UnknownFieldTypeException e) {
			e.printStackTrace();
		}
	}

}
