package com.raqsoft.lib.hive.function;

import java.util.regex.Matcher;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.Driver;
import org.apache.hadoop.hive.ql.QueryState;
import org.apache.hadoop.hive.ql.session.SessionState;

import com.raqsoft.common.Logger;
import com.raqsoft.dm.Context;
import com.raqsoft.dm.IResource;
import com.raqsoft.dm.cursor.ICursor;

public class HiveDriverCli implements IResource{
	public HiveBase hiveBase = null; 

	public HiveDriverCli(Context ctx, String hdfsUrl, String thriftUrl, String dbName, String hdfsName, String opt) {
		init(hdfsUrl, thriftUrl, dbName, hdfsName, opt);
		ctx.addResource(this);		
	}

	// �ر������ͷ���Դ
	public void close() {
		hiveBase.driver.close();
	}
	// ��ʼ��Hive
	private void init(String hdfsUrl, String thriftUrl, String dbName, String hdfsName, String opt)  {
		try {
			Matcher m[] = new Matcher[1];
			if (!Utils.isMatch(hdfsUrl, "hdfs:\\/\\/(.*?):(\\d+)[.*]?", m)){
				System.out.println("url:"+hdfsUrl + " is error expression");
				Logger.debug("url:"+hdfsUrl + " is error expression");
				return;
			}
			
			String masterName = "master";
			if ( 2==m[0].groupCount()){
				masterName = m[0].group(1);
			}
			String sUrl = "hdfs://" + masterName + ":"+m[0].group(2);
			
			if (!Utils.isMatch(thriftUrl, "thrift:\\/\\/(.*?):(\\d+)", m)){
				Logger.debug("url:"+thriftUrl + " is error expression");
				return;
			}
			
			//if (1==1)
			{
				String envPath = System.getProperty("java.library.path"); 
				String path = System.getProperty("user.dir");
				
				System.setProperty("HADOOP_USER_NAME", hdfsName);
			    System.setProperty("HADOOP_USER_GROUP", "supergroup");
			    
			    System.setProperty("user.name", "root");
			    System.setProperty("USERNAME","root");
				envPath = path + ";" + envPath;
				path = path.replace("\\bin", ""); 
				
				System.setProperty("hadoop.home.dir", path);		
				
				System.setProperty("java.library.path",envPath);
			}
			//System.out.println("debug 1000");
			HiveConf conf = new HiveConf(HiveDriverCli.class);

			if (opt!=null){
				if (opt.equals("p")){
					conf.setBoolean("hive.query.results.cache.enabled", false); //����ʱ��ѯpart�������쳣
				}else if(opt.equals("a")){					
					conf.setBoolean("hive.support.concurrency", true);
					conf.setBoolean("hive.enforce.bucketing", true);
					conf.set("hive.exec.dynamic.partition.mode", "nonstrict");
					conf.set("hive.txn.manager", "org.apache.hadoop.hive.ql.lockmgr.DbTxnManager");
					//conf.setBoolean("hive.compactor.initiator.on", true);
					//conf.setInt("hive.compactor.worker.threads", 1);
				}
			}
			conf.set("fs.default.name", sUrl);
			conf.set("hive.metastore.local", "false");
			conf.set("hive.metastore.token.signature", "hive");
			conf.set("hive.metastore.uris", thriftUrl);
			conf.set("hive.cbo.enable", "false");
			conf.set("hive.cbo.returnpath.hiveop", "false");
			conf.set("yarn.nodemanager.hostname", masterName);
			conf.set("yarn.resourcemanager.hostname", masterName);
			conf.set("hive.hbase.wal.enabled", "false");			
			conf.set("hive.query.lifetime.hooks", "");
			conf.set("hive.log4j.file", "");
			conf.set("hive.exec.log4j.file", "");
			conf.set("hive.async.log.enabled", "false");
			conf.set("hive.execution.engine", "mr");
			conf.set("fs.hdfs.impl",org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
			//conf.set("hive.in.test", "true");
			
//			conf.set("hive.explain.user", "false");
//			conf.set("hive.warehouse.subdir.inherit.perms", "false");
//			
			//String wareHouse = "/user/hive/warehouse";
			//conf.set("hive.metastore.warehouse.dir", wareHouse);
			//conf.set(HiveConf.ConfVars.METASTOREWAREHOUSE.varname, wareHouse);
//			conf.setVar(HiveConf.ConfVars.HIVEINPUTFORMAT, HiveInputFormat.class.getName());
		   
			//System.out.println("debug 1002");
			SessionState ss = SessionState.start(conf);
			//ss.applyAuthorizationPolicy();
			//System.out.println("debug 1003");
			Driver driver = new Driver(new QueryState.Builder().withHiveConf(conf).nonIsolated().build(), null);
			driver.setMaxRows(ICursor.INITSIZE);
			//System.out.println("debug 1004");
			// set dbName
			String cmd = "use "+dbName;
			//driver.run(cmd);

			//System.out.println("OK NewDriver = " + driver);
			hiveBase = new HiveBase(driver);

			//����hiveMetaStore����ĵ�ַ
	        //this.hiveMetaStoreClient = new HiveMetaStoreClient(conf);
	        //��ǰ�汾2.3.4�뼯Ⱥ3.0�汾�����ݣ����������
	        //this.hiveMetaStoreClient.setMetaConf("hive.metastore.client.capability.check","false");
			
//			// �����ݿ�����ƻ�ȡ���ݿ�Ķ���(һЩ������Ϣ)
//			Database database= this.hiveMetaStoreClient.getDatabase(dbName);
//			// �������ݿ����ƻ�ȡ���еı���
//			List<String> tablesList = this.hiveMetaStoreClient.getAllTables(dbName);
//			// �ɱ��������ݿ����ƻ�ȡtable����(�ܻ�ȡ�С�����Ϣ)
//			Table table= this.hiveMetaStoreClient.getTable(dbName,"emp");
//			// ��ȡ���е��ж���
//			List<FieldSchema> fieldSchemaList= table.getSd().getCols();
//			// �رյ�ǰ����
//			this.hiveMetaStoreClient.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}