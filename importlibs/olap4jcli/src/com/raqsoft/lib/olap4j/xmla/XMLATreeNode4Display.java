package com.raqsoft.lib.olap4j.xmla;

public class XMLATreeNode4Display
{
  private String uniqueName;
  private String caption;
  
  public XMLATreeNode4Display(String paramString1, String paramString2)
  {
    caption = paramString1;
    uniqueName = paramString2;
  }
  
  public XMLATreeNode4Display(String paramString)
  {
    uniqueName = paramString;
    caption = paramString;
  }
  
  public String getCaption()
  {
    return caption;
  }
  
  public void setCaption(String paramString)
  {
    caption = paramString;
  }
  
  public String getUniqueName()
  {
    return uniqueName;
  }
  
  public void setUniqueName(String paramString)
  {
    uniqueName = paramString;
  }
  
  public boolean equals(Object paramObject)
  {
    return ((paramObject instanceof XMLATreeNode4Display)) && 
    		(StringUtils.equals(getUniqueName(), ((XMLATreeNode4Display)paramObject).getUniqueName()));
  }
  
  public String toString()
  {
    return getCaption();
  }
}