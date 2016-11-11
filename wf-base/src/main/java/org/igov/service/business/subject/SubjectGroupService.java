/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.igov.model.core.BaseEntityDao;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupNode;
import org.igov.model.subject.SubjectGroupResult;
import org.igov.model.subject.SubjectGroupTree;
import org.igov.util.cache.CachedInvocationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author inna
 */
@Service
public class SubjectGroupService {
	private static final Log LOG = LogFactory.getLog(SubjectGroupService.class);
	private static final long FAKE_ROOT_SUBJECT_ID = 0;
	private static final String GET_SERVICE_SUBJECT_GROUP_CACHE_KEY = "SubjectGroupService.getCatalogTreeSubjectGroups";
	private static Long deepLevelChildSubjectGroup = 0L;

	@Autowired
	private BaseEntityDao<Long> baseEntityDao;

	@Autowired
	private CachedInvocationBean cachedInvocationBean;

	public List<SubjectGroupsVO> getCatalogTreeSubjectGroups(String sID_Group_Activiti, Long deepLevel) {
		List<SubjectGroupsVO> subjectGroupsVOList = new ArrayList<>();

		SubjectGroupResult tree = getSubjectGroupsByGroupActiviti(sID_Group_Activiti,deepLevel);

		
		for(SubjectGroupNode subjectGroupNode:tree.getRootSubjectNodes()) {
			SubjectGroupsVO subjectGroupsVO = new SubjectGroupsVO();
			SubjectGroup subjectGroup = subjectGroupNode.getGroup();
			if(subjectGroup.getsID_Group_Activiti().equals(sID_Group_Activiti)) {
				subjectGroupsVO.setoSubjectGroup_Root(subjectGroup);
				for(SubjectGroupNode childrens:subjectGroupNode.getChildren()) {
					SubjectGroup subjectGroupChildren = childrens.getGroup();
					subjectGroupsVO.addChild(subjectGroupChildren);
				}
				subjectGroupsVOList.add(subjectGroupsVO);
			}else {
				for(SubjectGroupNode childrensRoot:subjectGroupNode.getChildren()) {
					if(childrensRoot.getGroup().getsID_Group_Activiti().equals(sID_Group_Activiti)) {
						subjectGroupsVO.setoSubjectGroup_Root(childrensRoot.getGroup());
						for(SubjectGroupNode childrens:subjectGroupNode.getChildren()) {
							if(SubjectGroupService.getDeepLevelChildSubjectGroup().compareTo(deepLevel)==0) {
								break;
							}
							SubjectGroup subjectGroupChildren = childrens.getGroup();
							if(!childrensRoot.getGroup().getsID_Group_Activiti().equals(sID_Group_Activiti)) {
							subjectGroupsVO.addChild(subjectGroupChildren);
							SubjectGroupService.setDeepLevelChildSubjectGroup(deepLevelChildSubjectGroup+1);
							}
						}
						subjectGroupsVOList.add(subjectGroupsVO);
					}
						break;
				}
			}
			
			
		}

		return subjectGroupsVOList;
	}

	public SubjectGroupResult getSubjectGroupsByGroupActiviti(String sID_Group_Activiti, Long deepLevel) {
		List<SubjectGroupTree> subjectGroupRelations = new ArrayList<>(baseEntityDao.findAll(SubjectGroupTree.class));
		Map<SubjectGroup, SubjectGroupNode> subjectToNodeMap = new HashMap<>();

		/*
		 * List<ParentSubjectGroup> parentSubjectGroups = new ArrayList<>();
		 * 
		 * for(SubjectGroupTree subjectGroupRelation : subjectGroupRelations) {
		 * final SubjectGroup parent =
		 * subjectGroupRelation.getoSubjectGroup_Parent(); if (parent.getId() !=
		 * FAKE_ROOT_SUBJECT_ID ) { ParentSubjectGroup parentSubjectGroup = new
		 * ParentSubjectGroup(parent);
		 * 
		 * final SubjectGroup child =
		 * subjectGroupRelation.getoSubjectGroup_Child(); ChildSubjectGroup
		 * childSubjectGroup = new ChildSubjectGroup(child,deepLevel);
		 * parentSubjectGroup.addChildSubjectGroup(childSubjectGroup);
		 * parentSubjectGroups.add(parentSubjectGroup);
		 * 
		 * } }
		 */

		Set<SubjectGroup> parentSubject = new LinkedHashSet<>();
		Set<SubjectGroup> childSubject = new HashSet<>();

		SubjectGroupNode parentNode = null;
		for (SubjectGroupTree subjectGroupRelation : subjectGroupRelations) {
			final SubjectGroup parent = subjectGroupRelation.getoSubjectGroup_Parent();
			LOG.info("SubjectGrouppppppparent " + parent);
			final SubjectGroup child = subjectGroupRelation.getoSubjectGroup_Child();
			LOG.info("SubjectGrouppppppchild " + child);

			if (parent.getId() != FAKE_ROOT_SUBJECT_ID) {
				parentNode = subjectToNodeMap.get(parent);
				if (parentNode == null) {
					parentSubject.add(parent);
					parentNode = new SubjectGroupNode(parent);
					subjectToNodeMap.put(parent, parentNode);
				}
			}

			SubjectGroupNode childNode = subjectToNodeMap.get(child);
			if (childNode == null) {
				childSubject.add(child);
				childNode = new SubjectGroupNode(child);
				subjectToNodeMap.put(child, childNode);
			}

			if (parentNode != null) {
				parentNode.addChild(childNode);
			}

		}

		Set<SubjectGroup> rootTags = new LinkedHashSet<>(parentSubject);

		LOG.info("parentSubjecttttttttttttttt " + parentSubject);
		LOG.info("childSubjectttttttttttttttttt " + childSubject);

		LOG.info("SubjectGrouppppppSettttt" + rootTags);
	//	rootTags.removeAll(childSubject);

		final List<SubjectGroupNode> rootSubjectNodes = rootTags.stream().map(subjectToNodeMap::get)
				.collect(Collectors.toList());
		/*
		 * SubjectGroupResult subjectGroupResult = new
		 * SubjectGroupResult(sID_Group_Activiti); for(ParentSubjectGroup
		 * parentSubjectGroup:parentSubjectGroups) {
		 * subjectGroupResult.addParentSubjectGroup(parentSubjectGroup); }
		 * 
		 * 
		 * SubjectGroupTreeResult subjectGroupTreeResult = new
		 * SubjectGroupTreeResult();
		 * subjectGroupResult.accept(subjectGroupTreeResult); return
		 * subjectGroupResult;
		 */
		
		SubjectGroupResult subjectGroupResult=new SubjectGroupResult(rootSubjectNodes);
		
		LOG.info("subjectGroupResultttttttttttttttt " + subjectGroupResult);
		return subjectGroupResult;

	}
	
	public static Long getDeepLevelChildSubjectGroup() {
		return deepLevelChildSubjectGroup;
	}

	public static void setDeepLevelChildSubjectGroup(Long deepLevelChildSubjectGroup) {
		SubjectGroupService.deepLevelChildSubjectGroup = deepLevelChildSubjectGroup;
	}

	/**
	 * Кэш для SubjectGroupResult
	 * @param sID_Group_Activiti
	 * @param nDeepLevel
	 * @return
	 */
	 public SubjectGroupResult getSubjectGroupResultCached(String sID_Group_Activiti,Long deepLevel) {
		return cachedInvocationBean.invokeUsingCache(
				new CachedInvocationBean.Callback<SubjectGroupResult>(GET_SERVICE_SUBJECT_GROUP_CACHE_KEY, sID_Group_Activiti) {
					@Override
					public SubjectGroupResult execute() {
						return getSubjectGroupsByGroupActiviti(sID_Group_Activiti,deepLevel);
					}
				});
	}

}
