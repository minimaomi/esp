package com.raqsoft.lib.olap4j.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.Position;
import org.olap4j.driver.xmla.XMLAByteBufferTable;
import org.olap4j.driver.xmla.XmlaOlap4jCellSet;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Member;

import com.raqsoft.common.RQException;
import com.raqsoft.dm.DataStruct;
import com.raqsoft.dm.Record;
import com.raqsoft.dm.Table;

public class Utils {
	public static String[] objectArray2StringArray(Object[] objs) {
		return Arrays.asList(objs).toArray(new String[0]);
	}

	/**
	 * If enabled and applicable to this command, print the field headers for
	 * the output.
	 * 
	 * @param qp
	 *            Driver that executed the command
	 * @param out
	 *            PrintStream which to send output to
	 */

	// �Ƿ�Ϸ���sql���.
	public static boolean isLegalSql(String strSql) {
		String span = strSql.toUpperCase();// ������sql���
		System.out.println(span);
		String column = "(\\w+\\s*(\\w+\\s*){0,1})";// һ�е�������ʽ ƥ���� product p
		String columns = column + "(,\\s*" + column + ")*"; // ����������ʽ ƥ����
															// product
															// p,category
															// c,warehouse w
		// һ�е�������ʽƥ����a.product p
		String ownerenable = "((\\w+\\.){0,1}\\w+\\s*(\\w+\\s*){0,1})";
		// ����������ʽƥ����a.product p,a.category c,b.warehouse w
		String ownerenables = ownerenable + "(,\\s*" + ownerenable + ")*";
		String from = "FROM\\s+" + columns;
		// ������������ʽƥ����a=b��a is b..
		String condition = "(\\w+\\.){0,1}\\w+\\s*(=|LIKE|IS)\\s*'?(\\w+\\.){0,1}[\\w%]+'?";
		// �������ƥ����a=b and c like 'r%' or d is null
		String conditions = condition + "(\\s+(AND|OR)\\s*" + condition + "\\s*)*";
		String where = "(WHERE\\s+" + conditions + "){0,1}";
		String pattern = "SELECT\\s+(\\*|" + ownerenables + "\\s+" + from + ")\\s+" + where + "\\s*"; // ƥ������sql��������ʽ
		// System.out.println(pattern);// ���������ʽ

		boolean bRet = span.matches(pattern);// �Ƿ����
		return bRet;
	}

	// ͨ��Url��ȡ��������port, warehouse
	public static boolean isMatch(String strUrl, String regExp, Matcher[] retMatch) {
		// 1.ͨ��Url��ȡ��������port, warehouse
		// String regex="hdfs:\\/\\/(.*?):(\\d+)(\\/.*)";
		// 2.ͨ��Url��ȡ��������port
		// String regex="hdfs:\\/\\/(.*?):(\\d+)";
		if (strUrl == null || strUrl.isEmpty()) {
			return false;
		}

		if (regExp == null || regExp.isEmpty()) {
			throw new RQException("isMatch regExp is empty");
		}

		Pattern p = Pattern.compile(regExp);
		retMatch[0] = p.matcher(strUrl);

		return retMatch[0].find();
	}

	// ������
	public static void doPrint(List<Object> result) {
		for (Object row : result) {
			System.out.println(row.toString());
		}
	}

	public static List<List<Object>> resultsConvertDList(List<Object> result) {
		if (result == null)
			return null;
		if (result.size() == 0)
			return null;

		List<List<Object>> lls = new ArrayList<List<Object>>();
		for (Object row : result) {
			String[] sourceStrArray = row.toString().split("\t");
			List list = Arrays.asList(sourceStrArray);
			lls.add(list);
		}

		return lls;
	}

	public static void testPrintTable(Table table) {
		if (table == null)
			return;
		System.out.println("size = " + table.length());

		DataStruct ds = table.dataStruct();
		String[] fields = ds.getFieldNames();
		int i = 0;
		// print colNames;
		for (i = 0; i < fields.length; i++) {
			System.out.print(fields[i] + "\t");
		}
		System.out.println();
		// print tableData
		for (i = 0; i < table.length(); i++) {
			Record rc = table.getRecord(i + 1);
			Object[] objs = rc.getFieldValues();
			for (Object o : objs) {
				System.out.printf(o + "\t");
			}
			System.out.println();
		}
	}

	public static Table toSapTable(CellSet cellSet) {
		Table table = null;

		if (cellSet == null) {
			return null;
		}
		XMLAByteBufferTable xtable = (XMLAByteBufferTable) ((XmlaOlap4jCellSet) cellSet).getDataBuf();
		List<String> ls = xtable.getColumn();
		String colNames[] = new String[ls.size()];
		ls.toArray(colNames);

		table = new Table(colNames);
		Object[] objs = new Object[colNames.length];

		for (int i = 0; i < xtable.rowCount(); i++) {
			for (int j = 0; j < colNames.length; j++) {
				objs[j] = xtable.getValueAt(j, i);
			}
			table.newLast(objs);
		}

		return table;
	}

	public static Table toTable(CellSet cellSet, String[] colNames) {
		int k = 0;
		Table table = new Table(colNames);
		Object[] objs = new Object[colNames.length];

		if (cellSet == null) {
			return null;
		}

		if (cellSet.getAxes().size() == 2) {
			for (Position row : cellSet.getAxes().get(1)) {
				k = 0;
				for (Member member : row.getMembers()) {
					objs[k++] = member.getName();
				}

				for (Position column : cellSet.getAxes().get(0)) {
					final Cell cell = cellSet.getCell(column, row);
					objs[k++] = cell.getFormattedValue();
				}
				table.newLast(objs);
			}
		} else if (cellSet.getAxes().size() == 1) {
			for (Position column : cellSet.getAxes().get(0)) {
				final Cell cell = cellSet.getCell(column);
				objs[k++] = cell.getFormattedValue();
			}
			table.newLast(objs);
		}

		return table;
	}

	public static String[] getColumnNames(CellSet cellSet) {
		List<String> colNames = new ArrayList<String>();
		if (cellSet.getAxes().size() == 2) {
			for (Position row : cellSet.getAxes().get(1)) {
				String regExp = "([\\s\\S]*)\\[(.*)\\]\\.[\\[all.*?\\] | &\\[\\s\\S]*]$";
				String name = "";
				Pattern p = Pattern.compile(regExp);
				for (Member member : row.getMembers()) {
					Dimension dm = member.getDimension();
					Member mem = member.getParentMember();
					if (mem != null) {
						name = mem.getUniqueName();
						Matcher m = p.matcher(name.toLowerCase());
						boolean bMatch = false;
						if (m.find()) {
							if (m.groupCount() == 2) { // ������1��ʼ
								colNames.add(m.group(2));
								bMatch = true;
							}
						}
						if (!bMatch) {
							colNames.add("");
						}
					} else if (dm != null) {
						colNames.add(dm.getCaption());
					} else {
						colNames.add("");
					}
				}
				break;
			}
		}
		String colName = "";
		for (Position pos : cellSet.getAxes().get(0)) {
			colName = "";
			List<Member> members = pos.getMembers();
			for (Member m : members) {
				if (colName.isEmpty()) {
					colName = m.getCaption();
				} else {
					colName += "_" + m.getCaption();
				}
			}
			colNames.add(colName);
		}
		
		String[] strings = new String[colNames.size()];
		colNames.toArray(strings);

		return strings;
	}
}
