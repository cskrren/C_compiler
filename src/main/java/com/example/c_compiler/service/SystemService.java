package com.example.c_compiler.service;

import com.example.c_compiler.domain.entity.SystemTree;
import com.example.c_compiler.domain.entity.SystemNode;
import com.example.c_compiler.domain.mapper.SystemTreeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemService {

    @Autowired
    SystemTreeMapper systemTreeMapper;
    Map<Integer, List<Integer>> childMap;
    Map<Integer, SystemNode> nodeMap;

    private void dfs(int id) {
        if(!childMap.containsKey(id)) {
            return;
        }
        for(Integer i:childMap.get(id)) {
            dfs(i);
            nodeMap.get(id).getChildren().add(nodeMap.get(i));
        }
    }
    public List<SystemNode> list() {
        List<SystemTree> nodes= systemTreeMapper.selectList(null);
        childMap = new HashMap<>();
        nodeMap = new HashMap<>();
        SystemNode root = new SystemNode(0, "root", "root", new ArrayList<>(), "");
        nodeMap.put(0, root);
        // 第一次遍历，创建节点并保存到Map中
        for (SystemTree node : nodes) {
            Integer id = node.getId();
            String label = node.getLabel();
            Integer fatherId = node.getFatherId();
            String type = node.getType();
            String fileContent = node.getFileContent();

            SystemNode tmp = new SystemNode(id, label, type, new ArrayList<>(), fileContent);
            nodeMap.put(id, tmp);
            if (!childMap.containsKey(fatherId)) {
                childMap.put(fatherId, new ArrayList<>());
            }
            childMap.get(fatherId).add(id);
        }
        dfs(0);
        System.out.println(nodeMap.get(0).getChildren());
        return nodeMap.get(0).getChildren();
    }

    public Integer nodemaxid() {
        List<SystemTree> nodes= systemTreeMapper.selectList(null);
        Integer maxid = 0;
        for (SystemTree node : nodes) {
            Integer id = node.getId();
            if(id > maxid) {
                maxid = id;
            }
        }
        return maxid;
    }

    public void insert(SystemTree newnode) {
        System.out.println(newnode);
        if(systemTreeMapper.selectById(newnode.getId()) != null){
            systemTreeMapper.updateNodeById(newnode.getId(), newnode.getLabel(), newnode.getFatherId(), newnode.getType(), newnode.getFileContent());
            return;
        }
        systemTreeMapper.insertNode(newnode.getId(), newnode.getLabel(), newnode.getFatherId(), newnode.getType(), newnode.getFileContent());
    }

    public void delete(Integer id) {
        systemTreeMapper.deleteNodeById(id);
    }

    public void rename(Integer id, String label) {
        systemTreeMapper.updateLabelById(id, label);
    }

    public void updateFileContent(Integer id, String fileContent) {
        systemTreeMapper.updateFileContentById(id, fileContent);
    }

    public SystemTree getNodeById(Integer id) {
        return systemTreeMapper.selectNodeById(id);
    }
}
