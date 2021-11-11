package com.raqsoft.lib.olap4j.xmla;

import java.util.regex.Matcher;

import com.raqsoft.lib.olap4j.function.Utils;

public class XMLATreeNodeField {
	private String valueEditName;
	private Object firstEditValue;
	private Integer firstCompare = Integer.valueOf(0);
	private Object secondEditValue;
	private Integer secondCompare;
	private Integer lastCompare;
	private Object lastEditValue;
	public static final String XMLATREENODEFIELD_TAG = "XMLATreeNodeField";
	public static final XMLATreeNodeField EMPTY = null;

	public XMLATreeNodeField() {
		
	}
	public XMLATreeNodeField(String value) {
		Matcher m[] = new Matcher[1];
		//String v = value.toUpperCase();
		//if (!isMatch(hdfsUrl, "hdfs:\\/\\/(.*?):(\\d+)[\\/.*?]", m)){
		// ((\\[\w+\\])\\.?)+
		if (Utils.isMatch(value, "(.+) ([!><=.]+|CONTAINS|NOT_CONTAINS|IN|NOT_IN|BEGINS_WITH) (.+)", m)){
			if ( 3==m[0].groupCount()){
				firstEditValue = m[0].group(1);
				secondEditValue = m[0].group(3);
				// EQUALS(0, "="),  NOT_EQUAL(1, "<>"),  GREATER_THAN(2, ">"),  GREATER_THAN_OR_EQUAL(3, ">="),  LESS_THAN(4, "<"),  LESS_THAN_OR_EQUAL(5, "<="),  CONTAINS(10),  NOT_CONTAIN(11),  BEGINS_WITH(6),  IN(12),  NOT_IN(13);
				if ( m[0].group(2).compareTo("=")==0){
					firstCompare = 0;
				}else if ( m[0].group(2).compareTo("<>")==0){
					firstCompare = 1;
				}else if ( m[0].group(2).compareTo(">")==0){
					firstCompare = 2;
				}else if ( m[0].group(2).compareTo(">=")==0){
					firstCompare = 3;
				}else if ( m[0].group(2).compareTo("<")==0){
					firstCompare = 4;
				}else if ( m[0].group(2).compareTo("<=")==0){
					firstCompare = 5;
				}else if ( m[0].group(2).compareToIgnoreCase("CONTAINS")==0){
					firstCompare = 10;
				}else if ( m[0].group(2).compareToIgnoreCase("NOT_CONTAIN")==0){
					firstCompare = 11;
				}else if ( m[0].group(2).compareToIgnoreCase("BEGINS_WITH")==0){
					firstCompare = 6;
				}else if ( m[0].group(2).compareToIgnoreCase("IN")==0){
					firstCompare = 12;
				}else if ( m[0].group(2).compareToIgnoreCase("NOT_IN")==0){
					firstCompare = 13;
				}else{
					
				}		
			}	
		//[a].[b].[c].[value]
		}else if(Utils.isMatch(value, "((\\[[\\w|-]+\\])\\.?)+", m)){
			secondEditValue = m[0].group(2);
			firstEditValue = value.replace("."+secondEditValue.toString(), "");
			
			firstCompare = 0;				
		}else{
			System.out.println("url:"+value + " is error expression");
			firstCompare = -1;
			firstEditValue = "";
		}		
	}

	public void setValueEditName(String paramString) {
		valueEditName = paramString;
	}

	public String getValueEditName() {
		return valueEditName;
	}

	public void setFirstEditValue(Object paramObject) {
		firstEditValue = paramObject;
	}

	public Object getFirstEditValue() {
		return firstEditValue;
	}

	public void setEditValues(Object paramObject1, Object paramObject2) {
		secondEditValue = paramObject1;
		lastEditValue = paramObject2;
	}

	public Object[] getEditValues() {
		return new Object[] { secondEditValue, lastEditValue };
	}

	public void setFirstCompare(Integer paramInteger) {
		firstCompare = paramInteger;
	}

	public Integer getFirstCompare() {
		return firstCompare;
	}

	public void setCompares(Integer paramInteger1, Integer paramInteger2) {
		secondCompare = paramInteger1;
		lastCompare = paramInteger2;
	}

	public Integer[] getCompares() {
		return new Integer[] { secondCompare, lastCompare };
	}

	public Object clone() throws CloneNotSupportedException {
		XMLATreeNodeField localXMLATreeNodeField = (XMLATreeNodeField) super.clone();
		firstEditValue = localXMLATreeNodeField.getFirstEditValue();
		firstCompare = localXMLATreeNodeField.getFirstCompare();
		secondEditValue = localXMLATreeNodeField.getEditValues()[0];
		secondCompare = localXMLATreeNodeField.getCompares()[0];
		lastEditValue = localXMLATreeNodeField.getEditValues()[1];
		lastCompare = localXMLATreeNodeField.getCompares()[1];
		return localXMLATreeNodeField;
	}
	
	  public boolean isNode()
	  {
	    return true;
	  }
	  
}