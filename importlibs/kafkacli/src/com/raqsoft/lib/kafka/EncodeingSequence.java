package com.raqsoft.lib.kafka;
import java.io.IOException;
import java.util.Map;  
import org.apache.kafka.common.serialization.Serializer;

import com.raqsoft.common.Logger;
import com.raqsoft.dm.Sequence;  

public class EncodeingSequence implements Serializer<Object> {  
    @Override  
    public void configure(Map<String, ?> configs, boolean isKey) {  
          
    }  
    @Override  
    public byte[] serialize(String topic, Object data) {  
			try {
			if (data instanceof Sequence){
	    		Sequence seq = (Sequence)data;
	    		return seq.serialize();
			}
		} catch (IOException e) {
			Logger.error(e.getStackTrace());
		}
    	
        return null;  
    }  

    @Override  
    public void close() {  
        //System.out.println("EncodeingKafka is close");  
    }  
}  