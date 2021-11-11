package com.raqsoft.lib.olap4j.xmla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSASQueryXMLATableData extends XMLATableData
{  
    private String m_mdx;
    private String m_whereState = "";
    private int sameDmPosition;
    private List<String> sliceDm = new ArrayList();    
    private List<XMLATreeNodeField> m_fields = new ArrayList<XMLATreeNodeField>();
	
	public SSASQueryXMLATableData(String cube, String rows, String cols, String filter){
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
		String mdx = null;
	    do
	    {
	    	String cube = this.getCube();
	    	if (cube==null || cube.isEmpty()){
	    		System.out.println("cube is empty");
	    		break;
	    	}
	    	String cols = this.getSelectMeasure();
	    	String rows = this.getSelectDimension();
	    	if ((StringUtils.isBlank(rows)) || (StringUtils.isBlank(cols))) {
	            return null;
	          }
	    	
	      String sCube = "[" + (cube).replaceAll("\\[", "").replaceAll("]", "") + "]";
	      String filter = this.getRowAfterFilter();
	      String str4 = createColumnMdx(cols);
	      if((StringUtils.isBlank(filter)) ){
	    	  mdx = String.format("select %s%s on rows from %s", new Object[] { str4, createRowMdx(rows), sCube });
	      }else{
	    	  StringBuilder localStringBuilder = new StringBuilder();
	    	  dealFilterState(m_fields, null);
	    	  
	          localStringBuilder.append(GetWithState()).append(" select ");
	          String str5 = !isChangedAxisDmName() ? createRowMdx(getSelectDimension()) : createRowMdxAfterFiltered(getRowAfterFilter());
	          localStringBuilder.append(str4).append(str5).append(" on rows ");
	          if (StringUtils.isNotBlank(m_whereState)) {
	        	  m_whereState = m_whereState.replaceAll(";", ",");
	          }
	    	  localStringBuilder.append("from ").append(sCube).append(m_whereState);
	          mdx = localStringBuilder.toString();
	      }
	    }while(false);
	    
	    return mdx;
	  }
	
	protected String createRowMdx(String paramString)
	  {
	    if (StringUtils.isBlank(paramString)) {
	      return null;
	    }
	    String[] arrayOfString1 = paramString.trim().split(",");
	    StringBuilder localStringBuilder = new StringBuilder();
	    if (arrayOfString1.length == 1) {
	      return "{" + dealDimensionMember(arrayOfString1[0]) + "}";
	    }
	    ArrayList localArrayList = new ArrayList();
	    for (int i = 0; i < arrayOfString1.length; i++)
	    {
	      String str1 = dealDimensionMember(arrayOfString1[i]);
	      String str2 = getHierarchyFromLevel(str1);
	      if (localArrayList.indexOf(str2) != -1)
	      {
	        int j = localArrayList.indexOf(str2);
	        String[] arrayOfString2 = localStringBuilder.toString().split(",");
	        arrayOfString2[j] = (j > arrayOfString2.length - 1 ? null : new StringBuilder().append(arrayOfString2[j]).insert(1, str1 + ";").toString());
	        localStringBuilder.delete(0, localStringBuilder.length());
	        for (int k = 0; k < arrayOfString2.length - 1; k++) {
	          localStringBuilder.append(arrayOfString2[k]).append(",");
	        }
	        localStringBuilder.append(arrayOfString2[(arrayOfString2.length - 1)]);
	      }
	      else
	      {
	        if (i > 0) {
	          localStringBuilder.append(",");
	        }
	        localStringBuilder.append("{").append(str1).append("}");
	        localArrayList.add(str2);
	      }
	    }
	    localArrayList = null;
	    //return ")".replaceAll(";", ",");
	    return String.format("non empty(%s)",  localStringBuilder);
	  }
		  
	  protected String createRowMdxAfterFiltered(String paramString)
	  {
	    if (StringUtils.isBlank(paramString)) {
	      return null;
	    }
	    String[] arrayOfString1 = paramString.split(",");
	    if (arrayOfString1.length == 1) {
	      return paramString;
	    }
	    String[] arrayOfString2 = getSelectRowAxisDm(getSelectDimension());
	    HashMap localHashMap = new HashMap(arrayOfString2.length);
	    for (int i = 0; i < arrayOfString2.length; i++) {
	      localHashMap.put(arrayOfString2[i], dealDimensionMember(arrayOfString1[i]));
	    }
	    Map localMap = getEachHierarchyLevels(arrayOfString2, localHashMap);
	    return generateOnRowsClause(localMap);
	  }
  
	  private String generateOnRowsClause(Map<String, List<String>> paramMap)
	  {
	    ArrayList localArrayList = new ArrayList(paramMap.size());
	    Iterator localIterator = paramMap.values().iterator();
	    while (localIterator.hasNext())
	    {
	      List localList = (List)localIterator.next();
	      localArrayList.add(String.format("{%s}", new Object[] { join(",", localList) }));
	    }
	    return String.format("non empty(%s)", new Object[] { join(",", localArrayList) });
	  }
	  
	  private Map<String, List<String>> getEachHierarchyLevels(String[] paramArrayOfString, Map<String, String> paramMap)
	  {
	    HashMap localHashMap = new HashMap();
	    for (String str1 : paramArrayOfString)
	    {
	      String str2 = getHierarchyFromLevel(str1);
	      String str3 = (String)paramMap.get(str1);
	      if (localHashMap.containsKey(str2)) {
	        ((List)localHashMap.get(str2)).add(str3);
	      } else {
	        localHashMap.put(str2, new ArrayList(Collections.singletonList(str3)));
	      }
	    }
	    return localHashMap;
	  }
	  
	  protected void dealSlice(String paramString, List<XMLATreeNodeField> paramList)
	  {
	    if (!checkFilterFields(paramList)) {
	      return;
	    }
	    String str1 = dealDimensionMember(((XMLATreeNodeField)paramList.get(0)).getFirstEditValue().toString());
	    String str2 = toFilterClause(str1, paramList);
	    StringBuilder localStringBuilder = new StringBuilder();
	    if (isHasSameDm(paramString)) {
	      dealWithSliceDm(str2);
	    } else if (StringUtils.isBlank(m_whereState)) {
	    	m_whereState = (" where (" + str2 + ")");
	    } else {
	    	m_whereState = localStringBuilder.append(m_whereState).insert(m_whereState.length() - 1, String.format(", %s", new Object[] { str2 })).toString();
	    }
	  }
	  
	  protected void dealSelectRowDm(String paramString, List<XMLATreeNodeField> paramList, List<String> paramList1)
	  {
	    if (!checkFilterFields(paramList)) {
	      return;
	    }
	    int i = paramList1.indexOf(paramString);
	    String str1 = String.format("set%d", new Object[] { Integer.valueOf(i) });
	    modifySelectDimension(str1, i);
	    setChangedAxisDmName(true);
	    StringBuilder localStringBuilder = new StringBuilder();
	    localStringBuilder.append(String.format(" set %s as %s", new Object[] { str1, toFilterClause(paramString, paramList) }));
	    String str2 = GetWithState();
	    setWithState(StringUtils.isBlank(str2) ? localStringBuilder.insert(0, "with").toString() : localStringBuilder.insert(0, str2).toString());
	  }
	  
	  private boolean checkFilterFields(List<XMLATreeNodeField> paramList)
	  {
	    Iterator localIterator = paramList.iterator();
	    while (localIterator.hasNext())
	    {
	    	XMLATreeNodeField localXMLATreeNodeField = (XMLATreeNodeField)localIterator.next();
	      if (isNotValidFilterState(localXMLATreeNodeField)) {
	        return false;
	      }
	    }
	    return true;
	  }
	  
	  protected boolean isHasSameDm(String paramString)
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
	      String[] localObject = paramString.replaceAll("[\\{\\}]", "").split(",");
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
	    if (sliceDm == null) {
	      sliceDm = new ArrayList();
	    }
	    if (sliceDm.isEmpty())
	    {
	      sliceDm.add(((StringBuilder)localObject).toString());
	      return false;
	    }
	    String str2 = ((StringBuilder)localObject).toString();
	    if (sliceDm.indexOf(str2) != -1)
	    {
	      sameDmPosition = sliceDm.indexOf(str2);
	      return true;
	    }
	    sliceDm.add(((StringBuilder)localObject).toString());
	    return false;
	  }
	  
	  private void dealWithSliceDm(String paramString)
	  {
	    String[] arrayOfString1 = m_whereState.split("[\\(\\)]");
	    String[] arrayOfString2 = arrayOfString1[(arrayOfString1.length - 1)].split(",");
	    if (sameDmPosition > arrayOfString2.length - 1) {
	      return;
	    }
	    String str1 = arrayOfString2[sameDmPosition].trim();
	    String str2 = "^\\{.*\\}$";
	    Pattern localPattern = Pattern.compile(str2);
	    Matcher localMatcher1 = localPattern.matcher(paramString);
	    if (localMatcher1.matches()) {
	      paramString = paramString.replaceAll("[\\{\\}]", "");
	    }
	    Matcher localMatcher2 = localPattern.matcher(str1);
	    StringBuilder localStringBuilder = new StringBuilder();
	    if (localMatcher2.matches())
	    {
	      String localObject = str1.substring(1, str1.length() - 1);
	      localStringBuilder.append("{").append((String)localObject).append(";").append(paramString).append("}");
	    }
	    else
	    {
	      localStringBuilder.append("{").append(str1).append(";").append(paramString).append("}");
	    }
	    arrayOfString2[sameDmPosition] = localStringBuilder.toString();
	    Object localObject = new StringBuilder(" where (");
	    ((StringBuilder)localObject).append(arrayOfString2[0]);
	    for (int i = 1; i < arrayOfString2.length; i++) {
	      ((StringBuilder)localObject).append(",").append(arrayOfString2[i]);
	    }
	    m_whereState = ")";
	  }
	  
	  protected boolean isNotValidFilterState(XMLATreeNodeField paramXMLATreeNodeField)
	  {
		  return (paramXMLATreeNodeField.getFirstEditValue() == null) || (paramXMLATreeNodeField.getFirstEditValue().toString().split("\\.").length < 2) || (paramXMLATreeNodeField.getFirstCompare() == null) || (paramXMLATreeNodeField.getEditValues()[0] == null);
	  }
	  
	  protected String dealDimensionMember(String paramString)
	  {
	    if (StringUtils.isBlank(paramString)) {
	      return null;
	    }
	    String[] arrayOfString = paramString.split("\\.");
	    return arrayOfString.length == 2 ? String.format("%s.%s", new Object[] { paramString, arrayOfString[1] }) : paramString;
	  }
		  
	  protected String[] getSelectRowAxisDm(String paramString)
	  {
	    if (StringUtils.isBlank(paramString)) {
	      return new String[0];
	    }
	    String[] arrayOfString = paramString.split(",");
	    for (int i = 0; i < arrayOfString.length; i++) {
	      arrayOfString[i] = dealDimensionMember(arrayOfString[i]);
	    }
	    return arrayOfString;
	  }
	  
	  private String toFilterClause(String paramString, List<XMLATreeNodeField> paramList)
	  {
	    String str = getHierarchyFromLevel(paramString);
	    ArrayList localArrayList1 = new ArrayList(paramList.size());
	    ArrayList localArrayList2 = new ArrayList();
	    ArrayList localArrayList3 = new ArrayList();
	    Object localObject1 = paramList.iterator();
	    Object localObject2;
	    while (((Iterator)localObject1).hasNext())
	    {
	      localObject2 = (XMLATreeNodeField)((Iterator)localObject1).next();
	      if (getFilterLevelName((XMLATreeNodeField)localObject2).equals(paramString)) {
	        localArrayList2.add(localObject2);
	      } else {
	        localArrayList3.add(localObject2);
	      }
	    }
	    localObject1 = localArrayList2.iterator();
	    while (((Iterator)localObject1).hasNext())
	    {
	      localObject2 = (XMLATreeNodeField)((Iterator)localObject1).next();
	      localArrayList1.add(compareCurrentMember(str, (XMLATreeNodeField)localObject2));
	    }
	    if (!localArrayList3.isEmpty())
	    {
	      localObject1 = Arrays.asList(getSelectLevelPath(paramString));
	      localObject2 = localArrayList3.iterator();
	      while (((Iterator)localObject2).hasNext())
	      {
	        XMLATreeNodeField localXMLATreeNodeField = (XMLATreeNodeField)((Iterator)localObject2).next();
	        localArrayList1.add(compareParentMember(str, localXMLATreeNodeField, (List)localObject1));
	      }
	    }
	    localObject1 = join(" and ", localArrayList1);
	    return String.format("{filter(%s.members, %s)}", new Object[] { paramString, localObject1 });
	  }
  
	  private String compareParentMember(String paramString, XMLATreeNodeField paramXMLATreeNodeField, List<String> paramList)
	  {
	    String[] arrayOfString = getFilterLevelName(paramXMLATreeNodeField).split("\\.");
	    String str1 = arrayOfString[(arrayOfString.length - 1)];
	    int i = paramList.size() - paramList.indexOf(str1) - 1;
	    String str2;
	    if (i > 0)
	    {
	      for (str2 = String.format("%s.currentMember", new Object[] { paramString }); i-- != 0; str2 = String.format("%s.parent", new Object[] { str2 })) {}
	      return compareMember(str2, paramXMLATreeNodeField);
	    }
	    return "";
	  }
	  private String compareCurrentMember(String paramString, XMLATreeNodeField paramXMLATreeNodeField)
	  {
	    return compareMember(String.format("%s.currentMember", new Object[] { paramString }), paramXMLATreeNodeField);
	  }
	  
	  private String compareMember(String paramString, XMLATreeNodeField paramXMLATreeNodeField)
	  {
	    Object localObject = dealWithCompareValue(paramXMLATreeNodeField.getEditValues()[0]);
	    if (localObject.toString().isEmpty()) {
	      localObject = "null";
	    }
	    FilterOperator localFilterOperator = FilterOperator.fromFrCoreCompare(paramXMLATreeNodeField.getFirstCompare().intValue());
	    return localFilterOperator.toFilterCondition(paramString, localObject.toString());
	  }
	  
	  private String join(String paramString, List<String> paramList)
	  {
	    return StringUtils.join(paramString, (String[])paramList.toArray(new String[paramList.size()]));
	  }
	  	  
	  private String getFilterLevelName(XMLATreeNodeField paramXMLATreeNodeField)
	  {
	    List localList = Arrays.asList(paramXMLATreeNodeField.getFirstEditValue().toString().split("\\."));
	    assert ((localList.size() == 2) || (localList.size() == 3)) : "filter first edit value error";
	    String str = join(".", localList.subList(0, 2));
	    return localList.size() == 3 ? join(".", localList) : String.format("%s.%s", new Object[] { str, localList.get(1) });
	  }
	  
}
