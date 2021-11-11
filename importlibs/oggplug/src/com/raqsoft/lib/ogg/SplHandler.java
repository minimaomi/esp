package com.raqsoft.lib.ogg;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//这是 OGG 11 的 import 
//import com.goldengate.atg.datasource.AbstractHandler;
//import com.goldengate.atg.datasource.DsConfiguration;
//import com.goldengate.atg.datasource.DsEvent;
//import com.goldengate.atg.datasource.GGDataSource.Status;
//import com.goldengate.atg.datasource.handler.*;
//import com.goldengate.atg.datasource.meta.DsMetaData;
//import com.goldengate.atg.datasource.test.DsTestUtils.Logger;
//import com.goldengate.atg.datasource.meta.*;
//import com.goldengate.atg.datasource.*;

//这是 OGG 12 的 import 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raqsoft.dm.DataStruct;
import com.raqsoft.dm.FileObject;
import com.raqsoft.dm.Record;
import com.raqsoft.dm.Sequence;
import com.raqsoft.dm.Table;
import oracle.goldengate.datasource.AbstractHandler;
import oracle.goldengate.datasource.DsColumn;
import oracle.goldengate.datasource.DsConfiguration;
import oracle.goldengate.datasource.DsEvent;
import oracle.goldengate.datasource.DsOperation;
import oracle.goldengate.datasource.DsOperation.OpType;
import oracle.goldengate.datasource.DsTransaction;
import oracle.goldengate.datasource.GGDataSource;
import oracle.goldengate.datasource.meta.ColumnMetaData;
import oracle.goldengate.datasource.meta.DsMetaData;
import oracle.goldengate.datasource.meta.TableMetaData;
import oracle.goldengate.datasource.meta.TableName;
import oracle.goldengate.datasource.GGDataSource.Status;

/***
 * 1. 将update拆分成delete, insert
 * 2. 按顺序执行操作，insert时就加入iTable
 * 3。delete时，先检测iTable中是否有相应的记录，若有则删除，再加入dTable
 * 4. dTable去重处理.
 * 
 * 1. 控制信息按天记，统一时按小时分段记
 * 2。PK_TABLE_info.txt记录主键信息.
 * **********************/

public class SplHandler extends AbstractHandler {    
    private final Logger logger = LoggerFactory.getLogger(SplHandler.class);
    //private final Logger logger = oracle.goldengate.datasource.test.DsTestUtils.Log4jLogger.getLogger(SampleHandler.class);
	protected Date m_firstDate = null;
	int batchSize = 10000;
	private int m_curFileSeq = 0;
	private int m_maxFileSeq = 100000;
	private String m_savePath = "dirout";
	private Map<String, Sequence> m_dbBuf; 	//<table: buf>

    @Override
    public void init(DsConfiguration conf, DsMetaData metaData) {
        super.init(conf,  metaData);
        m_dbBuf = new HashMap<String, Sequence>();
    }
    
    public void setDbBuf(Map<String, Sequence> buf){
    	m_dbBuf = buf;
    }
    
    public void setSavePath(String path){
    	m_savePath = path;
    }
    
    @Override
    public Status transactionCommit(DsEvent e, DsTransaction tx) {
        Status superResult = Status.OK; //super.transactionCommit(e, tx);
        SimpleDateFormat df = null;
        String sTime, outFile = "";
        String controlFile = "";
        try{
	        //System.out.println("transactionCommit Map="+m_dbBuf.size());
	        for (String key : m_dbBuf.keySet()) {
	        	df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	        	sTime = df.format(new Date());
	        	outFile = m_savePath + "/" + key.replace(".", "_")+"_"+ sTime+ 
	    				"_"+autoGenericCode(m_curFileSeq, 5)+".btx";
	        	df = new SimpleDateFormat("yyyy-MM-dd");
	        	sTime = df.format(new Date());
	        	controlFile = m_savePath + "/control/" + key.replace(".", "_")+"_"+sTime+ "_data.ctrl";
	        	
	    		WriteSequenceToFile(m_dbBuf.get(key),outFile,controlFile);
	    		m_curFileSeq++;
	    		if (m_curFileSeq>=m_maxFileSeq){
	    			m_curFileSeq = 0;
	    		}
	        }
	        System.out.println("transactionCommit xxxxxxxxxxxxxxxxxxx\n");
        }catch(Exception ex){
        	ex.printStackTrace();
        }
        return superResult;
    }

    @Override
    public Status operationAdded(DsEvent e, DsTransaction tx, DsOperation op) {
        DsMetaData meta = e.getMetaData();
      
        //System.out.println("------------ 111111111111111---------------------------");
        OpType opType = op.getOperationType();
        TableName tableName = op.getTableName();
        String tblName = tableName.getFullName();
	    //System.out.println("type="+opType+";table="+tblName);
        logger.info("operationAdded type="+opType+";table="+tblName);
	    
	    // table metadata
	    Table seq=null;
	    if (m_dbBuf.containsKey(tblName)){
	    	 seq = (Table)m_dbBuf.get(tblName);
	    }else{
	    	TableMetaData tmeta = meta.getTableMetaData(tableName);
	    	seq = doPrimaryKey(tmeta);

	    	m_dbBuf.put(tblName, seq);
//	        String debug = System.getenv("debug");
//	        System.out.println("xxxxx debug.trc = "+debug);
//	        System.out.println("xxxxx log4j.configuration = "+System.getenv("log4j.configuration"));
	    }
	    logger.info("operationAdded seq="+seq);
    	Status status = GGDataSource.Status.OK;
		if (opType.isInsert()) {
			status = insertOpt(e, tx, op, seq);
		} else if (opType.isUpdate()) {
			status = updateOpt(e, tx, op, seq);
		} else if (opType.isDelete()) {
			status = deleteOpt(e, tx, op, seq);
		}

        //System.out.println("----------------2222222-----------------------");
        Status superResult = super.operationAdded(e, tx, op);
        return superResult;
    }

    private Table doPrimaryKey(TableMetaData tmeta){
    	Table seq = null;
    	try{
	    	String[] colv = new String[tmeta.size()+1];
	    	colv[0] = "OP";
	    	List<String> keys = new ArrayList<String>();
			String pkey = "PK=1"; //for save to table.ctrl
			String cols = "#";
			String types = "#";
			String colName;
			//System.out.print("tmeta="+tmeta.size());
			for (int i = 0; i < tmeta.size(); i++) {
				ColumnMetaData cmeta = tmeta.getColumnMetaData(i);
				colName = cmeta.getColumnName();
				
				if (cmeta.isKeyCol()) {
					keys.add(colName);
					pkey += ";"+colName;
					//System.out.println("table key="+cmeta.getColumnName());
				}
				colv[i+1]=colName;
				cols+=";" + colName;
				types+=";" + cmeta.getDataType().toString();
						
				System.out.println("col="+colName+"; type="+cmeta.getDataType().toString());
			}
			
			// 无主键参考oracle字段全pk
			if (tmeta.size()==keys.size()){ //not primary key
				pkey = pkey.replace("PK=1", "PK=0");
			}
			seq=new Table(colv);
	    	seq.setPrimary(keys.toArray(new String[keys.size()]));

	    	String controlPath = m_savePath + "/control";
	    	File f = new File(controlPath);
	    	if (!f.exists()){
	    		f.mkdirs();
	    	}
	    	String tableName = tmeta.getTableName().getFullName();
	    	//logger.info("pk seq="+tableName);
	    	String controlFile = m_savePath + "/control/PK_" + tableName.replace(".", "_")+"_info.txt";
	    	f = new File(controlFile);
	    	if (!f.exists()){
	    		String info = cols+"\r\n"+types+"\r\n"+pkey;
	    		info = info.replaceAll("#;", "");
	    		System.out.println("info = "+info);
	    		WriteStringToFile(controlFile, info);
	    	}	    	
    	}catch(Exception e){
    		System.out.println(e.getMessage());
    	}
    	
    	return seq;
    }
    
    // insert操作
    protected Status insertOpt(DsEvent e, DsTransaction tx, DsOperation op, Table seq) {
 		try {
 			TableMetaData tmeta = e.getMetaData().getTableMetaData(op.getTableName());
 			Object[] objs = new Object[tmeta.size()+1];
 			objs[0] = "I";
 			for (int i = 0; i < tmeta.size(); i++) {
 				//ColumnMetaData cmeta = tmeta.getColumnMetaData(i);
 				DsColumn col = op.getColumn(i);
 				objs[i+1] = (col.isChanged()? col.getAfterValue():"");
 			}
 			seq.newLast(objs);
 			
 			return GGDataSource.Status.OK;
 		} catch (Exception ex) {
 			logger.error("Method insertOpt failed.", ex);
 			return GGDataSource.Status.ABEND;
 		}
 	}

    /*** update操作 *************
     * 则拆分成del, insert两部分.(insert中after值为空的，要用before值)
     * ****/
 	protected Status updateOpt(DsEvent e, DsTransaction tx, DsOperation op, Table seq) {
 		try {
 			TableMetaData tmeta = e.getMetaData().getTableMetaData(op.getTableName());
 			
 			String[] keys = seq.getPrimary();
 			List<String> list = Arrays.asList(keys); 
 			Object[] objs = new Object[tmeta.size()+1];
 			
			// for delete
			objs[0] = "D";
			for (int i = 0; i < tmeta.size(); i++) {
 				ColumnMetaData cmeta = tmeta.getColumnMetaData(i);
 				DsColumn col = op.getColumn(i);
 				if (!col.isMissing()) {
 					if (list.contains(cmeta.getColumnName())){ //检测是否有主键值被修改
 						objs[i+1] =col.getBeforeValue();
 						//System.out.println(cmeta.getColumnName()+" before="+col.getBeforeValue()+" after="+col.getAfterValue());
 					}
 				}
 			}
			seq.newLast(objs);
			
			// for insert
			objs = null;
			objs = new Object[tmeta.size()+1];
			objs[0] = "I";
			for (int i = 0; i < tmeta.size(); i++) {
 				ColumnMetaData cmeta = tmeta.getColumnMetaData(i);
 				DsColumn col = op.getColumn(i);
 				if (!col.isMissing()) {
 					//System.out.println(cmeta.getColumnName()+" before="+col.getBeforeValue()+" after="+col.getAfterValue());
 					objs[i+1] =col.getAfterValue();
 					if (col.getAfterValue()==null || col.getAfterValue().isEmpty()){
 						objs[i+1] =col.getBeforeValue();
 					}
 				}
 			}
			seq.newLast(objs);
		
 			return GGDataSource.Status.OK;
 		} catch (Exception ex) {
 			logger.error("Method updateOpt failed.", ex);
 			return GGDataSource.Status.ABEND;
 		}
 	}

 	// delete操作
 	protected Status deleteOpt(DsEvent e, DsTransaction tx, DsOperation op, Table seq) {
 		try {
 			TableMetaData tmeta = e.getMetaData().getTableMetaData(op.getTableName());
 			Object[] objs = new Object[tmeta.size()+1];
 			objs[0] = "D";
 			for (int i = 0; i < tmeta.size(); i++) {
 				ColumnMetaData cmeta = tmeta.getColumnMetaData(i);
 				if (cmeta.isKeyCol()) {
 					DsColumn col = op.getColumn(i);
 					objs[i+1]=col.getBeforeValue();
 					//System.out.println(cmeta.getColumnName()+"key1=="+col.getBefore());
 				}
 			} 			
 			seq.newLast(objs);
 			return GGDataSource.Status.OK;
 		} catch (Exception ex) {
 			logger.error("Method deleteOpt failed.", ex);
 			return GGDataSource.Status.ABEND;
 		}
 	}

 
    @Override
    public void destroy() {
        super.destroy();
        //WriteStringToFile("D:\\SampleHandler.log", "SampleHandler.destroy(*)"); 
        logger.info("destroy!");
    }
    @Override
    public String reportStatus() {
        String superResult = "OK"; //super.reportStatus(); 调用父类函数,程序就会崩溃。
        //WriteStringToFile("D:\\SampleHandler.log", "SampleHandler.reportStatus(*) => "+superResult);
        return superResult;
        //return "status report...===";
    }  

    //写文件增加锁，合并时可能有冲突.
    public void WriteSequenceToFile(Sequence seq,String filePath, String fileControl) throws Exception {  
    	RandomAccessFile randomAccessFile = null;
        FileChannel channel = null;
        String lockFP = filePath.replace(".btx", ".lck");
        try {  
        	if(seq.length()<1) return;
        	FileWriter writer = new FileWriter(fileControl, true);     
        	System.out.println("savefile :" + filePath); 
        	testPrintTable((Table)seq);  
        	
        	randomAccessFile = new RandomAccessFile(lockFP, "rw");
            channel = randomAccessFile.getChannel();
            FileLock lock = null;

            while (null == lock) {
                try {
                    lock = channel.lock();
                } catch (Exception e) {
                    System.out.println("Write Process : get lock failed");
                }
            }
            
            FileObject fo = new FileObject(filePath); 
        	fo.exportSeries(seq, "b", "|"); 
        	seq.clear();
        	writer.write(filePath+"\r\n");   
        	writer.close(); 
            lock.release();
            
        } catch (Exception e) {     
            //e.printStackTrace();  
        	logger.error("writeToFile: "+e.getMessage()); 
        } finally{
        	if (null != randomAccessFile) {
                randomAccessFile.close();
            }
            if (null != channel) {
                channel.close();
            }
            
            File f = new File(filePath);
            f.deleteOnExit();
        }
    }   
    
    // 保留num的位数
    // 0 代表前面补充0     
    // num 代表长度为4     
    // d 代表参数为正数型 
    private String autoGenericCode(int code, int num) {
        String result = "";        
        result = String.format("%0" + num + "d", code );

        return result;
    }
    
    
    ////////////////////////////// for test //////////////////////////////////////////
    public static void main(String[] args) {
    	//readBtxFile();
    	SplHandler s = new SplHandler();
    	//s.getTimeSegment(15);
    	//s.autoGenericCode(123, 5);
    	//s.readBtxFile();
    	s.sequenceOpt();
    	
    }
    
 // 多个操作批量执行，提升效率
  	protected int[] executeBatch() throws SQLException {
  		try {
  			FileObject fo = new FileObject();
         	Table series = new  Table();
         	fo.exportSeries(series, "b", "ss");
         	
  			int[] result = {10,20};//pstmt.executeBatch();
  			if (logger.isDebugEnabled())
  				logger.debug(new StringBuilder("Batch executed. Op=[").append(" insert ").append("] size=")
  						.append(batchSize).append(" result=").append(Arrays.toString(result)).toString());
  			batchSize = 0;
  			return result;
  		} catch (Exception ex) {
  			System.out.println(ex.getMessage());
  			ex.printStackTrace();
  			return null;
  		}
  	}
  	
    
//  private void getTableMetaData(DsMetaData meta){
//  Set<TableName> tableNames = meta.getTableNames();
//  for(TableName tableName : tableNames){
//      String tableStr = "meta::";
//      tableStr = tableStr + tableName+" : ";
//      TableMetaData metaData = meta.getTableMetaData(tableName);
//      ArrayList<ColumnMetaData> columns = metaData.getColumnMetaData();
//      for(ColumnMetaData column : columns){               
//          tableStr = tableStr + "\r\n   " + column.getColumnName() + " | "+column.getDataType().toString();
//      }
//      
//      System.out.println(tableStr+"\r\n");            
//  }      
//}
    
//    private String[] getTableColumns(DsMetaData meta,TableName tableName){
//        TableMetaData metaData = meta.getTableMetaData(tableName);
//        ArrayList<ColumnMetaData> columns = metaData.getColumnMetaData();
//        String [] cols= new String[columns.size()]; 
//        int i = 0;
//        for(ColumnMetaData column : columns){               
//            //tableStr = tableStr + "\r\n   " + column.getColumnName() + " | "+column.getDataType().toString();
//            cols[i++] = column.getColumnName();
//        }
//      
//        return cols;
//      }
    
    
    public static void WriteStringToFile(String filePath, String text) {   
        try {     
        	//System.out.println("AAAAAAAAAAAAAAAAA :" + text); 
        	//System.out.println("BBBBBBBBBBBBBBBBB :" + filePath); 
            FileWriter writer = new FileWriter(filePath, true);     
            writer.write(text+"\r\n");       
            writer.close();     
        } catch (IOException e) {     
            //e.printStackTrace();  
            System.out.println(e.getMessage()); 
        } 
    }     
    
    public void testPrintTable(Table table) {
		if (table == null)
			return;
		//System.out.println("size = " + table.length());

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

	public void getTimeSegment(int nUnitTime) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String fileName = "d:\\tmp\\ogg\\";
			String outfile = "";
			if (m_firstDate == null) {
				m_firstDate = new Date();
				String t = df.format(m_firstDate);
				System.out.println("cur = " + t);
				if (nUnitTime < 60) {
					int cnt = 60/nUnitTime+(60%nUnitTime==0?0:1);
					t = t.substring(0,t.length()-2)+"00";
					m_firstDate = df.parse(t);
					t = df.format(m_firstDate);
					System.out.println("<60s cur = " + t);
				} else if (nUnitTime == 60) {
					t = t.substring(0,t.length()-4)+"0000";
					m_firstDate = df.parse(t);
					t = df.format(m_firstDate);
					System.out.println("=60s cur = " + t);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("transactionCommit Map="+m_dbBuf.size());
	}
	
    private void sequenceOpt(){
    	try{
	    	SplHandler sh = new SplHandler();
	    	Map<String, Sequence> dbBuf = new HashMap<String, Sequence>();
	    	sh.setDbBuf(dbBuf);
	    	String[] cols = {"opType", "id","name","gender"};
	    	Table seq = new Table(cols);
	    	
	    	Object[] o = new Object[]{"I", "pm101","A",1};
	    	o[3] += "\t"+4;
	    	seq.newLast( o);
	    	seq.newLast(new Object[]{"I", "pm102","B","B1"});
	    	System.out.println(seq.length());
	    	seq.newLast(new Object[]{"U", "pm103","NULL",null});
	    	seq.newLast(new Object[]{"U", "pm104",null,null});
	    	seq.newLast(new Object[]{"D", "pm105","C","B1"});
	    	seq.newLast(new Object[]{"D", "pm106","C","B1"});
	    	dbBuf.put("ogg.tt", seq);
	    	
	    	//testPrintTable(seq);
			for(String key: dbBuf.keySet()){
				WriteSequenceToFile(seq, "d:\\tmp\\ogg\\OGG.TT_20190408_154636.btx","aa.ctrl");
			}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	
    	//sh.transactionCommit(null, null);
	}
    
    public void readBtxFile() {
    	String filePath = "D:\\app\\orcl\\product\\ggs\\dirout\\OGG_TEST_2019-04-10_11-58-05_00014.btx";
    	try {    
            FileObject fo = new FileObject(filePath); 
        	Sequence seq = fo.importSeries("b"); 
        	Table t = (Table)seq;
        	String[] keys = t.getPrimary();
        	testPrintTable((Table)seq);
        } catch (Exception e) {     
            //e.printStackTrace();  
            System.out.println("writeToFile: "+e.getMessage()); 
        } finally{
        	
        }
    }
    
    
}