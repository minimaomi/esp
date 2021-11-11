package com.raqsoft.lib.olap4j.xmla;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SAPBWQueryXMLATabelData
  extends XMLATableData
{
  private String m_mdx;
  private String m_whereState = "";
  private int m_sameDmPosition;
  private List<String> m_sliceDm = new ArrayList();
    private List<XMLATreeNodeField> m_fields = new ArrayList<XMLATreeNodeField>();
  
	public SAPBWQueryXMLATabelData(String cube, String rows, String cols, String filter){
		this.setCube(cube);
		this.setSelectDimension(rows);
		this.setSelectMeasure(cols);
		this.setRowAfterFilter(filter);

		if (filter!=null){
			String ss[] = filter.split("and");
			for(String s:ss){
				XMLATreeNodeField f = new XMLATreeNodeField(s.trim());
				m_fields.add(f);			
			}
		}
	}
	
	public String getMdxString()
	{
		m_mdx = null;
	    do
	    {
	    	String cube = this.getCube();
	    	if (cube==null || cube.isEmpty()){
	    		System.out.println("cube is empty");
	    		break;
	    	}
	    	String sDefineMdx = getUserDefinedMdx();
	    	if (StringUtils.isNotBlank(sDefineMdx))
	        {
	    		return sDefineMdx;
	        }
	    	String cols = transferSelectMeasure();
	    	String rows = transferSelectDimension();
	    	if ((StringUtils.isBlank(rows)) || (StringUtils.isBlank(cols))) {
	            return null;
	          }
	    	
	      String sCube = "[" + (cube).replaceAll("\\[", "").replaceAll("]", "") + "]";
	      String filter = this.getRowAfterFilter();
	      String colMdx = createColumnMdx(cols);
	      if((StringUtils.isBlank(filter)) ){
	    	 // mdx = String.format("select %s%s on rows from %s", new Object[] { colMdx, createRowMdx(rows), sCube });
	    	  m_mdx = ("select " + colMdx + createRowMdx(rows) + " on rows from " + sCube);
	      }else{
	    	  StringBuilder localStringBuilder = new StringBuilder();
	    	  dealFilterState(m_fields, null);
	    	  
	          localStringBuilder.append(GetWithState()).append(" select ");
	          String rowMdx = !isChangedAxisDmName() ? createRowMdx(getSelectDimension()) : createRowMdxAfterFiltered(getRowAfterFilter());
	          localStringBuilder.append(colMdx).append(rowMdx).append(" on rows ");
	          if (StringUtils.isNotBlank(m_whereState)) {
	        	  m_whereState = m_whereState.replaceAll(";", ",");
	          }
	    	  localStringBuilder.append("from ").append(sCube).append(m_whereState);
	    	  m_mdx = localStringBuilder.toString();
	      }
	    }while(false);
	    
	    return m_mdx;
	  }
	
  
  protected String getHierarchyFromLevel(String paramString)
  {
    String[] arrayOfString = paramString.split("\\.");
    StringBuilder localStringBuilder = new StringBuilder();
    if (arrayOfString.length == 3) {
      localStringBuilder.append(arrayOfString[0]).append(".").append(arrayOfString[1]);
    } else if (arrayOfString.length == 2) {
      localStringBuilder.append(arrayOfString[0]);
    } else {
      localStringBuilder.append(paramString);
    }
    return localStringBuilder.toString();
  }
  
  private boolean isJustSelectOneDm(String paramString)
  {
    String[] arrayOfString = paramString.trim().split(",");
    if (arrayOfString.length == 1) {
      return true;
    }
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(getHierarchyFromLevel(arrayOfString[0]));
    for (int i = 1; i < arrayOfString.length; i++)
    {
      String str = getHierarchyFromLevel(arrayOfString[i]);
      if (localArrayList.indexOf(str) == -1) {
        return false;
      }
    }
    return true;
  }
  
  protected String createRowMdx(String paramString)
  {
    if (StringUtils.isBlank(paramString)) {
      return null;
    }
    String[] arrayOfString = paramString.trim().split(",");
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("Non Empty{");
    if (isJustSelectOneDm(paramString))
    {
      for (int i = 0; i < arrayOfString.length - 1; i++) {
        localStringBuilder.append(arrayOfString[i]).append(".Members").append(",");
      }
      localStringBuilder.append(arrayOfString[(arrayOfString.length - 1)] + ".Members" + "}");
      return localStringBuilder.toString();
    }
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    int j = 0;
    int k = 0;
    while (j < arrayOfString.length)
    {
      String str1 = getHierarchyFromLevel(arrayOfString[j]);
      if (localArrayList1.indexOf(str1) != -1)
      {
        int m = localArrayList1.indexOf(str1);
        String str2 = (String)localArrayList2.get(m) + ".Members" + "," + arrayOfString[j];
        localArrayList2.set(m, str2);
      }
      else
      {
        localArrayList1.add(str1);
        localArrayList2.add(arrayOfString[j]);
      }
      j++;
    }
    for (j = 0; j < localArrayList2.size(); j++)
    {
      localStringBuilder.append("{").append((String)localArrayList2.get(j)).append(".Members").append("}");
      if (j < localArrayList2.size() - 1) {
        localStringBuilder.append("*");
      }
    }
    localStringBuilder.append("}");
    return localStringBuilder.toString();
  }
  
  private String processDimensionName(String paramString)
  {
    if (paramString.contains(".[LEVEL")) {
      paramString = paramString.substring(0, paramString.lastIndexOf("."));
    }
    return paramString;
  }
  
  protected String createRowMdxAfterFiltered(String paramString)
  {
    if (StringUtils.isBlank(paramString)) {
      return null;
    }
    String[] arrayOfString1 = paramString.split(",");
    StringBuilder localStringBuilder = new StringBuilder();
    if (arrayOfString1.length == 1) {
      return "Non Empty{" + paramString + "}";
    }
    String[] arrayOfString2 = getSelectRowAxisDm(transferSelectDimension());
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    localStringBuilder.append("Non Empty{");
    for (int i = 0; i < arrayOfString1.length; i++)
    {
      String[] arrayOfString3 = arrayOfString1[i].split("\\.");
      String str1 = getHierarchyFromLevel(arrayOfString2[i]);
      String str2 = arrayOfString3.length == 1 ? arrayOfString1[i] : arrayOfString2[i];
      if (localArrayList1.indexOf(str1) != -1)
      {
        int j = localArrayList1.indexOf(str1);
        String str3 = (String)localArrayList2.get(j) + ".Members" + "," + str2;
        localArrayList2.set(j, str3);
      }
      else
      {
        localArrayList1.add(str1);
        localArrayList2.add(str2);
      }
    }
    for (int i = 0; i < localArrayList2.size(); i++)
    {
      if (!((String)localArrayList2.get(i)).startsWith("set")) {
        localStringBuilder.append("{").append((String)localArrayList2.get(i)).append(".Members").append("}");
      } else {
        localStringBuilder.append("{").append((String)localArrayList2.get(i)).append("}");
      }
      if (i < localArrayList1.size() - 1) {
        localStringBuilder.append("*");
      }
    }
    return "}";
  }
  
  protected void dealSlice(XMLATreeNodeField paramXMLATreeNodeField)
  {
    if (isNotValidFilterState(paramXMLATreeNodeField)) {
      return;
    }
    StringBuilder localStringBuilder1 = new StringBuilder();
    Integer localInteger = paramXMLATreeNodeField.getFirstCompare();
    String str1 = paramXMLATreeNodeField.getFirstEditValue().toString();
    String str2 = processDimensionName(str1);
    String str3 = getHierarchyFromLevel(str2);
    Object localObject = dealWithCompareValue(paramXMLATreeNodeField.getEditValues()[0]);
    if (StringUtils.equals(localInteger, Integer.valueOf(0))) {
      localStringBuilder1.append(str2).append(".[").append(localObject).append("]");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(1))) {
      localStringBuilder1.append("Except({").append(str1).append(".Members}").append(",{").append(str2).append(".[").append(localObject).append("]})");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(2))) {
      localStringBuilder1.append("{").append(str2).append(".[").append(localObject).append("].NextMember : ").append(str2).append(".[").append(localObject).append("].LastSibling} ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(3))) {
      localStringBuilder1.append("{").append(str2).append(".[").append(localObject).append("] : ").append(str2).append(".[").append(localObject).append("].LastSibling} ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(4))) {
      localStringBuilder1.append("{").append(str2).append(".[").append(localObject).append("].FirstSibling : ").append(str2).append(".[").append(localObject).append("].PrevMember}  ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(5))) {
      localStringBuilder1.append("{").append(str2).append(".[").append(localObject).append("].FirstSibling : ").append(str2).append(".[").append(localObject).append("]}  ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(10))) {
      localStringBuilder1.append("{Filter(").append(str2).append(".Members").append(";Instr(").append(str3).append(".CurrentMember.name;\"").append(localObject).append("\")>=1)} ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(11))) {
      localStringBuilder1.append("{Filter(").append(str2).append(".Members").append(";Instr(").append(str3).append(".CurrentMember.name;\"").append(localObject).append("\")=0)} ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(6))) {
      localStringBuilder1.append("{Filter(").append(str2).append(".Members").append(";Instr(").append(str3).append(".CurrentMember.name;\"").append(localObject).append("\")=1)} ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(12))) {
      localStringBuilder1.append("{").append(dealWithSliceDmConditionIn(localObject.toString(), str2)).append("} ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(13))) {
      localStringBuilder1.append("{").append(dealWithDmConditionNotIn(localObject.toString(), str2)).append("} ");
    }
    if (isHasSameDm(str2))
    {
      dealWithSliceDm(localStringBuilder1);
    }
    else
    {
      StringBuilder localStringBuilder2 = new StringBuilder();
      int i = m_whereState.length() - 1;
      m_whereState = (StringUtils.isBlank(m_whereState) ? " where (" + localStringBuilder1.toString() + ")" : 
    	  localStringBuilder2.append(m_whereState).insert(i, localStringBuilder1.insert(0, ",").toString()).toString());
    }
  }
  
  private void dealWithSliceDm(StringBuilder paramStringBuilder)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    int i = m_whereState.length() - 1;
    m_whereState = (StringUtils.isBlank(m_whereState) ? " where (" + paramStringBuilder.toString() + ")" : 
    	localStringBuilder.append(m_whereState).insert(i, paramStringBuilder.insert(0, ",").toString()).toString());
  }
  
  private boolean isHasSameDm(String paramString)
  {
    if (StringUtils.isBlank(paramString)) {
      return false;
    }
    String str1 = "^\\{.*\\}$";
    Pattern localPattern = Pattern.compile(str1);
    Matcher localMatcher = localPattern.matcher(paramString);
    String[] arrayOfString1;
    if (localMatcher.matches())
    {
      String[] localObject = paramString.replaceAll("[\\{\\}]", "").split(";");
      String[] arrayOfString2 = localObject[0].split("[\\(\\)]");
      arrayOfString1 = arrayOfString2[(arrayOfString2.length - 1)].split("\\.");
    }
    else
    {
      arrayOfString1 = paramString.split("\\.");
    }
    Object localObject = new StringBuilder();
    for (int i = 0; i < arrayOfString1.length - 1; i++)
    {
      if (i > 0) {
        ((StringBuilder)localObject).append(".");
      }
      ((StringBuilder)localObject).append(arrayOfString1[i]);
    }
    if (m_sliceDm == null) {
    	m_sliceDm = new ArrayList();
    }
    if (m_sliceDm.isEmpty())
    {
    	m_sliceDm.add(((StringBuilder)localObject).toString());
      return false;
    }
    String str2 = ((StringBuilder)localObject).toString();
    if (m_sliceDm.indexOf(str2) != -1)
    {
    	m_sameDmPosition = m_sliceDm.indexOf(str2);
      return true;
    }
    m_sliceDm.add(((StringBuilder)localObject).toString());
    return false;
  }
  
  public String getMdx()
  {
    return m_mdx;
  }
  
  public void setMdx(String paramString)
  {
	  m_mdx = paramString;
  }
  
  protected void dealSelectRowDm(XMLATreeNodeField paramXMLATreeNodeField, List<String> paramList)
  {
    if (isNotValidFilterState(paramXMLATreeNodeField)) {
      return;
    }
    String str1 = paramXMLATreeNodeField.getFirstEditValue().toString();
    int i = paramList.indexOf(str1);
    String str2 = "set" + String.valueOf(i);
    modifySelectDimension(str2, i);
    setChangedAxisDmName(true);
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append(" set ").append(str2).append(" as ");
    String str3 = processDimensionName(str1);
    Integer localInteger = paramXMLATreeNodeField.getFirstCompare();
    String str4 = getHierarchyFromLevel(str3);
    Object localObject = dealWithCompareValue(paramXMLATreeNodeField.getEditValues()[0]);
    if (StringUtils.equals(localInteger, Integer.valueOf(0))) {
      localStringBuilder.append(str3).append(".[").append(localObject).append("] ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(1))) {
      localStringBuilder.append("'Except({").append(str1).append(".Members}").append(",{").append(str3).append(".[").append(localObject).append("]})'");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(2))) {
      localStringBuilder.append(str3).append(".[").append(localObject).append("].NextMember : ").append(str3).append(".[").append(localObject).append("].LastSibling ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(3))) {
      localStringBuilder.append(str3).append(".[").append(localObject).append("] : ").append(str3).append(".[").append(localObject).append("].LastSibling ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(4))) {
      localStringBuilder.append(str3).append(".[").append(localObject).append("].FirstSibling : ").append(str3).append(".[").append(localObject).append("].PrevMember  ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(5))) {
      localStringBuilder.append(str3).append(".[").append(localObject).append("].FirstSibling : ").append(str3).append(".[").append(localObject).append("] ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(10))) {
      localStringBuilder.append("'Filter(").append(str3).append(".Members").append(",Instr(").append(str4).append(".CurrentMember.name,\"").append(localObject).append("\")>=1)' ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(11))) {
      localStringBuilder.append("'Filter(").append(str3).append(".Members").append(",Instr(").append(str4).append(".CurrentMember.name,\"").append(localObject).append("\")=0)' ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(6))) {
      localStringBuilder.append("'Filter(").append(str3).append(".Members").append(",Instr(").append(str4).append(".CurrentMember.name,\"").append(localObject).append("\")=1)' ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(12))) {
      localStringBuilder.append("{").append(dealWithDmConditionIn(localObject.toString(), str3)).append("} ");
    } else if (StringUtils.equals(localInteger, Integer.valueOf(13))) {
      localStringBuilder.append("{").append(dealWithDmConditionNotIn(localObject.toString(), str3)).append("} ");
    }
    String str5 = GetWithState();
    setWithState(StringUtils.isBlank(str5) ? localStringBuilder.insert(0, "with").toString() : localStringBuilder.insert(0, str5).toString());
  }
    
  protected String dealDimensionMember(String paramString)
  {
    return paramString;
  }
  
  public String transferSelectDimension()
  {
    String str = super.getSelectDimension();
    ConcurrentHashMap localConcurrentHashMap = getDimensionMap();
    if (localConcurrentHashMap.size() > 0)
    {
      String[] arrayOfString = str.split(",");
      if (arrayOfString.length > 0)
      {
        str = (String)localConcurrentHashMap.get(arrayOfString[0]);
        for (int i = 1; i < arrayOfString.length; i++) {
          str = str + "," + (String)localConcurrentHashMap.get(arrayOfString[i]);
        }
      }
    }
    return str;
  }
  
  private String transferSelectMeasure()
  {
    String str = super.getSelectMeasure();
    ConcurrentHashMap localConcurrentHashMap = getMeasureMap();
    if (localConcurrentHashMap.size() > 0)
    {
      String[] arrayOfString = str.split(",");
      if (arrayOfString.length > 0)
      {
        str = (String)localConcurrentHashMap.get(arrayOfString[0]);
        for (int i = 1; i < arrayOfString.length; i++) {
          str = str + "," + (String)localConcurrentHashMap.get(arrayOfString[i]);
        }
      }
    }
    return str;
  }
}