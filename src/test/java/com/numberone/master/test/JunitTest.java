package com.numberone.master.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.druid.util.StringUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring-context*.xml"})
public class JunitTest {
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private ProcessEngine processEngine;
	@Test
	public void test1(){
		//查出流程定义，转型至扩展类ProcessDefinitionEntity以便拥有获取定义节点的方法
		ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition("test_audit:4:852d5c402d2c4e1aac6991bdc89279a6");
        //获取定义的节点集合
		List<ActivityImpl> activities = definitionEntity.getActivities();
        for (ActivityImpl activityImpl : activities) {
        		//获取节点的传出线
				List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();
				 for (PvmTransition pvmTransition : pvmTransitions) {
					 //传出线的条件
					 System.out.println(pvmTransition.getProperty("conditionText"));//条件属性获取Key"conditionText"
				}
		}
	}
	@Test
	public void test3(){
		List<String> elSring = elSring("cbf8aa7ad2534975be37c8419fe40a96");
		System.out.println(elSring);
	}
	
   public List<String> elSring(String taskID){
	   List<String> estr = new ArrayList<String>();;
	   	//taskService
		Task task = taskService.createTaskQuery().taskId(taskID).singleResult();
		if(task!=null){
			//根据task获取到流程实例的id
			String processInstanceId = task.getProcessInstanceId();
			//根据流程实例的ID获取运行器
			ExecutionEntity execution = (ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
			//跟运行器或 正在执行的节点ID
	        String activitiId = execution.getActivityId(); 
	        //根据task获取流程定义的id
	        String processDefinitionId = task.getProcessDefinitionId();
	        //查出流程定义，转型至扩展类ProcessDefinitionEntity以便拥有获取定义节点的方法
	      	ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinitionId);
	      	//查出所有的节点
	        List<ActivityImpl> activitiList = definitionEntity.getActivities();
	        for (ActivityImpl activityImpl : activitiList) {
	        	if("userTask".equals(activityImpl.getProperty("type")) && activitiId.equals(activityImpl.getId())){
	        		// 获取节点所有流向线路信息
	                List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
	                for (PvmTransition pvmTransition : outTransitions) {
	                	ActivityImpl destination = (ActivityImpl)pvmTransition.getDestination();
						 //传出线的连接任务
	                	if("userTask".equals(destination.getProperty("type"))){
	                		break;
	                	}else{//传出线连接网关
	                		List<PvmTransition> outgoingTransitions = destination.getOutgoingTransitions();
	                		for (PvmTransition pvmTransition2 : outgoingTransitions) {
	                			//传出线的条件的条件
	       					 	String str = (String)pvmTransition2.getProperty("conditionText");
	       					 	estr.add(str);
							}
	                	}
					}
	        	}
	        }
		}
		return estr;
   }
	
	private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinitionEntity,List<HistoricActivityInstance> historicActivityInstances) {
        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
            ActivityImpl activityImpl = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i)
                            .getActivityId());// 得到节点定义的详细信息
            List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
            ActivityImpl sameActivityImpl1 = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i + 1)
                            .getActivityId());
            // 将后面第一个节点放在时间相同节点的集合里
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances
                        .get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances
                        .get(j + 1);// 后续第二个节点
                if (activityImpl1.getStartTime().equals(
                        activityImpl2.getStartTime())) {
                    // 如果第一个节点和第二个节点开始时间相同保存
                    ActivityImpl sameActivityImpl2 = processDefinitionEntity
                            .findActivity(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {
                    // 有不相同跳出循环
                    break;
                }
            }
            List<PvmTransition> pvmTransitions = activityImpl
                    .getOutgoingTransitions();// 取出节点的所有出去的线
            for (PvmTransition pvmTransition : pvmTransitions) {
                // 对所有的线进行遍历
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition
                        .getDestination();
                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }
	
	
	
	
	
	
	
	
	
	
	
	@Test
	public void test2(){
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CC");
		System.out.println(processInstance.getId());
		List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("AA").list();	
		for (Task task : tasks) {
			System.out.println(task.getProcessInstanceId()+"==");
			System.out.println(task.getId());
		}
	}
	@Value("${web.maxUploadSize}")  
    private String oid_partner; 
	
	@Test
	public void test4(){
		System.out.println(oid_partner);
	}
}
