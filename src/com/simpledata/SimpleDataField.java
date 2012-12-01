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
import com.simpledata.exception.UnknownFieldTypeException;

/**
 * 
 * Class to represent a field on a db table
 * 
 * @author Liam Svenson
 *
 */
public class SimpleDataField {

	// Field Type Constants
	public static final int FIELD_TYPE_INTEGER	= 0;
	public static final int FIELD_TYPE_TEXT	= 1;
	public static final int FIELD_TYPE_NUMERIC	= 2;
	
	// Field Meta Data
	public String name;
	public int type;
	public SimpleDataFieldAttributeSet attributes;
	
	/**
	 * Get an instance of a field for a db table
	 * 
	 * @param String fieldName  The name of the db field
	 * @param int fieldType  The data type of the field
	 * @param SimpleDataFieldAttributeSet fieldAttributes Custom attributes for the field, see DatabaseField annotation
	 * @throws UnknownFieldTypeException If an unknown field type is specified
	 */
	public SimpleDataField(String fieldName, int fieldType, SimpleDataFieldAttributeSet fieldAttributes) throws UnknownFieldTypeException {
		
		// Check specified field type is ok
		validateFieldType(fieldType);
		
		name = fieldName;
		type = fieldType;
		attributes = fieldAttributes;
	}
	
	/**
	 * Get an instance of a field for a db table
	 * 
	 * @param String fieldName  The name of the db field
	 * @param int fieldType  The data type of the field
	 * @throws UnknownFieldTypeException If an unknown field type is specified
	 */
	public SimpleDataField(String fieldName, int fieldType) throws UnknownFieldTypeException {
		// Check specified field type is ok
		validateFieldType(fieldType);
		name = fieldName;
		type = fieldType;
	}
	
	/**
	 * Get the sqlite data type name for the specified field type
	 * 
	 * @return String data type name
	 */
	public String getTypeName() {
		switch(type) {
		case FIELD_TYPE_INTEGER:
			return "INTEGER";
		case FIELD_TYPE_TEXT:
			return "TEXT";
		case FIELD_TYPE_NUMERIC: 
			return "FLOAT";
		default:
			return "TEXT";
		}
	}
	
	/**
	 * Check that the specified field type is a valid one
	 * 
	 * @param int fieldType  The type of field
	 * @return boolean true if field is valid
	 * @throws UnknownFieldTypeException if specified field is invalid
	 */
	private boolean validateFieldType(int fieldType) throws UnknownFieldTypeException {
		switch (fieldType) {
		case FIELD_TYPE_INTEGER:
		case FIELD_TYPE_TEXT:
		case FIELD_TYPE_NUMERIC:
			break;
		default:
			throw new UnknownFieldTypeException();
		}
		return true;
	}
	
}
