package com.raqsoft.lib.olap4j.xmla;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class XMLATableData
  extends MultiDimensionTableData
{
  public static final String XMLA_TABLE_TAG = "XMLATableDataAttr";
  private String userDefinedMdx = "";
  private boolean isChangedAxisDmName = false;
  private String withState = "";
  private String rowAfterFilter = "";
  private ConcurrentHashMap<String, String> measureMap = new ConcurrentHashMap();
  private ConcurrentHashMap<String, String> dimensionMap = new ConcurrentHashMap();
  private ConcurrentHashMap<String, String> filterMap = new ConcurrentHashMap();
  
  public void setDimensionMap(ConcurrentHashMap<String, String> paramConcurrentHashMap)
  {
    dimensionMap = paramConcurrentHashMap;
  }
  
  public void setFilterMap(ConcurrentHashMap<String, String> paramConcurrentHashMap)
  {
    filterMap = paramConcurrentHashMap;
  }
  
  public void setMeasureMap(ConcurrentHashMap<String, String> paramConcurrentHashMap)
  {
    measureMap = paramConcurrentHashMap;
  }
  
  public ConcurrentHashMap<String, String> getDimensionMap()
  {
    return dimensionMap;
  }
  
  public ConcurrentHashMap<String, String> getFilterMap()
  {
    return filterMap;
  }
  
  public ConcurrentHashMap<String, String> getMeasureMap()
  {
    return measureMap;
  }
  
  public String getSelectDimension()
  {
    return super.getSelectDimension();
  }
  
  public String getSelectMeasure()
  {
    return super.getSelectMeasure();
  }
  
  
  
  public String getUserDefinedMdx()
  {
    return userDefinedMdx;
  }
  
  public void setUserDefinedMdx(String paramString)
  {
    userDefinedMdx = paramString;
  }
  
  protected String createColumnMdx(String paramString)
  {
    if (StringUtils.isBlank(paramString)) {
      return null;
    }
    StringBuilder localStringBuilder = new StringBuilder("{");
    localStringBuilder.append(paramString.trim() + "} on columns, ");
    return localStringBuilder.toString();
  }
  
  protected String[] getSelectAxisDm(String paramString1, String paramString2)
  {
    if ((StringUtils.isBlank(paramString1)) || (StringUtils.isBlank(paramString2))) {
      return new String[0];
    }
    String[] arrayOfString1 = getSelectRowAxisDm(paramString1);
    String[] arrayOfString2 = getSelectColAxisDm(paramString2);
    String[] arrayOfString3 = new String[arrayOfString1.length + arrayOfString2.length];
    System.arraycopy(arrayOfString1, 0, arrayOfString3, 0, arrayOfString1.length);
    System.arraycopy(arrayOfString2, 0, arrayOfString3, arrayOfString1.length, arrayOfString2.length);
    return arrayOfString3;
  }
  
  protected String[] getSelectRowAxisDm(String paramString)
  {
    if (StringUtils.isBlank(paramString)) {
      return new String[0];
    }
    return paramString.split(",");
  }
  
  protected String[] getSelectColAxisDm(String paramString)
  {
    if (StringUtils.isBlank(paramString)) {
      return new String[0];
    }
    return paramString.split(",");
  }
  
//  protected void dealFilterStatement(XMLATreeNodeField paramXMLATreeNodeField)
//  {
//    if (paramXMLATreeNodeField == null) {
//      return;
//    }
//    ArrayList localArrayList1 = new ArrayList();
//    ArrayList localArrayList2 = new ArrayList();
//    if (paramXMLATreeNodeField.isNode())
//    {
//      addXMLATreeNodeField(localArrayList1, localArrayList2, paramXMLATreeNodeField);
//    }
//    else
//    {
//      assert ((paramXMLATreeNodeField instanceof XMLAListField)) : "class type error";
//      XMLAListField localXMLAListField = (XMLAListField)XMLAListField.class.cast(paramXMLATreeNodeField);
//      for (int i = 0; i < localXMLAListField.getJoinFieldCount(); i++)
//      {
//        XMLATreeNodeField localXMLATreeNodeField = localXMLAListField.getJoinField(i).getField();
//        addXMLATreeNodeField(localArrayList1, localArrayList2, localXMLATreeNodeField);
//      }
//    }
//    dealFilterState(localArrayList1, localArrayList2);
//  }
  
  private void addXMLATreeNodeField(List<XMLATreeNodeField> paramList1, List<XMLATreeNodeField> paramList2, XMLATreeNodeField paramXMLATreeNodeField)
  {
    paramList1.add(paramXMLATreeNodeField);
  }
  
  protected void dealFilterState(List<XMLATreeNodeField> paramList1, List<XMLATreeNodeField> paramList2)
  {
    if ((StringUtils.isBlank(getSelectDimension())) || (StringUtils.isBlank(getSelectMeasure()))) {
      return;
    }
    if (!paramList1.isEmpty()) {
      dealDmFilter(paramList1);
    }
    if (paramList2!=null && !paramList2.isEmpty()) {
      dealSelectColWithMeasure(paramList2);
    }
  }
  
  protected void dealDmFilter(List<XMLATreeNodeField> paramList)
  {
    List localList = Arrays.asList(getSelectRowAxisDm(getSelectDimension()));
    HashMap localHashMap = new HashMap(); //rows dim
    Object localObject1 = localList.iterator();
    while (((Iterator)localObject1).hasNext())
    {
    	String localObject2 = (String)((Iterator)localObject1).next();
    	Object localObject3 = getHierarchyFromLevel((String)localObject2);
      if (localHashMap.containsKey(localObject3)) {
        ((List)localHashMap.get(localObject3)).add(localObject2);
      } else {
        localHashMap.put(localObject3, new ArrayList(Collections.singletonList(localObject2)));
      }
    }
    localObject1 = new ArrayList();
    Object localObject2 = new ArrayList();
    Object localObject3 = new HashSet();
    Object localObject4 = paramList.iterator();
    Object localObject7;
    while (((Iterator)localObject4).hasNext())
    {
    	Object localObject5 = ((Iterator)localObject4).next();
    	Object localObject6 = dealDimensionMember(((XMLATreeNodeField)localObject5).getFirstEditValue().toString());
      localObject7 = getHierarchyFromLevel((String)localObject6);
      if (!localHashMap.containsKey(localObject7))
      {
        ((List)localObject1).add(localObject5);
      }
      else
      {
        ((List)localObject2).add(localObject5);
        ((Set)localObject3).add(localObject7);
      }
    }
    // filter dim
    localObject4 = splitByLevelName((List)localObject1);
    Object localObject5 = ((Map)localObject4).entrySet().iterator();
    while (((Iterator)localObject5).hasNext())
    {
    	Object localObject6 = (Map.Entry)((Iterator)localObject5).next();
        dealSlice((String)((Map.Entry)localObject6).getKey(), (List)((Map.Entry)localObject6).getValue());
    }
    localObject5 = allLevelFiltersOnHierarchy((Set)localObject3, localHashMap, (List)localObject2);
    Object localObject6 = ((Map)localObject5).entrySet().iterator();
    while (((Iterator)localObject6).hasNext())
    {
      localObject7 = (Map.Entry)((Iterator)localObject6).next();
      dealSelectRowDm((String)((Map.Entry)localObject7).getKey(), (List)((Map.Entry)localObject7).getValue(), localList);
    }
  }
  
  private Map<String, List<XMLATreeNodeField>> allLevelFiltersOnHierarchy(Set<String> paramSet, Map<String, List<String>> paramMap, List<XMLATreeNodeField> paramList)
  {
    HashMap localHashMap = new HashMap();
    Iterator localIterator1 = paramSet.iterator();
    Object localObject1;
    Object localObject2;
    String str1;
    while (localIterator1.hasNext())
    {
      localObject1 = (String)localIterator1.next();
      assert (paramMap.containsKey(localObject1));
      localObject2 = ((List)paramMap.get(localObject1)).iterator();
      while (((Iterator)localObject2).hasNext())
      {
        str1 = (String)((Iterator)localObject2).next();
        localHashMap.put(str1, new ArrayList());
      }
    }
    localIterator1 = paramList.iterator();
    while (localIterator1.hasNext())
    {
      localObject1 = (XMLATreeNodeField)localIterator1.next();
      localObject2 = dealDimensionMember(((XMLATreeNodeField)localObject1).getFirstEditValue().toString());
      str1 = getHierarchyFromLevel((String)localObject2);
      Iterator localIterator2 = ((List)paramMap.get(str1)).iterator();
      while (localIterator2.hasNext())
      {
        String str2 = (String)localIterator2.next();
        assert (localHashMap.containsKey(str2));
        ((List)localHashMap.get(str2)).add(localObject1);
      }
    }
    return localHashMap;
  }
  
  private Map<String, List<XMLATreeNodeField>> splitByLevelName(List<XMLATreeNodeField> paramList)
  {
    HashMap localHashMap = new HashMap();
    Iterator localIterator = paramList.iterator();
    while (localIterator.hasNext())
    {
      XMLATreeNodeField localXMLATreeNodeField = (XMLATreeNodeField)localIterator.next();
      String str = dealDimensionMember(localXMLATreeNodeField.getFirstEditValue().toString());
      if (!localHashMap.containsKey(str)) {
        localHashMap.put(str, new ArrayList(Collections.singletonList(localXMLATreeNodeField)));
      } else {
        ((List)localHashMap.get(str)).add(localXMLATreeNodeField);
      }
    }
    return localHashMap;
  }
  
  protected void dealSlice(String paramString, List<XMLATreeNodeField> paramList)
  {
    dealSlice((XMLATreeNodeField)paramList.get(0));
  }
  
  protected void dealSlice(XMLATreeNodeField paramXMLATreeNodeField) {}
  
  protected void dealSelectRowDm(String paramString, List<XMLATreeNodeField> paramList, List<String> paramList1)
  {
    dealSelectRowDm((XMLATreeNodeField)paramList.get(0), paramList1);
  }
  
  protected void dealSelectRowDm(XMLATreeNodeField paramXMLATreeNodeField, List<String> paramList) {}
  
  protected void dealSelectColWithMeasure(List<XMLATreeNodeField> paramList)
  {
    StringBuilder localStringBuilder1 = new StringBuilder("set measureset as 'filter( ");
    String str1 = !isChangedAxisDmName ? createRowMdx(getSelectDimension()) : createRowMdxAfterFiltered(rowAfterFilter);
    localStringBuilder1.append(str1.replaceAll("Non Empty", "CrossJoin")).append(",");
    setRowAfterFilter("measureset");
    isChangedAxisDmName = true;
    StringBuilder localStringBuilder2 = new StringBuilder();
    for (int i = 0; i < paramList.size(); i++) {
      if (!isNotValidFilterState((XMLATreeNodeField)paramList.get(i)))
      {
        if (i > 0) {
          localStringBuilder2.append(" and ");
        }
        String str2 = ((XMLATreeNodeField)paramList.get(i)).getFirstEditValue().toString();
        Integer localInteger = ((XMLATreeNodeField)paramList.get(i)).getFirstCompare();
        Object localObject = dealWithCompareValue(((XMLATreeNodeField)paramList.get(i)).getEditValues()[0]);
        if (StringUtils.equals(localInteger, Integer.valueOf(0))) {
          localStringBuilder2.append(str2).append("=").append(localObject);
        } else if (StringUtils.equals(localInteger, Integer.valueOf(1))) {
          localStringBuilder2.append(str2).append("<>").append(localObject);
        } else if (StringUtils.equals(localInteger, Integer.valueOf(2))) {
          localStringBuilder2.append(str2).append(">").append(localObject);
        } else if (StringUtils.equals(localInteger, Integer.valueOf(3))) {
          localStringBuilder2.append(str2).append(">=").append(localObject);
        } else if (StringUtils.equals(localInteger, Integer.valueOf(4))) {
          localStringBuilder2.append(str2).append("<").append(localObject);
        } else if (StringUtils.equals(localInteger, Integer.valueOf(5))) {
          localStringBuilder2.append(str2).append("<=").append(localObject);
        } else if (StringUtils.equals(localInteger, Integer.valueOf(12))) {
          localStringBuilder2.append(dealWithMeasureConditionIn(localObject.toString(), str2));
        } else if (StringUtils.equals(localInteger, Integer.valueOf(13))) {
          localStringBuilder2.append(dealWithMeConditionNotIn(localObject.toString(), str2));
        }
      }
    }
    localStringBuilder1.append(localStringBuilder2.toString()).append(")' ");
    withState = (StringUtils.isBlank(withState) ? localStringBuilder1.insert(0, "with ").toString() : localStringBuilder1.insert(0, withState).toString());
  }
    
  protected abstract String dealDimensionMember(String paramString);
  
  protected abstract String createRowMdx(String paramString);
  
  protected abstract String createRowMdxAfterFiltered(String paramString);
  
  protected String dealWithDmConditionIn(String paramString1, String paramString2)
  {
    String[] arrayOfString = paramString1.replaceAll("[\\(\\)]", "").split(",");
    StringBuilder localStringBuilder = new StringBuilder();
    for (int i = 0; i < arrayOfString.length - 1; i++) {
      localStringBuilder.append(paramString2).append(".[").append(arrayOfString[i]).append("],");
    }
    return paramString2 + ".[" + arrayOfString[(arrayOfString.length - 1)] + "]";
  }
  
  protected String dealWithDmConditionNotIn(String paramString1, String paramString2)
  {
    String[] arrayOfString = paramString1.replaceAll("[\\(\\)]", "").split(",");
    StringBuilder localStringBuilder = new StringBuilder(paramString2);
    for (int i = 0; i < arrayOfString.length - 1; i++) {
      localStringBuilder.append("-").append(paramString2).append(".[").append(arrayOfString[i]).append("]");
    }
    return "-" + paramString2 + ".[" + arrayOfString[(arrayOfString.length - 1)] + "]";
  }
  
  protected void modifySelectDimension(String paramString, int paramInt)
  {
    String[] arrayOfString;
    if (isChangedAxisDmName) {
      arrayOfString = rowAfterFilter.split(",");
    } else {
      arrayOfString = getSelectDimension().split(",");
    }
    if (paramInt >= arrayOfString.length) {
      return;
    }
    arrayOfString[paramInt] = paramString;
    setRowAfterFilter(StringUtils.join(",", arrayOfString));
  }
  
  protected String dealWithSliceDmConditionIn(String paramString1, String paramString2)
  {
    String[] arrayOfString = paramString1.replaceAll("[\\(\\)]", "").split(",");
    StringBuilder localStringBuilder = new StringBuilder();
    for (int i = 0; i < arrayOfString.length - 1; i++) {
      localStringBuilder.append(paramString2).append(".[").append(arrayOfString[i]).append("];");
    }
    return paramString2 + ".[" + arrayOfString[(arrayOfString.length - 1)] + "]";
  }
  
  protected String dealWithMeasureConditionIn(String paramString1, String paramString2)
  {
    String[] arrayOfString = paramString1.replaceAll("[\\(\\)]", "").split(",");
    StringBuilder localStringBuilder = new StringBuilder();
    for (int i = 0; i < arrayOfString.length - 1; i++) {
      localStringBuilder.append(paramString2).append("=").append(arrayOfString[i]).append(" and ");
    }
    return paramString2 + "=" + arrayOfString[(arrayOfString.length - 1)];
  }
  
  protected String dealWithMeConditionNotIn(String paramString1, String paramString2)
  {
    String[] arrayOfString = paramString1.replaceAll("[\\(\\)]", "").split(",");
    StringBuilder localStringBuilder = new StringBuilder();
    for (int i = 0; i < arrayOfString.length - 1; i++) {
      localStringBuilder.append(paramString2).append("<>").append(arrayOfString[i]).append(" and ");
    }
    return paramString2 + "<>" + arrayOfString[(arrayOfString.length - 1)];
  }
  
  protected boolean isNotValidFilterState(XMLATreeNodeField paramXMLATreeNodeField)
  {
    return (paramXMLATreeNodeField.getFirstEditValue() == null) || 
    	   (paramXMLATreeNodeField.getFirstEditValue().toString().split("\\.").length < 1) || 
    	   (paramXMLATreeNodeField.getFirstCompare() == null) || 
    	   (paramXMLATreeNodeField.getEditValues()[0] == null);
  }
  
  protected Object dealWithCompareValue(Object paramObject)
  {
    Object localObject = null;
    String str;
//    if ((paramObject instanceof ParameterProvider))
//    {
//      str = "${" + ((ParameterProvider)paramObject).getName() + "}";
//      localObject = ParameterHelper.analyze4Templatee(str, parameters);
//    }
//    else if ((paramObject instanceof FormulaProvider))
//    {
//      str = ((FormulaProvider)paramObject).getContent();
//      try
//      {
//        localObject = Calculator.createCalculator().evalValue(str);
//      }
//      catch (UtilEvalError localUtilEvalError)
//      {
//        localObject = str;
//      }
//    }
//    else 
    if ((paramObject instanceof String))
    {
      localObject = ((String)paramObject).replaceAll("[\\[\\]\"'боб░]", "");
    }
    else
    {
      if ((paramObject instanceof Date)) {
        return ((Date)paramObject).toGMTString();
      }
      localObject = String.valueOf(paramObject);
    }
    return localObject;
  }
  
  protected String getHierarchyFromLevel(String paramString)
  {
    String[] arrayOfString = paramString.split("\\.");
    if (arrayOfString.length < 3) {
      return paramString;
    }
    return arrayOfString[0] + "." + arrayOfString[1];
  }
  
  protected boolean isChangedAxisDmName()
  {
    return isChangedAxisDmName;
  }
  
  protected void setChangedAxisDmName(boolean paramBoolean)
  {
    isChangedAxisDmName = paramBoolean;
  }
  
  protected String GetWithState()
  {
    return withState;
  }
  
  protected void setWithState(String paramString)
  {
    withState = paramString;
  }
  
  public boolean isXMLAData()
  {
    return true;
  }
  
  public boolean isFineBIData()
  {
    return false;
  }
  
  public String getRowAfterFilter()
  {
    return rowAfterFilter;
  }
  
  public void setRowAfterFilter(String paramString)
  {
    rowAfterFilter = paramString;
  }
}