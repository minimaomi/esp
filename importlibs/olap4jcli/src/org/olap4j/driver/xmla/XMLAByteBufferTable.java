package org.olap4j.driver.xmla;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//考虑到性能问题，getValue地不检验，调用者来验证
public class XMLAByteBufferTable {
	private MapObject[] m_list = null;
	private List<String> m_columns;
	
	public XMLAByteBufferTable(){
		m_columns = new ArrayList<String>();
		m_list = new MapObject[256];
		for(int i=0; i<m_list.length; i++){
			m_list[i] = new MapObject();
		}
	}
	
	public void clear(){
		for(MapObject l:m_list){
			l.clear();
		}
	}
	
	public int rowCount(){
		return m_list[0].size();
	}
	
	  public void setColumn(List<String> cols){
		  m_columns.clear();
		  for(String s : cols){
			  m_columns.add(s);
		  }
	  }
	  
	  public List<String> getColumn(){
		  return m_columns;
	  }
	class MapObject extends HashMap{
		//public List<Object> m_listObj;
	}
	
	public void addValue(int row, int col, Object val){
		m_list[row].put(col,val);
	}
	
	public Object getValueAt(int row, int col){
		return m_list[row].get(col);
	}
	
	public static Object getTypedValue(String paramString1, String paramString2)
	  {
	    XsdTypes localXsdTypes = XsdTypes.fromString(paramString1);
	    try
	    {
	      switch (localXsdTypes)
	      {
	      case XSD_BOOLEAN: 
	        return Boolean.valueOf(paramString2);
	      case XSD_INT: 
	        return Integer.valueOf(paramString2);
	      case XSD_INTEGER: 
	        return new BigInteger(paramString2);
	      case XSD_DOUBLE: 
	        return Double.valueOf(paramString2);
	      case XSD_POSITIVEINTEGER: 
	        return new BigInteger(paramString2);
	      case XSD_DECIMAL: 
	        return new BigDecimal(paramString2);
	      case XSD_SHORT: 
	        return Short.valueOf(paramString2);
	      case XSD_FLOAT: 
	        return Float.valueOf(paramString2);
	      case XSD_LONG: 
	        return Long.valueOf(paramString2);
	      case XSD_BYTE: 
	        return Byte.valueOf(paramString2);
	      case XSD_UNSIGNEDBYTE: 
	        return Short.valueOf(paramString2);
	      case XSD_UNSIGNEDSHORT: 
	        return Integer.valueOf(paramString2);
	      case XSD_UNSIGNEDLONG: 
	        return new BigDecimal(paramString2);
	      case XSD_UNSIGNEDINT: 
	        return Long.valueOf(paramString2);
	      }
	      return String.valueOf(paramString2);
	    }
	    catch (Exception localException)
	    {
	      System.out.println("Error while casting a cell value to the correct java type for its XSD type " + paramString1);
	    }
	    return null;
	  }
	
	static enum XsdTypes
	  {
	    XSD_INT("xsd:int"),  
	    XSD_INTEGER("xsd:integer"),  
	    XSD_DOUBLE("xsd:double"),  
	    XSD_POSITIVEINTEGER("xsd:positiveInteger"),  
	    XSD_DECIMAL("xsd:decimal"),  
	    XSD_SHORT("xsd:short"),  
	    XSD_FLOAT("xsd:float"),  
	    XSD_LONG("xsd:long"),  
	    XSD_BOOLEAN("xsd:boolean"),  
	    XSD_BYTE("xsd:byte"),  
	    XSD_UNSIGNEDBYTE("xsd:unsignedByte"),  
	    XSD_UNSIGNEDSHORT("xsd:unsignedShort"),  
	    XSD_UNSIGNEDLONG("xsd:unsignedLong"),  
	    XSD_UNSIGNEDINT("xsd:unsignedInt"),  
	    XSD_STRING("xsd:string");
	    
	    private final String name;
	    private static final Map<String, XsdTypes> TYPES;
	    
	    private XsdTypes(String paramString)
	    {
	      name = paramString;
	    }
	    
	    public static XsdTypes fromString(String paramString)
	    {
	      XsdTypes localXsdTypes = (XsdTypes)TYPES.get(paramString);
	      return localXsdTypes == null ? XSD_STRING : localXsdTypes;
	    }
	    
	    static
	    {
	      HashMap localHashMap = new HashMap();
	      String k = "kaa";
	      for (XsdTypes localXsdTypes : values()) {
	        localHashMap.put(k, localXsdTypes);
	      }
	      TYPES = Collections.unmodifiableMap(localHashMap);
	    }
	  }
	
}
