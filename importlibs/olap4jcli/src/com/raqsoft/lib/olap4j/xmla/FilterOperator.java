package com.raqsoft.lib.olap4j.xmla;

public enum FilterOperator
{
  EQUALS(0, "="),  NOT_EQUAL(1, "<>"),  GREATER_THAN(2, ">"),  GREATER_THAN_OR_EQUAL(3, ">="),  LESS_THAN(4, "<"),  LESS_THAN_OR_EQUAL(5, "<="),  CONTAINS(10),  NOT_CONTAIN(11),  BEGINS_WITH(6),  IN(12),  NOT_IN(13);
  
  private int frCoreCompare;
  private String operator;
  
  private FilterOperator(int paramInt)
  {
    frCoreCompare = paramInt;
  }
  
  private FilterOperator(int paramInt, String paramString)
  {
    frCoreCompare = paramInt;
    operator = paramString;
  }

  public String toFilterCondition(String paramString1, String paramString2)
  {
	  if (frCoreCompare==10){
		  return String.format("(Instr(%s.name, '%s') >=1 )", new Object[] { paramString1, paramString2 });
	  }else if (frCoreCompare==12){
		  return String.format("(Instr('%s', %s.name) >=1 )", new Object[] { paramString2, paramString1 });
	  }else if (frCoreCompare==6){
		  return String.format("(Instr(%s.name, '%s') =1 )", new Object[] { paramString1, paramString2 });
	  }else if(1==1){
		  return compareOnName(paramString1, paramString2);
	  }else{
		  return compareOnValue(paramString1, paramString2);
	  }
  }
  
  public static FilterOperator fromFrCoreCompare(int paramInt)
  {
    for (FilterOperator localFilterOperator : FilterOperator.values()) {
      if (localFilterOperator.frCoreCompare == paramInt) {
        return localFilterOperator;
      }
    }
    throw new IllegalArgumentException(String.format("unsupport compare type, %s", new Object[] { Integer.valueOf(paramInt) }));
  }
  
  protected String compareOnName(String paramString1, String paramString2)
  {
    return String.format("(%s.name %s '%s')", new Object[] { paramString1, operator, paramString2 });
  }
  
  protected String compareOnValue(String paramString1, String paramString2)
  {
    return String.format("(%s.memberValue %s %s)", new Object[] { paramString1, operator, paramString2 });
  }
}
