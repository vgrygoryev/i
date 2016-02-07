package org.igov.model.action.task.core.entity;

import org.igov.model.action.task.core.entity.ActionTaskLink;
import org.igov.model.core.EntityDao;

/**
 * @author NickVeremeichyk
 * @since 2016-02-07.
 */
public interface ActionTaskLinkDao extends EntityDao<ActionTaskLink> {

    ActionTaskLink setSubjectOrganJoinTax(Long nIdProcess, String sKey, Long nIdSubjectHolder);

    void removeByKey(String sKey, Long nIdSubjectHolder);

    ActionTaskLink getByKey(Long nIdProcess, String sKey, Long nIdSubjectHolder);
}
