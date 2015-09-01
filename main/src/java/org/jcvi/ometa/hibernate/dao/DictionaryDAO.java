package org.jcvi.ometa.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.jcvi.ometa.model.Dictionary;
import org.jcvi.ometa.model.DictionaryDependency;

import java.util.Date;
import java.util.List;

/**
 * Created by mkuscuog on 6/23/2015.
 */
public class DictionaryDAO extends HibernateDAO {

    public List<Dictionary> getDictionaries(Session session, boolean includeInactive) throws DAOException {
        List<Dictionary> rtnVal = null;

        try {
            Criteria crit = session.createCriteria( Dictionary.class );
            if(!includeInactive) crit.add( Restrictions.eq("isActive", 1) );
            rtnVal = crit.list();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public List<DictionaryDependency> getDictionaryDependencies(Session session) throws DAOException {
        List<DictionaryDependency> rtnVal = null;

        try {
            Criteria crit = session.createCriteria( DictionaryDependency.class );
            rtnVal = crit.list();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public List<Dictionary> getDictionaryByType( String dictType, Session session ) throws DAOException {
        List<Dictionary> rtnVal = null;

        try {
            Criteria crit = session.createCriteria( Dictionary.class );
            crit.add( Restrictions.eq("isActive", 1) );
            crit.add( Restrictions.eq("dictionaryType", dictType) );
            rtnVal = crit.list();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public Dictionary getDictionaryByTypeAndCode(String dictType, String dictCode, Session session) throws DAOException {
        Dictionary rtnVal = null;

        try {
            Criteria crit = session.createCriteria( Dictionary.class );
            crit.add( Restrictions.eq("isActive", 1) );
            crit.add( Restrictions.eq("dictionaryType", dictType) );
            crit.add( Restrictions.eq("dictionaryCode", dictCode) );
            rtnVal = (Dictionary) crit.uniqueResult();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public List<Dictionary> getDictionaryDependenciesByType( String dictType, String dictCode, Session session ) throws DAOException {
        List<Dictionary> rtnVal = null;

        try {
            String sql = " select distinct * from ifx_projects.dictionary d " +
                    "where d.dict_id in (" +
                    "select dd.dict_id from ifx_projects.dictionary_dependency dd " +
                    "left join ifx_projects.dictionary d on dd.parent_id = d.dict_id " +
                    "where d.dict_type = :dictType and d.dict_code = :dictCode) and d.dict_is_active = 1";

            SQLQuery query = session.createSQLQuery(sql);
            query.addEntity(Dictionary.class);
            query.setParameter("dictType", dictType);
            query.setParameter("dictCode", dictCode);

            rtnVal = query.list();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public List<Object[]> getAllDictionaryTypeCodePairs(Session session) throws DAOException {
        List<Object[]> rtnVal = null;

        try {
            ProjectionList projList = Projections.projectionList();
            projList.add(Projections.groupProperty("dictionaryType"));
            projList.add(Projections.groupProperty("dictionaryCode"));

            Criteria crit = session.createCriteria( Dictionary.class );
            crit.setProjection(projList);
            crit.add(Restrictions.eq("isActive", 1));
            rtnVal = (List<Object[]>) crit.list();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public void loadDictionary(String dictType, String dictValue, String dictCode, Date creationDate, Session session) throws DAOException {
        try {
            Dictionary dictionary = new Dictionary();
            dictionary.setDictionaryCode(dictCode);
            dictionary.setDictionaryValue(dictValue);
            dictionary.setDictionaryType(dictType);
            dictionary.setIsActive(1);
            dictionary.setCreationDate(creationDate);

            session.save(dictionary);
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
    }

    public void loadDictionaryWithDependency(String dictType, String dictValue, String dictCode,
                                             String parentDictTypeCode, Date creationDate, Session session) throws DAOException {
        try {
            Dictionary dictionary = new Dictionary();
            dictionary.setDictionaryCode(dictCode);
            dictionary.setDictionaryValue(dictValue);
            dictionary.setDictionaryType(dictType);
            dictionary.setIsActive(1);
            dictionary.setCreationDate(creationDate);

            session.save(dictionary);

            String[] dictTypeCode = parentDictTypeCode.split(" - ");

            Criteria crit = session.createCriteria(Dictionary.class);
            crit.add(Restrictions.eq("dictionaryType", dictTypeCode[0]));
            crit.add(Restrictions.eq("dictionaryCode", dictTypeCode[1]));
            crit.setProjection(Projections.property("dictionaryId"));

            Long parentDictId = (Long) crit.uniqueResult();

            DictionaryDependency dictDependency = new DictionaryDependency();
            dictDependency.setParentId(parentDictId);
            dictDependency.setDictionaryId(dictionary.getDictionaryId());
            dictDependency.setCreatedDate(creationDate);

            session.save(dictDependency);
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
    }

    public void updateDictionary(Session session, Long dictionaryId, boolean active) throws Exception{
        try {
            Query query = session.createQuery("update Dictionary set isActive = :active" +
                    " where dictionaryId = :dictionaryId");
            query.setParameter("dictionaryId", dictionaryId);
            if(active) query.setParameter("active", 1);
            else query.setParameter("active", 0);
            query.executeUpdate();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

    }
}
