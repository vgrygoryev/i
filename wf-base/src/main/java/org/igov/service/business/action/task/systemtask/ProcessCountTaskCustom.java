package org.igov.service.business.action.task.systemtask;

import java.util.List;

import org.activiti.engine.FormService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.FormPropertyImpl;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.igov.io.GeneralConfig;
import org.igov.io.web.HttpRequester;
import static org.igov.service.business.action.task.core.AbstractModelTask.getStringFromFieldExpression;
import static org.igov.service.business.action.task.systemtask.ProcessCountTask.S_ID_ORDER_GOV_PUBLIC;
import org.igov.service.controller.interceptor.ActionProcessCountUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author askosyr
 *
 */
@Component("ProcessCountTaskCustomListener")
public class ProcessCountTaskCustom implements JavaDelegate, TaskListener {

	private static final String sID_Field_Result = "sID_Custom_GovPublic";

    public Expression sPattern;
    public Expression sKey;
        
	private final static Logger LOG = LoggerFactory.getLogger(ProcessCountTaskCustom.class);
	
	@Autowired
    HttpRequester httpRequester;
	
	@Autowired
    GeneralConfig generalConfig;
	
	@Autowired
    RuntimeService runtimeService;
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	FormService formService;
	
	@Override
	public void execute(DelegateExecution oDelegateExecution) throws Exception {
		loadProcessCountCustom(oDelegateExecution);
	}

	private void loadProcessCountCustom(DelegateExecution oExecution) {
            
                String sParamPattern = getStringFromFieldExpression(sPattern, oExecution);
                String sParamKey = getStringFromFieldExpression(sKey, oExecution);
            
//		String snCount = getActionProcessCount(execution.getProcessDefinitionId(), null);
		//int res = ActionProcessCountUtils.callGetActionProcessCount(httpRequester, generalConfig, StringUtils.substringBefore(sID_BP, ":"), nID_Service, null);
		int nCount = ActionProcessCountUtils.callGetActionProcessCount(httpRequester, generalConfig, sParamKey, null, null);
		String snCount = String.format("%07d", nCount);
		LOG.info("Retrieved snCount={}", snCount);
		
		if (snCount != null) {
                    
                    String sReturn=sParamPattern; //snCount
                    //${sID_Order_GovPublic}/${sID_Custom_GovPublic}
                    
                    if(sReturn.contains("[sID_Order_GovPublic]")){
                        String s=runtimeService.getVariable(oExecution.getProcessInstanceId(), S_ID_ORDER_GOV_PUBLIC, String.class);
                        if(s==null){
                            s="";
                        }
                        sReturn=sReturn.replace("[sID_Order_GovPublic]", s);
                    }
                    sReturn=sReturn.replace("[sID_Custom_GovPublic]", snCount);
                    
                    runtimeService.setVariable(oExecution.getProcessInstanceId(), sID_Field_Result, sReturn);
                    LOG.info("Set variable to runtime sReturn={}", sReturn);
                    
                    LOG.info("Looking for a new task to set form properties");
                    List<Task> aTask = taskService.createTaskQuery().processInstanceId(oExecution.getId()).active().list();
                    LOG.info("Get {} active tasks for the process", aTask);
                    for (Task oTask : aTask) {
                        TaskFormData oTaskFormData = formService.getTaskFormData(oTask.getId());
                        for (FormProperty oFormProperty : oTaskFormData.getFormProperties()) {
                            if (oFormProperty.getId().equals(sID_Field_Result)) {
                                LOG.info("Found form property with the id " + sID_Field_Result + ". Setting sReturn={}", sReturn);
                                if (oFormProperty instanceof FormPropertyImpl) {
                                    ((FormPropertyImpl) oFormProperty).setValue(sReturn);
                                }
                            }
                        }
                        StartFormData oStartFormData = formService.getStartFormData(oExecution.getProcessDefinitionId());
                        for (FormProperty oFormProperty : oStartFormData.getFormProperties()) {
                            if (oFormProperty.getId().equals(sID_Field_Result)) {
                                LOG.info("Found start form property with the id " + sID_Field_Result + ". Setting sReturn={}", sReturn);
                                if (oFormProperty instanceof FormPropertyImpl) {
                                    ((FormPropertyImpl) oFormProperty).setValue(sReturn);
                                }
                            }
                        }
                    }
                    
                    
                    
                }
	}

	@Override
	public void notify(DelegateTask oDelegateTask) {
		loadProcessCountCustom(oDelegateTask.getExecution());
	}
}
