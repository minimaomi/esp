package com.raqsoft.lib.hdfs.function;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Function;
import com.raqsoft.expression.Node;
import com.raqsoft.resources.EngineMessage;

// hdfs_close(hdfs_client)
public class HdfsClose extends Function {
	public Node optimize(Context ctx) {
		return this;
	}

	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("hdfs_close" + mm.getMessage("function.missingParam"));
		}

		Object client = param.getLeafExpression().calculate(ctx);
		if (!(client instanceof HdfsClient)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("hdfs_close" + mm.getMessage("function.paramTypeError"));
		}
		
		try {
			FileSystem fs = ((HdfsClient)client).getFileSystem();
			fs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
