package com.raqsoft.lib.olap4j.xmla;

import java.util.ArrayList;


public abstract class MultiDimensionTableData
{
  private static final long serialVersionUID = -7761369936363494102L;
  private String connectionName;
  private String cube;
  private String queryMode = "";
  private String selectMeasure;
  private String selectDimension;
  private XMLATreeNodeField fields = null;
  
  public void setConnectionName(String paramString)
  {
    connectionName = paramString;
  }
  
  public String getConnectionName()
  {
    return connectionName;
  }
  
  public String getCube()
  {
    return cube;
  }
  
  public void setCube(String paramString)
  {
    cube = paramString;
  }
  
  public String getQueryMode()
  {
    return queryMode;
  }
  
  public void setQueryMode(String paramString)
  {
    queryMode = paramString;
  }
  
  public String getSelectMeasure()
  {
    return selectMeasure;
  }
  
  public void setSelectMeasure(String paramString)
  {
    selectMeasure = paramString;
  }
  
  public String getSelectDimension()
  {
    String[] arrayOfString1 = selectDimension.split(",");
    ArrayList localArrayList = new ArrayList(arrayOfString1.length);
    for (String str1 : arrayOfString1)
    {
      String str2 = levelPathToName(str1);
      localArrayList.add(str2);
    }
    return StringUtils.join(",", (String[])localArrayList.toArray(new String[localArrayList.size()]));
  }
  
  public String getSelectLevelPath()
  {
    return selectDimension;
  }
  
  protected String[] getSelectLevelPath(String paramString)
  {
    String[] arrayOfString1 = selectDimension.split(",");
    for (String str : arrayOfString1) {
      if (levelPathToName(str).equals(paramString)) {
        return str.split("\\.");
      }
    }
    return paramString.split("\\.");
  }
  
  private String levelPathToName(String paramString)
  {
    String[] arrayOfString = paramString.split("\\.");
    if (arrayOfString.length < 3) {
      return paramString;
    }
    if (arrayOfString[1].equals(arrayOfString[2])) {
      return String.format("%s.%s", new Object[] { arrayOfString[0], arrayOfString[1] });
    }
    return String.format("%s.%s.%s", new Object[] { arrayOfString[0], arrayOfString[1], arrayOfString[(arrayOfString.length - 1)] });
  }
  
  public void setSelectDimension(String paramString)
  {
    selectDimension = paramString;
  }
  
  public XMLATreeNodeField getFilterStatement()
  {
    return fields;
  }
  
  public void setFilterStatement(XMLATreeNodeField paramXMLATreeNodeField)
  {
    fields = paramXMLATreeNodeField;
  }
  
  public abstract boolean isXMLAData();
  
  public abstract boolean isFineBIData();
}