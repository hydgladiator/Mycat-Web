package org.mycat.web.service.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.curator.utils.ZKPaths;
import org.hx.rainbow.common.context.RainbowContext;
import org.hx.rainbow.common.util.JavaBeanUtil;
import org.mycat.web.model.cluster.DataHost;
import org.mycat.web.model.cluster.Schema;
import org.mycat.web.model.cluster.Table;
import org.mycat.web.util.Constant;
import org.mycat.web.util.ZookeeperCuratorHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

@Lazy
@Service("tableService")
public class TableService {
	private ZookeeperCuratorHandler zkHander=  ZookeeperCuratorHandler.getInstance();
	
	@SuppressWarnings("unchecked")
	public RainbowContext queryByPage(RainbowContext context){
		String clusterPath = (String)context.getAttr("zkId");
		String guid =(String)context.getAttr("guid");
		String parentPath = ZKPaths.makePath(Constant.MYCAT_CLUSTER_KEY, clusterPath, Constant.CLUSTER_SCHEMA,guid);
		int pageNo = context.getPage() == 0 ?1:context.getPage();
	    int pageSize = context.getLimit();
		Map<String,Object> data = zkHander.getChildNodeData(parentPath, Table.class, pageNo, pageSize,null);
		if(data != null && data.size() > 0){
			context.setRows((List<Map<String, Object>>) data.get("rows"));
			context.setTotal((int) data.get("total"));
		}
	    return context;
	}
	
	@SuppressWarnings("unchecked")
	public RainbowContext queryAll(RainbowContext context){
		String clusterPath=(String)context.getAttr("zkId");
		String parentPath = ZKPaths.makePath(Constant.MYCAT_CLUSTER_KEY, clusterPath, Constant.CLUSTER_SCHEMA);
		Map<String,Object> data = zkHander.getChildNodeData(parentPath, Table.class);
		if(data != null && data.size() > 0){
			context.setRows((List<Map<String, Object>>) data.get("rows"));
		}
	    return context;
	}
	
	
	public RainbowContext insert (RainbowContext context) throws Exception{
		Map<String,Object> params = context.getAttr();
		String clusterPath = (String)context.getAttr("zkid");
		String guid = (String) context.getAttr("guid");
		String parentPath = ZKPaths.makePath(Constant.MYCAT_CLUSTER_KEY, clusterPath, Constant.CLUSTER_SCHEMA,guid);
		String datahostPath = ZKPaths.makePath(parentPath , guid);
		String mycatJson_new = JSON.toJSONString(params);
		Schema schema  = JSON.parseObject(mycatJson_new, Schema.class);
		
		List<String> childrenPath =  zkHander.getChildNode(parentPath);
		for (String cpath : childrenPath) {
			if(schema.getName().equals(cpath)){
				context.setSuccess(false);
				context.setMsg("名称已存在");
				return context;
			}
		}
		String data = JSON.toJSONString(schema);
		zkHander.createNode(datahostPath, data);
		return context;
	}
	
	public RainbowContext update(RainbowContext context) throws Exception{
		Map<String,Object> params = context.getAttr();
		if(params.containsKey("guid")){
			String json_newStr = JSON.toJSONString(params);
			String path =  String.valueOf(params.get("zkId"));
			DataHost _new = JSON.parseObject(json_newStr, DataHost.class);
			String  json_oldStr = zkHander.getNodeData(path);
			DataHost _old = JSON.parseObject(json_oldStr, DataHost.class);
			JavaBeanUtil.copyProperties(_new, _old);
			String data = JSON.toJSONString(_old);
			zkHander.setNodeData(path, data);
		}
		return context;
	}

}