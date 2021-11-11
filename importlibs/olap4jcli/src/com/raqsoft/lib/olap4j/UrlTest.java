package com.raqsoft.lib.olap4j;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.olap4j.driver.xmla.XMLAByteBufferTable;
import org.olap4j.driver.xmla.XMLASaxParser;

import com.raqsoft.common.Base64;



public class UrlTest {
	private HttpURLConnection urlConnection;
	private URL endpoint;

	private String getMdxStringEx(String dataSourceInfo,String catalog,String mdx) {
		
		StringBuilder buf = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<soapenv:Envelope\n"
				+ "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
				// + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
				// + "
				// xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
				+ "      soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" + "    <soapenv:Body>\n"
				// + " <Execute
				// xmlns=\"urn:schemas-microsoft-com:xml-analysis\">\n"
				+ "        <Execute xmlns=\"urn:schemas-microsoft-com:xml-analysis\"\n"
				+ "        soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" + "        <Command>\n"
				+ "        <Statement>\n"
				+ " <![CDATA[\n" + mdx + "]]>\n"
				//+ "           " + mdx + " \n" 
				+ "         </Statement>\n" + "        </Command>\n"
				+ "        <Properties>\n" + "          <PropertyList>\n");
		if (catalog != null) {
			buf.append("            <Catalog>");
			buf.append(catalog);
			buf.append("</Catalog>\n");
		}

		if (dataSourceInfo != null) {
			buf.append("            <DataSourceInfo>");
			buf.append(dataSourceInfo);
			buf.append("</DataSourceInfo>\n");
		}
		buf.append("            <Format>Multidimensional</Format>\n" + "            <Content>Data</Content>\n"
				+ "            <AxisFormat>TupleFormat</AxisFormat>\n" + "          </PropertyList>\n"
				+ "        </Properties>\n" + "</Execute>\n" + "</soapenv:Body>\n" + "</soapenv:Envelope>");
		final String request = buf.toString();
		System.out.println(request);

		return request;
	}
	
	
	private String getMdxString(String dataSourceInfo,String catalog,String mdx) {
        StringBuilder buf = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soapenv:Envelope\n"
                + "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
                + "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
                + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"

                + "    <soapenv:Body>\n"

                + "        <Execute xmlns=\"urn:schemas-microsoft-com:xml-analysis\">\n"

                + "        <Command>\n"
                + "        <Statement>\n"

                + "           <![CDATA[\n" + mdx + "]]>\n"
                + "         </Statement>\n"
                + "        </Command>\n"
                + "        <Properties>\n"
                + "          <PropertyList>\n");

		if (catalog != null) {
			buf.append("            <Catalog>");
			buf.append(catalog);
			buf.append("</Catalog>\n");
		}

		if (dataSourceInfo != null) {
			buf.append("            <DataSourceInfo>");
			buf.append(dataSourceInfo);
			buf.append("</DataSourceInfo>\n");
		}
		buf.append("            <Format>Multidimensional</Format>\n" + "            <Content>Data</Content>\n"
				+ "            <AxisFormat>TupleFormat</AxisFormat>\n" + "          </PropertyList>\n"
				+ "        </Properties>\n" + "</Execute>\n" + "</soapenv:Body>\n" + "</soapenv:Envelope>");
		final String request = buf.toString();
		System.out.println(request);

		return request;
	}

	private boolean init(String sUrl, String paramString1, String paramString2) throws Exception {
		try {
			endpoint = new URL(sUrl);
			String str1 = paramString1 + ":" + paramString2;
			// String str2 = new BASE64Encoder().encode(str1.getBytes());
			final Base64 base64 = new Base64();
			String str2 = base64.byteArrayToBase64(str1.getBytes());
			urlConnection = ((HttpURLConnection) endpoint.openConnection());
			urlConnection.setRequestProperty("Authorization", "Basic " + str2);
			// urlConnection.setConnectTimeout(30000);
			// urlConnection.setReadTimeout(3600000);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
		} catch (IOException localIOException) {
			throw new Exception(urlConnection.toString() + " is not available or not able to respond");
		}
		return true;
	}
	
	private void writeToStream(byte[] paramArrayOfByte, OutputStream paramOutputStream)
		    throws IOException
		  {
		    BufferedOutputStream localBufferedOutputStream = null;
		    try
		    {
		      localBufferedOutputStream = new BufferedOutputStream(paramOutputStream);
		      localBufferedOutputStream.write(paramArrayOfByte);
		      localBufferedOutputStream.flush();
		      return;
		    }
		    finally
		    {
		      try
		      {
		        if (localBufferedOutputStream != null) {
		          localBufferedOutputStream.close();
		        }
		        if (paramOutputStream != null) {
		          paramOutputStream.close();
		        }
		      }
		      catch (IOException localIOException2) {}
		    }
		  }

	private void doSapExcute(){
		try {
			String dataSourceInfo = "SAP_BW";
			String catalog = "ZFIGL_C10";
			//String mdx = "select {[Measures].[00O2TOL47DO0XRI5G4J3VZ7KQ]} on columns, Non Empty{{[0GL_ACCOUNT].[LEVEL01].Members}} on rows from [ZFIGL_C10/ZFIGL_C10_QTEST01] ";
			String mdx = "select {[Measures].[00O2TOL47DO0XRI5G4J3VZ7KQ],[Measures].[00O2TOL47DO2O6IGIXSWG3LH0],[Measures].[00O2TOL47DO2O6IGIXSWG3RSK]} on columns,"
					+" Non Empty{{[0COMP_CODE].[LEVEL01].Members}*{[0GL_ACCOUNT].[LEVEL01].Members}} on "
					+" rows from [ZFIGL_C10/ZFIGL_C10_QTEST01] sap variables [!V000001] including 2017 ";
			String user="ZTF_WANGQQ";
			String passwd="ccs123456";
			String resMdx = getMdxString(dataSourceInfo, catalog, mdx);
			String sUrl = "http://10.188.186.49:8000/sap/bw/xml/soap/xmla?sap-client=300";
			init(sUrl, "ZTF_WANGQQ", "ccs123456");

			writeToStream( resMdx.getBytes(), urlConnection.getOutputStream());
			InputStream in = urlConnection.getInputStream();
			
			 
			 SAXParserFactory localSAXParserFactory = SAXParserFactory.newInstance();
		        SAXParser localSAXParser = localSAXParserFactory.newSAXParser();
		        XMLASaxParser localXMLASaxParser = new XMLASaxParser();
		        //localSAXParser.parse(urlConnection.getInputStream(), localXMLASaxParser);
		        localSAXParser.parse(in, localXMLASaxParser);
		        List<String> axis0 = localXMLASaxParser.getAxis0();
		        List<String> axis1 = localXMLASaxParser.getAxis1();
		        XMLAByteBufferTable mappedByteBufferTable = localXMLASaxParser.getMappedByteBufferTable();
		        int columnNum = (axis0.size() + axis1.size());			    
			      for(int i=0; i<mappedByteBufferTable.rowCount(); i++){
			    	  for(int j=0; j<columnNum; j++){
			    		  System.out.print(mappedByteBufferTable.getValueAt(j, i)+"\t");
			    	  }
			    	  System.out.println();
			      }
		        System.out.println("aaa");

			urlConnection.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//http://localhost/olap/msmdpump.dll","Analysis Services Tutorial","administrator","rootpwd")
	private void doMsExcute(){
		try {
			String dataSourceInfo = "MicroWin10-1119";
			String catalog = "Analysis Services Tutorial";
			String mdx = "SELECT "
				  + " non empty [Due Date].[Calendar Year].&[2001]* [Product].[Product Key].Members ON 0 , "
				  + " [Due Date].[English Month Name].[English Month Name] on 1 "
				+ " FROM [Analysis Services Tutorial] "
				+ " WHERE [Measures].[Internet Sales ¼ÆÊý]";
			
			String resMdx = getMdxStringEx(dataSourceInfo, catalog, mdx);
			String sUrl = "http://localhost/olap/msmdpump.dll";
			init(sUrl, "administrator","rootpwd");
			
			writeToStream( resMdx.getBytes(), urlConnection.getOutputStream());
			InputStream in = urlConnection.getInputStream();
			byte[] bt = new byte[1024];
			while(in.read(bt)!=-1){
				String ss = new String(bt);
				System.out.println(ss);
			}
			
			
			//Document localDocument = builder.parse(urlConnection.getInputStream());
			urlConnection.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void doParse(InputStream in){
		try {
			SAXParserFactory localSAXParserFactory = SAXParserFactory.newInstance();
	        SAXParser localSAXParser = localSAXParserFactory.newSAXParser();
	        XMLASaxParser localXMLASaxParser = new XMLASaxParser();
	        //localSAXParser.parse(urlConnection.getInputStream(), localXMLASaxParser);
	        localSAXParser.parse(in, localXMLASaxParser);
	        List<String> axis0 = localXMLASaxParser.getAxis0();
	        List<String> axis1 = localXMLASaxParser.getAxis1();
	        XMLAByteBufferTable mappedByteBufferTable = localXMLASaxParser.getMappedByteBufferTable();
	        List<String> cols = localXMLASaxParser.getColumn();
	        int columnNum = (axis0.size() + axis1.size());			    
		      for(int i=0; i<mappedByteBufferTable.rowCount(); i++){
		    	  for(int j=0; j<columnNum; j++){
		    		  System.out.print(mappedByteBufferTable.getValueAt(j, i)+"\t");
		    	  }
		    	  System.out.println();
		      }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String argc[]) {
		try {
			UrlTest cls = new UrlTest();
			if (1==2){
				//cls.doSapExcute();
				cls.doMsExcute();
			}else{
				//InputStream in = new FileInputStream("D:/dev/workspace/olap4j/test.xml");
				InputStream in = new FileInputStream("D:/dev/workspace/olap4j/olap4jcli/docs/test3.xml");
				cls.doParse(in);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	{
//		SAXParserFactory localSAXParserFactory = SAXParserFactory.newInstance();
//        SAXParser localSAXParser = localSAXParserFactory.newSAXParser();
//        XMLASaxParser localXMLASaxParser = new XMLASaxParser();
//        localSAXParser.parse(urlConnection.getInputStream(), localXMLASaxParser);
//        axis0 = localXMLASaxParser.getAxis0();
//        axis1 = localXMLASaxParser.getAxis1();
//        mappedByteBufferTable = localXMLASaxParser.getMappedByteBufferTable();
//        for(int i=0; i<mappedByteBufferTable.columnCount(); i++){
//        	for(int j=0; j<mappedByteBufferTable.getAssignLength(i); j++){
//        		System.out.println(mappedByteBufferTable.getValueAt(i, j));
//        	}
//        	
//        }
//	}
}
