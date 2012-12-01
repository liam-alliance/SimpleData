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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Custom annotation to allow setting database field properties
 * 
 * Subclasses of SimpleDataItem should use this custom annotation to provide
 * metadata about the fields that are stored in the database
 * 
 * @author Liam Svenson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseField {

	// Field type
	int type();
	
	// Is this field a primary key?
	boolean primaryKey() default false;

	// Is this field an autonumber?
	boolean autoNumber() default false;
	
	// Is this field required?
	boolean required() default false;

	// What is the maximum length of a text value 
	// (only applies where type = SimpleDataField.FIELD_TYPE_TEXT)
	int maxLength() default 0;

	// What is the minimum allowed value
	// (only applies where type = SimpleDataField.FIELD_TYPE_INTEGER) 
	//  or SimpleDataField.FIELD_TYPE_NUMERIC)
	int min() default -1;
	
	// What is the maximum allowed value
	// (only applies where type = SimpleDataField.FIELD_TYPE_INTEGER) 
	//  or SimpleDataField.FIELD_TYPE_NUMERIC)
	int max() default -1;
	
	// What precision is required?
	// (only applies where type = SimpleDataField.FIELD_TYPE_NUMERIC)
	int precision() default 4;

}
