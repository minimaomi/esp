package com.raqsoft.lib.olap4j.xmla;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.StringTokenizer;

public class StringUtils
{
  public static final String EMPTY = "";
  public static final String BLANK = " ";
  
  public static String alwaysNotNull(String paramString)
  {
    return paramString == null ? "" : paramString;
  }
  
  public static boolean isEmpty(String paramString)
  {
    return (paramString == null) || (paramString.length() == 0);
  }
  
  public static boolean isNotEmpty(String paramString)
  {
    return !isEmpty(paramString);
  }
  
  public static boolean isBlank(String paramString)
  {
    int i;
    return (paramString == null) || ((i = paramString.length()) == 0) ? true : isBlank(paramString, i);
  }
  
  private static boolean isBlank(String paramString, int paramInt)
  {
    for (int i = 0; i < paramInt; i++) {
      if (!Character.isWhitespace(paramString.charAt(i))) {
        return false;
      }
    }
    return true;
  }
  
  public static String cutStringStartWith(String paramString1, String paramString2)
  {
    if (paramString1 == null) {
      return null;
    }
    if (paramString2 == null) {
      return paramString1;
    }
    if (!paramString1.startsWith(paramString2)) {
      return paramString1;
    }
    return paramString1.substring(paramString2.length());
  }
  
  public static String cutStringEndWith(String paramString1, String paramString2)
  {
    if (paramString1 == null) {
      return null;
    }
    if (paramString2 == null) {
      return paramString1;
    }
    if (!paramString1.endsWith(paramString2)) {
      return paramString1;
    }
    int i = paramString1.indexOf(paramString2);
    if (i == -1) {
      return paramString1;
    }
    return paramString1.substring(0, i);
  }
  
  public static boolean isNotBlank(String paramString)
  {
    return !isBlank(paramString);
  }
  
  public static String trim(String paramString)
  {
    return paramString == null ? null : paramString.trim();
  }
  
  public static String trimToNull(String paramString)
  {
    String str = trim(paramString);
    return isEmpty(str) ? null : str;
  }
  
  public static String perfectStart(String paramString1, String paramString2)
  {
    if (paramString1 == null) {
      return paramString2;
    }
    return paramString2 + paramString1;
  }
  
  public static String perfectEnd(String paramString1, String paramString2)
  {
    if (paramString1 == null) {
      return paramString2;
    }
    return paramString1 + paramString2;
  }
  
  public static String perfectSurround(String paramString1, String paramString2)
  {
    if (paramString1 == null) {
      return paramString2;
    }
    paramString1 = paramString1 + paramString2;
    paramString1 = paramString2 + paramString1;
    return paramString1;
  }
  
  public static int getLength(String paramString)
  {
    return paramString == null ? 0 : paramString.length();
  }
  
  public static boolean equalsIgnore(String paramString1, String paramString2, String paramString3)
  {
    return perfectStart(perfectEnd(paramString1, paramString3), paramString3).equals(perfectStart(perfectEnd(paramString2, paramString3), paramString3));
  }
  
  public static boolean contains(String paramString1, String paramString2)
  {
    return (paramString1 != null) && (paramString1.indexOf(paramString2) > -1);
  }
  
  public static StringTokenizer text2StringTokenizer(String paramString)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "\n");
    int i = paramString.length();
    for (int j = 0; j < i; j++)
    {
      int k = paramString.charAt(j);
      if (k == 13)
      {
        if ((j < i - 1) && (paramString.charAt(j + 1) == '\n')) {
          localStringTokenizer = new StringTokenizer(paramString, "\r\n");
        } else {
          localStringTokenizer = new StringTokenizer(paramString, "\r");
        }
      }
      else {
        if (k == 10) {
          break;
        }
      }
    }
    return localStringTokenizer;
  }
  
  public static String join(String paramString, String[] paramArrayOfString)
  {
    int i = paramArrayOfString.length;
    if (i == 0) {
      return "";
    }
    StringBuffer localStringBuffer = new StringBuffer(i * paramArrayOfString[0].length()).append(paramArrayOfString[0]);
    for (int j = 1; j < i; j++) {
      localStringBuffer.append(paramString).append(paramArrayOfString[j]);
    }
    return localStringBuffer.toString();
  }
  
  public static String parseVersion(String paramString)
  {
    paramString = paramString.replace('A', '0');
    paramString = paramString.replace('B', '1');
    paramString = paramString.replace('C', '2');
    paramString = paramString.replace('D', '3');
    paramString = paramString.replace('E', '4');
    paramString = paramString.replace('F', '5');
    paramString = paramString.replace('G', '6');
    paramString = paramString.replace('H', '7');
    paramString = paramString.replace('I', '8');
    paramString = paramString.replace('J', '9');
    return paramString;
  }
  
  public static boolean isArrayType(String paramString)
  {
    return (paramString != null) && (((paramString.startsWith("[")) && (paramString.endsWith("]"))) || ((paramString.startsWith("[[")) && (paramString.endsWith("]]"))));
  }
  
  public static String[][] stringToArray(String paramString)
  {
	String[] localObject;
    if (isArrayType(paramString))
    {
      
      paramString = paramString.replaceAll("\"", "");
      if ((paramString.startsWith("[[")) && (paramString.endsWith("]]")))
      {
        localObject = paramString.substring(2, paramString.length() - 2).split("],\\[");
        String[][] arrayOfString = new String[localObject.length][];
        for (int i = 0; i < arrayOfString.length; i++) {
          arrayOfString[i] = localObject[i].split(",");
        }
        return arrayOfString;
      }
      String[][] localObject2 = new String[1][];
      localObject2[0] = paramString.substring(1, paramString.length() - 1).split(",");
      return (String[][])localObject2;
    }
    localObject = paramString.split(";");
    String[][] arrayOfString = new String[localObject.length][];
    for (int i = 0; i < arrayOfString.length; i++) {
      arrayOfString[i] = localObject[i].split(",");
    }
    return arrayOfString;
  }
  
  public static String subStringByByteLength(String paramString1, String paramString2, int paramInt)
    throws UnsupportedEncodingException
  {
    if ((isBlank(paramString1)) || (paramInt <= 0)) {
      return "";
    }
    char[] arrayOfChar = paramString1.toCharArray();
    int i = 0;
    int j = arrayOfChar.length;
    for (int k = 0; k < arrayOfChar.length; k++)
    {
      int m = String.valueOf(arrayOfChar[k]).getBytes(paramString2).length + i;
      if (m <= paramInt)
      {
        i = m;
      }
      else
      {
        j = k;
        break;
      }
    }
    return String.valueOf(arrayOfChar, 0, j);
  }
  
  public static boolean equalsIgnoreCase(String paramString1, String paramString2)
  {
    if (paramString1 == null) {
      return paramString2 == null;
    }
    return (paramString2 != null) && (paramString1.equalsIgnoreCase(paramString2));
  }
  
  public static boolean equals(Object paramObject1, Object paramObject2)
  {
    if (paramObject1 == paramObject2) {
      return true;
    }
    if ((paramObject1 == null) || (paramObject2 == null)) {
      return (paramObject1 == null) && (paramObject2 == null);
    }
    
    if (((paramObject1 instanceof String)) && ((paramObject2 instanceof String))) {
      return paramObject1.equals(paramObject2);
    }
   
    try
    {
      if (((paramObject1 instanceof String)) && ((paramObject2 instanceof BigDecimal))) {
        return ((BigDecimal)paramObject2).compareTo(new BigDecimal((String)paramObject1)) == 0;
      }
      if (((paramObject2 instanceof String)) && ((paramObject1 instanceof BigDecimal))) {
        return ((BigDecimal)paramObject1).compareTo(new BigDecimal((String)paramObject2)) == 0;
      }
    }
    catch (Exception localException)
    {
      return false;
    }
   
    return false;
  }
}