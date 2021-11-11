package com.raqsoft.lib.ogg;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDateTime;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.DataStruct;
import com.raqsoft.dm.FileObject;
import com.raqsoft.dm.Record;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.Table;
import com.raqsoft.expression.Expression;

/**
 * MergeFile类
 * 关于datetime,它不有关心时间的分、秒，则按小时记就行。
 * 与同步程序有冲突怎么办？加文件锁
 * 
 * *******************************************************/
public class MergeFile {
	private String m_savePath = "./";
	private List<String> m_bufList; //btx列表
	private List<String> m_pkList; //主键
	private Sequence[] m_seqs; // 0:all; 1:I; 2:D
	private String m_outfileType = "btx";

	public MergeFile() {
		m_bufList = new  ArrayList<String>();
		m_pkList = new ArrayList<String>();
		m_seqs = new Table[3];
	}

	private void readControlFile(String tableName, Date d){
		try {
			m_bufList.clear();
			m_pkList.clear();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        	String sTime = df.format(d);
        	String controlFile = m_savePath + "/dirout/control/" + tableName.replace(".", "_")+"_"+sTime+ "_data.ctrl";
        	String pkFile = m_savePath + "/dirout/control/PK_" + tableName.replace(".", "_")+ "_info.txt";
        	
        	df = new SimpleDateFormat("yyyy-MM-dd_HH");
        	sTime = df.format(d);
        	// 1. for ctrl file
        	File fh = new File(controlFile);
        	if (!fh.exists()){
        		return;
        	}
			FileReader reader = new FileReader(controlFile);
			char[] buf = new char[1024];
			StringBuffer sb = new StringBuffer();
			while(reader.read(buf)!=-1){
				sb.append(new String(buf));
			}
			//System.out.println(sb.toString());
			String[] vs = sb.toString().trim().split("\r\n");
			df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			for(String s:vs){
				int offset = s.indexOf(tableName)+tableName.length()+1;
				String sDate = s.substring(offset, offset+19);
				Date fileDate = df.parse(sDate); 
				if(fileDate.compareTo (d)==1){
					fh = new File(m_savePath + "/"+s);
		        	if (fh.exists()){
		        		m_bufList.add(s);
		        	}
				}
			}
			
			// 若无合并文件，则直接返回.
			if (m_bufList.size()==0){
				return;
			}
//			if (!vs[0].isEmpty() ){
//				m_bufList = Arrays.asList(vs); 
//			}
			// 2. for pk(primary key) file
			buf = new char[1024*4];
			reader = new FileReader(pkFile);
			reader.read(buf);
			String pkVal = new String(buf).trim();	
			vs = pkVal.split("\r\n"); 
			if (vs!=null && vs.length==3){
				pkVal = vs[2].replace("PK=1;", "");
				pkVal = pkVal.replace("PK=0;", "");
				String[] vpk = pkVal.split(";");
				for ( String s:vpk ){
					m_pkList.add(s); 
				}
			}else{
				System.out.println(pkFile+" is not right");
			}
			//System.out.println("fileSize = "+m_bufList.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doMerge(String tableName, Date d) {
		String tblName = tableName.toUpperCase();
		readControlFile(tblName, d);
		if(m_bufList.size()>0){
			readFiles(d);
			if (parseData()){
				saveData(tblName);
			}
			
			for(String f:m_bufList){
				//System.out.println("file = " + f);
				File file = new File(m_savePath + "/"+f);
				//System.out.println(file.exists() + ";isfile = " + file.isFile());
				if(file.exists() && file.isFile()){
					file.delete();
				}
			}
		}
	}
	
	private void saveData(String tableName ){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String outFile = "";
        String controlFile = "";
        
    	String sTime = df.format(new Date());
    	String savePath =  m_savePath + "/dirout/merge";
    	File f = new File(savePath);
    	if (!f.exists()){
    		f.mkdir();
    	}
    	
    	String txtFile = null;
    	if(m_outfileType.equalsIgnoreCase("btx")){
	    	outFile = savePath + "/PUMP_" + tableName.replace(".", "_")+"_I_"+ sTime+".btx";
			WriteSequenceToFile(m_seqs[1],outFile);
			m_seqs[1].clear();
			
			outFile = outFile.replace("_I_", "_D_");
			WriteSequenceToFile(m_seqs[2],outFile);
    	}else{
			txtFile = savePath + "/TEST_" + tableName.replace(".", "_")+"_I_"+ sTime+".txt";
			testSaveTable((Table)m_seqs[1], txtFile);
			m_seqs[1].clear();
			txtFile = txtFile.replace("_I_", "_D_");
			testSaveTable((Table)m_seqs[2], txtFile);
    	}
		
		m_seqs[2].clear();
	}
	
	public void testSaveTable(Table table, String file) {
		if (table == null)
			return;
		//System.out.println("size = " + table.length());
		try {
			DataStruct ds = table.dataStruct();
			String[] fields = ds.getFieldNames();
			StringBuilder sb = new StringBuilder();
			int i = 0;
			// print colNames;
			for (i = 0; i < fields.length; i++) {
				//System.out.print(fields[i] + "\t");
				sb.append(fields[i]+";");
			}
			sb.append("\r\n");
			
			FileWriter fw = new FileWriter(file);
			for (i = 0; i < table.length(); i++) {
				Record rc = table.getRecord(i + 1);
				Object[] objs = rc.getFieldValues();
				for (Object o : objs) {
					sb.append(o+";");
				}
				sb.append("\r\n");
				if (i>0 && i%10000==0){
					fw.write(sb.toString());
					sb.setLength(0);
				}
			}
			
			fw.write(sb.toString());
			
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean parseData(){
		Table sTable = (Table)m_seqs[0];
		Table iTable = (Table)m_seqs[1];
		Table dTable = (Table)m_seqs[2];
		
		DataStruct ds = sTable.dataStruct();
		if (ds==null){
			System.out.println("table struct is null");
			return false;
		}
		String[] fields = ds.getFieldNames();
		int i = 0;
		// print colNames;
//		for (i = 0; i < fields.length; i++) {
//			System.out.print(fields[i] + "\t");
//		}
		
		//System.out.println("\n2. insert");
		String filter = "";
		Expression exp = null;
		Sequence seq = null;
		Context ctx = new Context();
		
		for (i = 0; i < sTable.length(); i++) {
			Record rc = sTable.getRecord(i + 1);
			Object[] objs = rc.getFieldValues();
//			for (Object o : objs) {
//				System.out.printf(o + "\t");
//			}
			//System.out.println();
			
			if (objs[0].toString().equals("I")){
				iTable.newLast(objs);
			}else if (objs[0].toString().equals("D")){
				filter = "OP==\"I\"";
				for(String s : m_pkList){
					Object obj = rc.getFieldValue(s);
					filter += " && "+s+"==\""+obj+"\"";
				}
				exp = new Expression(ctx, filter);
				seq = (Sequence)iTable.select(exp, "", ctx);
				if (seq.length()>0){
					iTable.delete(seq, null);
				}
				dTable.newLast(objs);
			}
		}
		sTable.clear();
		
		Table newSeq = new Table(iTable.dataStruct());
		Set setTmp = new HashSet();
		String pkVal="";
		// remove duplicate　pk for dTable		
		for (i = dTable.length(); i >0 ; i--) {
			pkVal="";
			Record rc = dTable.getRecord(i);
			for(String s : m_pkList){
				Object obj = rc.getFieldValue(s);
				if (pkVal.isEmpty()){
					pkVal = obj.toString();
				}else{
					pkVal += ","+obj;
				}
			}
			if (!setTmp.contains(pkVal)){
				newSeq.newLast(rc.getFieldValues());
				setTmp.add(pkVal);
				//System.out.println("key = " + pkVal);
			}			
		}
		m_seqs[2] = newSeq;
		
		return true;
	}
		
	private void mergeData(){
		Table iTable = (Table)m_seqs[0];
		Table uTable = (Table)m_seqs[1];
		Table dTable = (Table)m_seqs[2];
		
		DataStruct ds = iTable.dataStruct();
		if (ds==null){
			System.out.println("table struct is null");
			return;
		}
		String[] fields = ds.getFieldNames();
		int i = 0;
		// print colNames;
//		for (i = 0; i < fields.length; i++) {
//			System.out.print(fields[i] + "\t");
//		}
//		
//		System.out.println("\n2. insert");
//		for (i = 0; i < iTable.length(); i++) {
//			Record rc = iTable.getRecord(i + 1);
//			Object[] objs = rc.getFieldValues();
//			for (Object o : objs) {
//				System.out.printf(o + "\t");
//			}
//			System.out.println();
//		}
		
		String filter = "";
		Expression exp = null;
		Sequence seq = null;
		Context ctx = new Context();

		System.out.println("3. Update");
		List<Integer> ls = new ArrayList<Integer>();
		for (i = 0; i < uTable.length(); i++) {
			Record rc = uTable.getRecord(i + 1);			
			Object[] objs = rc.getFieldValues();
			
			for (Object o : objs) {
				System.out.printf(o + "\t");
			}
			System.out.println();

			if (objs[0].toString().compareTo("D")==0){
				for(String s : m_pkList){
					Object obj = rc.getFieldValue(s);
					if (filter.isEmpty()){
						filter += s+"==\""+obj+"\n";
					}else{
						filter += ","+s+"==\""+obj+"\n";
					}
				}
				exp = new Expression(ctx, filter);
				Object ret = iTable.select(exp, "", ctx);
				iTable.delete((Sequence)ret, null);
				ls.add(i+1);
			}else if (objs[0].toString().compareTo("I")==0){
				iTable.newLast(objs);
				ls.add(i+1);
			}
		}
		
		// remove I and D from updateList
		for (i=ls.size()-1; i>=0; i--){
			uTable.delete(ls.get(i));
		}
		
		// update不能合并pk重复的值，它的set col=xxx可能不同　
		// remove duplicate　pk for uTable
//		Table newSeq = new Table(iTable.dataStruct());
//		Set setTmp = new HashSet();
//		String pkVal="";
//		for (i = uTable.length(); i >0 ; i--) {
//			Record rc = uTable.getRecord(i);
//			for(String s : m_pkList){
//				Object obj = rc.getFieldValue(s);
//				if (pkVal.isEmpty()){
//					pkVal += obj;
//				}else{
//					pkVal += ","+obj;
//				}
//			}
//			if (!setTmp.contains(pkVal)){
//				newSeq.newLast(rc.getFieldValues());
//				setTmp.add(pkVal);
//				System.out.println("key = " + pkVal);
//			}			
//		}
//		m_seqs[1] = newSeq;		
		System.out.println("Delete");
		for (i = 0; i < dTable.length(); i++) {
			Record rc = dTable.getRecord(i + 1);
			Object[] objs = rc.getFieldValues();
			for (Object o : objs) {
				System.out.printf(o + "\t");
			}
			System.out.println();
		}
		
		Table newSeq = new Table(iTable.dataStruct());
		Set setTmp = new HashSet();
		String pkVal="";
		// remove duplicate　pk for dTable		
		newSeq=new Table(iTable.dataStruct());
		for (i = dTable.length(); i >0 ; i--) {
			Record rc = dTable.getRecord(i);
			for(String s : m_pkList){
				Object obj = rc.getFieldValue(s);
				if (pkVal.isEmpty()){
					pkVal += obj;
				}else{
					pkVal += ","+obj;
				}
			}
			if (!setTmp.contains(pkVal)){
				newSeq.newLast(rc.getFieldValues());
				setTmp.add(pkVal);
				System.out.println("key = " + pkVal);
			}			
		}
		m_seqs[2] = newSeq;		
		
		dTable = newSeq;
		for (i = 0; i < dTable.length(); i++) {
			Record rc = dTable.getRecord(i + 1);
			for(String s : m_pkList){
				Object obj = rc.getFieldValue(s);
				if (pkVal.isEmpty()){
					pkVal += obj;
				}else{
					pkVal += ","+obj;
				}
			}
			exp = new Expression(ctx, "ID==\""+pkVal+"\"");
			Object ret = iTable.select(exp, "", ctx);
			if (ret!=null){
				iTable.delete((Sequence)ret, null);
			}	
			ret = uTable.select(exp, "", ctx);
			if (ret!=null){
				uTable.delete((Sequence)ret, null);
			}			
		}
	}
	
/*
 * for(String s:m_list[0]){
	//System.out.println(s);
	if(s.indexOf(vs[0])!=-1){
		m_list[1].add(s);
	}else if(s.indexOf(vs[1])!=-1){
		m_list[2].add(s);
	}else if(s.indexOf(vs[2])!=-1){
		m_list[3].add(s);
	}else{
		System.out.println("not found: "+s);
	}			
 */
	


	private void readFiles(Date d) {
		Sequence seq = null;
		// insert file
		m_seqs[0] = new Table();
		for(String s: m_bufList){
			seq = readBtxFile(m_savePath + "/"+s);
			if (m_seqs[0].dataStruct()==null){
				m_seqs[0] = seq;
			}else{
				m_seqs[0].addAll(seq);
			}
		}
		
		for(int n=1; n<3; n++){
    		m_seqs[n]=new Table(m_seqs[0].dataStruct());
    	}
	}
	
	private Sequence readBtxFile(String filePath) {
		Sequence ret = null;
		RandomAccessFile randomAccessFile = null;
        FileChannel channel = null;
        String lockFP = filePath.replace(".btx", ".lck");
        
    	try {
    		randomAccessFile = new RandomAccessFile(lockFP, "rw");
            channel = randomAccessFile.getChannel();
            FileLock lock = null;

            while (true) {
                lock = channel.tryLock();

                if (null == lock) {
                    System.out.println("Read Process : get lock failed");
                    Thread.sleep(1000);
                } else {
                    break;
                }
            }
            
            FileObject fo = new FileObject(filePath); 
        	ret = fo.importSeries("b"); 
        	lock.release();
        } catch (Exception e) {     
            //e.printStackTrace();  
            System.out.println("writeToFile: "+e.getMessage()); 
        } finally {
        	try{
	            if (null != randomAccessFile) {
	                randomAccessFile.close();
	            }
	            if (null != channel) {
	                channel.close();
	            }
	            
	            File f = new File(lockFP);
	            f.deleteOnExit();
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        } 
    	
    	return ret;
    }
	
	 public void WriteSequenceToFile(Sequence seq,String filePath) {   
        try {  
        	if(seq.length()<1) return;
        	//System.out.println("DDDDDDD :" + filePath); 
        	//testPrintTable((Table)seq);      
            FileObject fo = new FileObject(filePath); 
        	fo.exportSeries(seq, "b", "|"); 
        	//seq.clear();
        } catch (Exception e) {     
            e.printStackTrace();  
        	//logger.error("writeToFile: "+e.getMessage()); 
        } finally{
        	
        }
    }   
	////////////////////////////// for test
	////////////////////////////// //////////////////////////////////////////
	public static void main(String[] args) {
		try {
			boolean bAuto = true;
			int interval = 60;
			String sDate = null, sType = "btx";
			
			MergeFile mgf = new MergeFile();
			System.out.println("/**************************************************\n"+
					" * Usage: merge.bat path key:value,....\n"+
					" * key: model, datetime, interval, filetype\n"+
					" * 1. model:使用模式，分为自动处理auto，手动处理manual、缺省为自动auto;\n" + 
					" * 2. datetime: 手动处理合并文件的启始时间，时间格式为yyyy-MM-dd HH:00:00\n"+
					" * 3. interval: 自动处理的间隔时间，缺省为60分钟(单位为分钟);\n" +
					" * 4. filetype：输出文件格式，分为txt、btx文件格式，缺省为btx\n"+					
					" * Auto Example: \n"+
					" * merge.bat ./ \"model:auto,datetime:2021-08-20 14:00:00,interval:5,filetype:txt\"\n"+
					"**************************************************/\n\n");
			if(args.length>=1){
				mgf.m_savePath = args[0];
			}
			if(args.length==2)
			{
				String[] vs = args[1].split(",");
				for(String line:vs){
					System.out.println(line);
					String[] sub = line.split(":");
					if (sub.length==2 || sub.length==4){
						if (sub[0].equalsIgnoreCase("model")){
							if (sub[1].equalsIgnoreCase("manual")){
								bAuto = false;
							}
						}else if (sub[0].equalsIgnoreCase("datetime")){
							sDate = line.toLowerCase().replace("datetime:", "");
						}else if (sub[0].equalsIgnoreCase("interval")){
							interval = Integer.parseInt(sub[1]);
						}else if (sub[0].equalsIgnoreCase("filetype")){
							mgf.m_outfileType = sub[1];
						}
					}else{
						System.out.println(line + " is not key:value");
					}
				}
			}
			
			//System.out.println(args.length + "; bManual="+bManual);
			if (1==2){
				mgf.doSingleTable();
			}else if(!bAuto){
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String ss = "";
				if (sDate==null || sDate.isEmpty()){
					ss = sf.format(new Date());
				}else{
					ss = sDate;
				}
				//System.out.println("date = " + ss);
				String newDate = ss.substring(0, ss.length()-6)+":00:00";				
				Date d = sf.parse(newDate);
				mgf.doMultiTable(d);
			}else{			
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String ss = sf.format(new Date());
				LocalDateTime now = LocalDateTime.now();
		        //System.out.println(now.toString());
		        
		        while(true){
		        	now = LocalDateTime.now();
		        	//System.out.print(now.getHourOfDay()+":"+now.getMinuteOfHour()+":"+now.getSecondOfMinute()+"\t"); 
		        	LocalDateTime startTime = now.minusMinutes(interval);
		        	//System.out.println(startTime.getHourOfDay()+":"+startTime.getMinuteOfHour()+":"+startTime.getSecondOfMinute()); 
		        	if (now.getMinuteOfHour()>0 ){
//		        		ss = String.format("%d-%d-%d %d:00:00", now.getYear(),now.getMonthOfYear(),
//		        				now.getDayOfMonth(),now.getHourOfDay()-1);
		        		System.out.println(ss);
		        		
		        		mgf.doMultiTable(startTime.toDate());	        		
		        	}
		        	
					Thread.sleep(1000*interval*60);
		        	//Thread.sleep(1000*interval);
		        }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void doMultiTable(Date d) {
		String lockFile = m_savePath + "/dirout/merging.lck";
		File file = new File(lockFile);
		try {
			String pkFile = m_savePath + "/dirout/control";
			try{
				if(!file.exists()){
					file.createNewFile();
				}
			}catch(IOException e){
				;
			}
			
			List<String> ls = findFile(new File(pkFile), ".txt");
			if (ls==null) return;
			for (String s : ls){
				doMerge(s, d);
			}
			
			//System.out.println("end: ");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			file.deleteOnExit();
		}
	}

	public void doSingleTable() {
		try {
			// readBtxFile();
			
			// s.getTimeSegment(15);
			// s.autoGenericCode(123, 5);
			String sDate = "2019-04-12 16:00:00";
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date d = sf.parse(sDate);
			
			doMerge("OGG.TT", d);
			// s.sequenceOpt();
			//System.out.println("end: ");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<String> findFile(File dir, String suffix) throws IOException {
		List<String> ls = new ArrayList<String>();
		File[] dirFiles = dir.listFiles();
		if (dirFiles==null) return null;
		for (File temp : dirFiles) {
			if (!temp.isFile()) {
				findFile(temp, suffix);
			}
			// 查找指定的文件
			String fName = "";
			if (temp.isFile() && temp.getAbsolutePath().endsWith(suffix)) {
				//System.out.println(temp.isFile() + " " + temp.getName());
				fName = temp.getName();
				fName = fName.substring(3, fName.length()-9);
				ls.add(fName);
				//readFileContent(temp);
			}
		}
		
		return ls;
	}
		
}
