package com.raqsoft.lib.olap4j.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.metadata.*;

import com.raqsoft.common.Logger;
import com.raqsoft.lib.olap4j.xmla.StringUtils;
import com.raqsoft.lib.olap4j.xmla.XMLATreeNode4Display;

public class ImMdxInfo {
	private boolean m_bSap = false;
	private OlapConnection m_olapConn = null;
	private String m_xmlaDatabaseName;

	public ImMdxInfo(OlapConnection olapConn, boolean bSap) {
		try {
			m_bSap = bSap;
			m_olapConn = olapConn;
			m_xmlaDatabaseName = olapConn.getCatalog();
		} catch (OlapException e) {
			Logger.error(e.getStackTrace());
		}
	}

	public XMLATreeNode4Display[] getAllMeasures(String paramString) {
		if (StringUtils.isBlank(paramString)) {
			return null;
		}
		if (m_bSap) {
			return getAllMeasuresFromSAP(paramString);
		}
		try {
			// OlapConnection localOlapConnection = getOlapConnection();
			List<Measure> localList = ((Cube) m_olapConn.getOlapSchema().getCubes().get(paramString)).getMeasures();
			int i = localList.size();
			XMLATreeNode4Display[] arrayOfXMLATreeNode4Display = new XMLATreeNode4Display[i];
			for (int j = 0; j < i; j++) {
				arrayOfXMLATreeNode4Display[j] = new XMLATreeNode4Display(((Measure) localList.get(j)).getUniqueName());
			}
			return arrayOfXMLATreeNode4Display;
		} catch (Exception localException) {
		}
		return new XMLATreeNode4Display[0];
	}

	public XMLATreeNode4Display[][] getAllDimensions(String paramString) {
		if (StringUtils.isBlank(paramString)) {
			return (XMLATreeNode4Display[][]) null;
		}
		if (m_bSap) {
			return getDimensionFromSAP(paramString);
		}
		XMLATreeNode4Display[][] arrayOfXMLATreeNode4Display = new XMLATreeNode4Display[3][];
		try {
			NamedList<Dimension> localNamedList1 = ((Cube) m_olapConn.getOlapSchema().getCubes().get(paramString))
					.getDimensions();
			int i = localNamedList1.size();
			ArrayList<XMLATreeNode4Display> localArrayList1 = new ArrayList<XMLATreeNode4Display>();
			localArrayList1.ensureCapacity(i);
			ArrayList<XMLATreeNode4Display> localArrayList2 = new ArrayList<XMLATreeNode4Display>();
			ArrayList<XMLATreeNode4Display> localArrayList3 = new ArrayList<XMLATreeNode4Display>();
			HashSet<XMLATreeNode4Display> localHashSet = new HashSet<XMLATreeNode4Display>(Arrays.asList(getAllMeasures(paramString)));
			for (int j = 0; j < i; j++) {
				String str = ((Dimension) localNamedList1.get(j)).getName();
				if ((!StringUtils.equals(str, "Measures")) && (!localHashSet.contains(new XMLATreeNode4Display(str)))) {
					localArrayList1.add(new XMLATreeNode4Display(((Dimension) localNamedList1.get(j)).getUniqueName()));
					ArrayList<XMLATreeNode4Display> localArrayList4 = getHierarchy((Dimension) localNamedList1.get(j));
					localArrayList2.addAll(localArrayList4);
					NamedList<Hierarchy> localNamedList2 = ((Dimension) localNamedList1.get(j)).getHierarchies();
					if (StringUtils.equals(localArrayList2, localArrayList1)) {
						return getDimensionFromLevels(localNamedList1);
					}
					int k = 0;
					int m = localNamedList2.size();
					while (k < m) {
						ArrayList<XMLATreeNode4Display> localArrayList5 = getLevels((Hierarchy) localNamedList2.get(k));
						localArrayList3.addAll(localArrayList5);
						k++;
					}
				}
			}
			XMLATreeNode4Display[] arrayOfXMLATreeNode4Display1 = (XMLATreeNode4Display[]) localArrayList1
					.toArray(new XMLATreeNode4Display[localArrayList1.size()]);
			XMLATreeNode4Display[] arrayOfXMLATreeNode4Display2 = (XMLATreeNode4Display[]) localArrayList2
					.toArray(new XMLATreeNode4Display[localArrayList2.size()]);
			XMLATreeNode4Display[] arrayOfXMLATreeNode4Display3 = (XMLATreeNode4Display[]) localArrayList3
					.toArray(new XMLATreeNode4Display[localArrayList3.size()]);
			arrayOfXMLATreeNode4Display[0] = arrayOfXMLATreeNode4Display1;
			arrayOfXMLATreeNode4Display[1] = arrayOfXMLATreeNode4Display2;
			arrayOfXMLATreeNode4Display[2] = arrayOfXMLATreeNode4Display3;
			localArrayList2 = null;
			localArrayList3 = null;
			localArrayList1 = null;
		} catch (Exception localException) {
			return new XMLATreeNode4Display[0][0];
		}
		return arrayOfXMLATreeNode4Display;
	}

	private XMLATreeNode4Display[] processLevels(XMLATreeNode4Display paramXMLATreeNode4Display) {
		String[] arrayOfString = paramXMLATreeNode4Display.getUniqueName().split("]\\.");
		XMLATreeNode4Display[] arrayOfXMLATreeNode4Display = new XMLATreeNode4Display[arrayOfString.length];
		for (int i = 0; i < arrayOfString.length; i++) {
			if (i < arrayOfString.length - 1) {
				int tmp45_43 = i;
				String[] tmp45_42 = arrayOfString;
				tmp45_42[tmp45_43] = (tmp45_42[tmp45_43] + "]");
			}
			if (i > 0) {
				arrayOfString[i] = (arrayOfString[(i - 1)] + "." + arrayOfString[i]);
			}
			arrayOfXMLATreeNode4Display[i] = new XMLATreeNode4Display(arrayOfString[i], arrayOfString[i]);
		}
		return arrayOfXMLATreeNode4Display;
	}

	private void processAllDimensionsLists(List<XMLATreeNode4Display> paramList1, List<XMLATreeNode4Display> paramList2,
			List<XMLATreeNode4Display> paramList3, List<XMLATreeNode4Display> paramList4) {
		Iterator<XMLATreeNode4Display> localIterator = paramList1.iterator();
		label186: while (localIterator.hasNext()) {
			XMLATreeNode4Display localXMLATreeNode4Display = (XMLATreeNode4Display) localIterator.next();
			XMLATreeNode4Display[] arrayOfXMLATreeNode4Display = processLevels(localXMLATreeNode4Display);
			switch (arrayOfXMLATreeNode4Display.length) {
			case 3:
				if ((arrayOfXMLATreeNode4Display.length > 2)
						&& (!paramList4.contains(arrayOfXMLATreeNode4Display[2]))) {
					paramList4.add(arrayOfXMLATreeNode4Display[2]);
				}
			case 2:
				if ((arrayOfXMLATreeNode4Display.length > 1)
						&& (!paramList3.contains(arrayOfXMLATreeNode4Display[1]))) {
					paramList3.add(arrayOfXMLATreeNode4Display[1]);
				}
			case 1:
				if (paramList2.contains(arrayOfXMLATreeNode4Display[0])) {
					break label186;
				}
				paramList2.add(arrayOfXMLATreeNode4Display[0]);
				break;
			}

			Logger.info("Level length error! length = " + arrayOfXMLATreeNode4Display.length);
		}
	}

	private XMLATreeNode4Display[][] getDimensionFromLevels(NamedList<Dimension> paramNamedList) throws OlapException {
		ArrayList<XMLATreeNode4Display> localArrayList1 = new ArrayList<XMLATreeNode4Display>();
		ArrayList<XMLATreeNode4Display> localArrayList2 = new ArrayList<XMLATreeNode4Display>();
		ArrayList<XMLATreeNode4Display> localArrayList3 = new ArrayList<XMLATreeNode4Display>();
		ArrayList<XMLATreeNode4Display> localArrayList4 = new ArrayList<XMLATreeNode4Display>();
		Iterator<Dimension> localObject5 = paramNamedList.iterator();
		while (localObject5.hasNext()) {
			Dimension localDimension = localObject5.next();
			NamedList<Hierarchy> localNamedList1 = localDimension.getHierarchies();
			if (!StringUtils.equals(localDimension.getUniqueName(), "[Accounts]")) {
				Iterator<Hierarchy> localIterator1 = localNamedList1.iterator();
				while (localIterator1.hasNext()) {
					Hierarchy localHierarchy = (Hierarchy) localIterator1.next();
					NamedList<Level> localNamedList2 = localHierarchy.getLevels();
					Iterator<Level> localIterator2 = localNamedList2.iterator();
					while (localIterator2.hasNext()) {
						Level localLevel = (Level) localIterator2.next();
						localArrayList4.add(new XMLATreeNode4Display(localLevel.getDescription(), localLevel.getUniqueName()));
					}
				}
			}
		}
		processAllDimensionsLists(localArrayList4, localArrayList1, localArrayList2, localArrayList3);
		XMLATreeNode4Display[][] localObject = new XMLATreeNode4Display[3][];
		localObject[0] = ((XMLATreeNode4Display[]) localArrayList1
				.toArray(new XMLATreeNode4Display[localArrayList1.size()]));
		localObject[1] = ((XMLATreeNode4Display[]) localArrayList2
				.toArray(new XMLATreeNode4Display[localArrayList2.size()]));
		localObject[2] = ((XMLATreeNode4Display[]) localArrayList3
				.toArray(new XMLATreeNode4Display[localArrayList3.size()]));
		return (XMLATreeNode4Display[][]) localObject;
	}

	private XMLATreeNode4Display[] getAllMeasuresFromSAP(String paramString) {
		
		return null;
	}

	private XMLATreeNode4Display[][] getDimensionFromSAP(String paramString) {
		
		return null;
	}

	private ArrayList<XMLATreeNode4Display> getHierarchy(Dimension paramDimension) {
		NamedList<Hierarchy> localNamedList = paramDimension.getHierarchies();
		int i = localNamedList.size();
		ArrayList<XMLATreeNode4Display> localArrayList = new ArrayList<XMLATreeNode4Display>();
		localArrayList.ensureCapacity(i);
		for (int j = 0; j < i; j++) {
			localArrayList.add(new XMLATreeNode4Display(((Hierarchy) localNamedList.get(j)).getUniqueName()));
		}
		return localArrayList;
	}

	private ArrayList<XMLATreeNode4Display> getLevels(Hierarchy paramHierarchy) {
		NamedList<Level> localNamedList = paramHierarchy.getLevels();
		int i = localNamedList.size();
		ArrayList<XMLATreeNode4Display> localArrayList = new ArrayList<XMLATreeNode4Display>();
		localArrayList.ensureCapacity(i);
		String str1 = paramHierarchy.getName();
		for (int j = 0; j < i; j++) {
			String str2 = ((Level) localNamedList.get(j)).getName();
			String str3 = ((Level) localNamedList.get(j)).getLevelType().name();
			if ((!StringUtils.equals(str2, "(All)")) && (!StringUtils.equals(str3, "ALL"))
					&& ((StringUtils.equals(getXmlaDatabaseName(), "SAPHANA")) || (!StringUtils.equals(str1, str2)))) {
				localArrayList.add(new XMLATreeNode4Display(((Level) localNamedList.get(j)).getUniqueName()));
			}
		}
		return localArrayList;
	}

	public String getXmlaDatabaseName() {
		return m_xmlaDatabaseName;
	}

	public void XMLAFilterPane(String catalog, String filter) {
		ArrayList<String> localArrayList1 = new ArrayList<String>();
		ArrayList<String> localArrayList2 = new ArrayList<String>();
		XMLATreeNode4Display[] arrayOfXMLATreeNode4Display = getAllMeasures(catalog);
		XMLATreeNode4Display[][] arrayOfXMLATreeNode4Display1 = getAllDimensions(catalog);
		String[] arrayOfString1 = filter.split(",");
		for (int i = arrayOfXMLATreeNode4Display1.length - 1; i > 0; i--) {
			for (int j = 0; j < arrayOfXMLATreeNode4Display1[i].length; j++) {
				if (i == arrayOfXMLATreeNode4Display1.length - 1) {
					for (String str1 : arrayOfString1) {
						if (StringUtils.equals(str1, arrayOfXMLATreeNode4Display1[i][j])) {
							String str2 = str1.substring(0, str1.lastIndexOf("."));
							localArrayList2.add(str2);
							localArrayList2.add(arrayOfXMLATreeNode4Display1[i][j].getUniqueName());
							break;
						}
					}
				} else {
					localArrayList1.add(arrayOfXMLATreeNode4Display1[i][j].getUniqueName());
				}
			}
		}
		int i = arrayOfXMLATreeNode4Display1.length - 1;
		int j = 0;
		int k;
		while (j < localArrayList1.size()) {
			k = 0;
			for (int n = 0; n < arrayOfXMLATreeNode4Display1[i].length; n++) {
				if ((!StringUtils.equals(arrayOfXMLATreeNode4Display1[i][n], localArrayList1.get(j)))
						&& (arrayOfXMLATreeNode4Display1[i][n].getUniqueName()
								.contains((CharSequence) localArrayList1.get(j)))) {
					localArrayList1.remove(j);
					k = 1;
					break;
				}
			}
			if (k == 0) {
				j++;
			}
		}
		if (!localArrayList2.isEmpty()) {
			for (j = 0; j < arrayOfXMLATreeNode4Display1[i].length; j++) {
				k = 1;
				Iterator<String> localIterator = localArrayList2.iterator();
				while (localIterator.hasNext()) {
					CharSequence localCharSequence = (CharSequence) localIterator.next();
					if (arrayOfXMLATreeNode4Display1[i][j].getUniqueName().contains(localCharSequence)) {
						if (StringUtils.equals(localCharSequence.toString(), arrayOfXMLATreeNode4Display1[i][j])) {
							localArrayList1.add(arrayOfXMLATreeNode4Display1[i][j].getUniqueName());
						}
						k = 0;
					}
				}
				if (k != 0) {
					localArrayList1.add(arrayOfXMLATreeNode4Display1[i][j].getUniqueName());
				}
			}
		} else {
			for (j = 0; j < arrayOfXMLATreeNode4Display1[i].length; j++) {
				localArrayList1.add(arrayOfXMLATreeNode4Display1[i][j].getUniqueName());
			}
		}
		String[] arrayOfString2 = new String[localArrayList1.size()];
		localArrayList1.toArray(arrayOfString2);
		String[] arrayOfString4 = new String[arrayOfXMLATreeNode4Display.length];
		for (int n = 0; n < arrayOfString4.length; n++) {
			arrayOfString4[n] = arrayOfXMLATreeNode4Display[n].getUniqueName();
		}
		//System.out.println("cols=" + arrayOfString2 + ";rows=" + arrayOfString4);
	}
}
