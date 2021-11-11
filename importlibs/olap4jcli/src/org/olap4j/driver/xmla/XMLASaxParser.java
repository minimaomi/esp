package org.olap4j.driver.xmla;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
/*
 * <Axes>
<Axis name="Axis0">
	<Tuples>
		<Tuple>
			<Member Hierarchy="[Due Date].[Calendar Year]">
				<UName>[Due Date].[Calendar Year].&amp;[2001]</UName>
				<Caption>2001</Caption>
				<LName>[Due Date].[Calendar Year].[Calendar Year]</LName>
				<LNum>1</LNum>
				<DisplayInfo>0</DisplayInfo>
			</Member>
			<Member Hierarchy="[Product].[Product Key]">
				<UName>[Product].[Product Key].[All]</UName>
				<Caption>All</Caption>
				<LName>[Product].[Product Key].[(All)]</LName>
				<LNum>0</LNum>
				<DisplayInfo>66142</DisplayInfo>
			</Member>
		</Tuple>
	</Tuples>
</Axis></Axes>
 */


public class XMLASaxParser extends DefaultHandler
{
  private XMLAByteBufferTable mappedByteBufferTable = new XMLAByteBufferTable();
  private List<String> axis0 = new ArrayList();
  private List<String> axis1 = new ArrayList();
  private String cellType;
  private int cellOrdinal;
  private String axisName = "";
  private String hierarchyName = "";
  private int index = 0;
  
  private StringBuilder sb = new StringBuilder();
  private static final int BUFFER_TABLE_MAXSIZE = 256;
  private XmalNodeInfo nodeInfo = new XmalNodeInfo();
  public boolean m_bFirst = true;
  
  class XmalNodeInfo{
	  public String nodeAxes;	//1
	  public String nodeAxis;	//2  
	  public String nodeTuples;	//3
	  public String nodeTuple;	//4
	  public String nodeMember;	//5
	  public String nodeValue;	//6
	  public String curValue;	//记录最终显示值，它可能是个组合值.
	  public int nodeLevel;		//1-->6
	  public int nodeCol;
	  public int nodeIndex;
	  public boolean bTuple; 
	  XmalNodeInfo(){
		  nodeLevel = 1;
		  nodeCol = 0;
		  nodeIndex = 0;
		  bTuple = false;
		  curValue = "";
	  }
  }
  
  public void startDocument()
    throws SAXException
  {
    System.out.println("start document -> parse begin");
  }
  
  public void endDocument()
    throws SAXException
  {
    System.out.println("end document -> parse finished");
  }
  
  public void startElement(String paramString1, String paramString2, String paramString3, Attributes paramAttributes)
    throws SAXException
  {
	  //System.out.println("start: "+paramString1+" "+paramString2+" "+paramString3);
	  doStartNodeInfo(paramString3,paramString2);
    sb.setLength(0);
    if (paramString3.equals("Error"))
    {
      StringBuilder localStringBuilder = new StringBuilder("XMLA Error: \n");
      for (int i = 0; i < paramAttributes.getLength(); i++)
      {
        String str1 = paramAttributes.getQName(i);
        String str2 = paramAttributes.getValue(i);
        localStringBuilder.append(str1 + " = " + str2 + "\n");
      }
      System.out.println(localStringBuilder.toString());
    }
    if ((paramString3.equals("Axis")) && (null != paramAttributes.getValue("name"))) {
      axisName = paramAttributes.getValue("name");
    }
    if ((paramString3.equals("Member")) && (null != paramAttributes.getValue("Hierarchy"))) {
      hierarchyName = paramAttributes.getValue("Hierarchy");
    }
    if ((paramString3.equals("Value")) && (null != paramAttributes.getValue("xsi:type"))) {
      cellType = paramAttributes.getValue("xsi:type");
    }
    if ((paramString3.equals("Cell")) && (null != paramAttributes.getValue("CellOrdinal"))) {
    	String s = paramAttributes.getValue("CellOrdinal");
    	if (s==null || s.isEmpty()){
    		cellOrdinal = 0;
    	}else{
    		cellOrdinal= Integer.parseInt(s);
    	}
      }
  }
  
  public void characters(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {
    sb.append(paramArrayOfChar, paramInt1, paramInt2);
  }
  
  private void doStartNodeInfo(String key,String val){	  
	  if ("Axes".equalsIgnoreCase(key) ){
		  nodeInfo.nodeAxes = "Axes";
		  nodeInfo.nodeLevel = 1;
	  }else if ("Axis".equalsIgnoreCase(key) ){
		  nodeInfo.nodeTuples = "Axis";
		  nodeInfo.nodeLevel=2;
	  }else if ("Tuples".equalsIgnoreCase(key) ){
		  nodeInfo.nodeTuples = "Tuples";
		  nodeInfo.nodeLevel=3;
	  }else if ("Tuple".equalsIgnoreCase(key) ){
		  nodeInfo.nodeTuple = "Tuple";
		  nodeInfo.nodeLevel=4;
	  }else if ("Member".equalsIgnoreCase(key) ){
		  nodeInfo.nodeMember = "Member";
		  nodeInfo.nodeLevel=5;
	  } 
  }
  
  private void doEndNodeInfo(String key,String val){
	  if ("Axes".equalsIgnoreCase(key) ){
		  nodeInfo.nodeAxes = "";
		  nodeInfo.nodeLevel = 1;
	  }else if ("Axis".equalsIgnoreCase(key) ){
		  nodeInfo.nodeTuples = "";
		  nodeInfo.nodeLevel=2;
	  }else if ("Tuples".equalsIgnoreCase(key) ){
		  nodeInfo.nodeTuples = "";
		  nodeInfo.nodeLevel=3;
	  }else if ("Tuple".equalsIgnoreCase(key) ){
		  nodeInfo.nodeTuple = "";
		  nodeInfo.nodeLevel=4;
		  nodeInfo.curValue = nodeInfo.nodeValue;
		  nodeInfo.nodeValue = "";
		  if (axisName.equals("Axis1")){
			  nodeInfo.nodeIndex++;
		  }
	  }else if ("Member".equalsIgnoreCase(key) ){
		  nodeInfo.nodeMember = "";
		  nodeInfo.nodeLevel=5;
	  } else if (nodeInfo.nodeLevel == 5){
		 if ((key.equals("Caption")) && (axisName.equals("Axis0"))){
			 if (nodeInfo.nodeValue==null || nodeInfo.nodeValue.isEmpty()){
				 nodeInfo.nodeValue=val;
			 }else{
				 nodeInfo.nodeValue+="_"+val;
			 }
		 }else if ((key.equals("Caption")) && (axisName.equals("Axis1"))){
			nodeInfo.nodeCol = nodeInfo.nodeIndex;
		 }
	  }
  }
  //采用列行式存储
  public void endElement(String paramString1, String paramString2, String paramString3)
    throws SAXException
  {
    String str = sb.toString().trim();
    //System.out.println("end: "+paramString1+" "+paramString2+" "+paramString3);
    doEndNodeInfo(paramString3, str);
    if ((paramString3.equals("Value")) && (cellType != null)) {
    	int rows = getAxis0Num() ;
    	int cols = getAxis1Num();
    	int row=0, col=0;
    	if (rows>0){
	    	row = cellOrdinal / rows;
	    	col = cellOrdinal % rows+getAxis1Num();
    	}else{
    		col = cols;
    	}
    	//System.out.println("row = "+ row +"; col="+col+ "; val=" + cellOrdinal+";v = "+str);
        mappedByteBufferTable.addValue(col, row, XMLAByteBufferTable.getTypedValue(cellType, str));
    }
    if (str.length() > 0) {
      if (paramString3.equals("UName"))
      {
        if ((axisName.equals("Axis0")) && (hierarchyName.contains("Measures")) && (!axis0.contains(str))) {
          if(!nodeInfo.bTuple){
              axis0.add(str);
          }
        }
        if ((axisName.equals("Axis1")) && (hierarchyName.contains("Measures")) && (!axis1.contains(str))) {
          axis1.add(str);
        }
      }
      else if (paramString3.equals("Member"))
      {
        if ((axisName.equals("Axis0")) && (!hierarchyName.contains("Measures")) && (!axis0.contains(hierarchyName))) {
          if(!nodeInfo.bTuple){
              axis0.add(hierarchyName);
          }
        }
        if ((axisName.equals("Axis1")) && (!hierarchyName.contains("Measures")) && (!axis1.contains(hierarchyName))) {
          axis1.add(hierarchyName);
        }
      }
      else if (paramString3.equals("Tuple"))
      {
        if ((axisName.equals("Axis0")) && (!nodeInfo.curValue.isEmpty())) {
        	if (m_bFirst){
        		m_bFirst = false;
        		nodeInfo.bTuple = true;
        		axis0.clear();
        	}
          axis0.add(nodeInfo.curValue);
          nodeInfo.curValue = "";
        }
        if ((axisName.equals("Axis1")) && (axis1.contains(hierarchyName))) {
          //axis1.add(hierarchyName);
        }
      }
      else if ((paramString3.equals("Caption")) && (axisName.equals("Axis1")))
      //else if ((paramString3.equals("Caption")) )
      {
    	  //System.out.println("Caption col= "+index + "; row=" + nodeInfo.nodeCol+"; val=" + str);
          mappedByteBufferTable.addValue(index++, nodeInfo.nodeCol, str);
      }
    }
    if (paramString3.equals("Tuple")) {
      index = 0;
    }
    if (paramString3.equals("faultcode")) {
      System.out.println("faultcode: \n" + str);
    }
    cellType = null;
  }
  
  public int getAxis0Num()
  {
    return axis0.size();
  }
  
  public int getAxis1Num()
  {
    return axis1.size();
  }
  
  public List<String> getAxis0()
  {
    return axis0;
  }
  
  public List<String> getAxis1()
  {
    return axis1;
  }
  
  public XMLAByteBufferTable getMappedByteBufferTable()
  {
    return mappedByteBufferTable;
  }
  
  public List<String> getColumn()
  {
	  List<String> ls = new ArrayList<>();
	  
	  ls.addAll(axis1);
	  ls.addAll(axis0);
	  
	  return ls;
  }
  
}
