package org.jcvi.ometa.hibernate.dao;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.query.NativeQuery;
import org.jcvi.ometa.model.Dictionary;
import org.jcvi.ometa.model.DictionaryDependency;
import org.jcvi.ometa.utils.GuidGetter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

/**
 * Created by mkuscuog on 6/23/2015.
 */
public class DictionaryDAO extends HibernateDAO {

    public List<Dictionary> getDictionaries(Session session, boolean includeInactive) throws DAOException {
        List<Dictionary> rtnVal;

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Dictionary> criteriaQuery = builder.createQuery(Dictionary.class);
            Root<Dictionary> dictionaryRoot = criteriaQuery.from(Dictionary.class);

            criteriaQuery.select(dictionaryRoot);
            if(!includeInactive)
                criteriaQuery.where(builder.equal(dictionaryRoot.get("isActive"), 1));

            rtnVal = session.createQuery(criteriaQuery).getResultList();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public List<DictionaryDependency> getDictionaryDependencies(Session session) throws DAOException {
        List<DictionaryDependency> rtnVal;

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<DictionaryDependency> criteriaQuery = builder.createQuery(DictionaryDependency.class);
            Root<DictionaryDependency> dictionaryDependencyRoot = criteriaQuery.from(DictionaryDependency.class);

            criteriaQuery.select(dictionaryDependencyRoot);

            rtnVal = session.createQuery(criteriaQuery).getResultList();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public List<Dictionary> getDictionaryByType( String dictType, Session session ) throws DAOException {
        List<Dictionary> rtnVal;

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Dictionary> criteriaQuery = builder.createQuery(Dictionary.class);
            Root<Dictionary> dictionaryRoot = criteriaQuery.from(Dictionary.class);

            criteriaQuery.select(dictionaryRoot)
                    .where(builder.and(
                            builder.equal(dictionaryRoot.get("isActive"), 1),
                            builder.equal(dictionaryRoot.get("dictionaryType"), dictType)
                    ));

            rtnVal = session.createQuery(criteriaQuery).getResultList();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public Dictionary getDictionaryByTypeAndCode(String dictType, String dictCode, Session session) throws DAOException {
        Dictionary rtnVal;

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Dictionary> criteriaQuery = builder.createQuery(Dictionary.class);
            Root<Dictionary> dictionaryRoot = criteriaQuery.from(Dictionary.class);

            criteriaQuery.select(dictionaryRoot)
                    .where(builder.and(
                            builder.equal(dictionaryRoot.get("isActive"), 1),
                            builder.equal(dictionaryRoot.get("dictionaryType"), dictType),
                            builder.equal(dictionaryRoot.get("dictionaryCode"), dictCode)
                    ));

            rtnVal = session.createQuery(criteriaQuery).getSingleResult();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public List<Dictionary> getDictionaryDependenciesByType( String dictType, String dictCode, Session session ) throws DAOException {
        List<Dictionary> rtnVal;

        try {
            String sql = " select distinct * from ifx_projects.dictionary d " +
                    "where d.dict_id in (" +
                    "select dd.dict_id from ifx_projects.dictionary_dependency dd " +
                    "left join ifx_projects.dictionary d on dd.parent_id = d.dict_id " +
                    "where d.dict_type = :dictType and d.dict_code = :dictCode) and d.dict_is_active = 1";

            NativeQuery query = session.createNativeQuery(sql);
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
        List<Object[]> rtnVal;

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
            Root<Dictionary> dictionaryRoot = criteriaQuery.from(Dictionary.class);

            criteriaQuery.multiselect(dictionaryRoot.get("dictionaryType"), dictionaryRoot.get("dictionaryCode"))
                    .where(builder.equal(dictionaryRoot.get("isActive"), 1))
                    .groupBy(dictionaryRoot.get("dictionaryType"), dictionaryRoot.get("dictionaryCode"));

            rtnVal = (List<Object[]>) session.createQuery(criteriaQuery).getResultList();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public void loadDictionary(String dictType, String dictValue, String dictCode, Date creationDate, Session session) throws DAOException {
        try {
            GuidGetter guidGetter = new GuidGetter();
            Dictionary dictionary = new Dictionary(guidGetter.getGuid(), dictType, dictCode, dictValue, 1, creationDate);

            session.save(dictionary);
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
    }

    public void loadDictionaryWithDependency(String dictType, String dictValue, String dictCode,
                                             String parentDictTypeCode, Date creationDate, Session session) throws DAOException {
        try {
            GuidGetter guidGetter = new GuidGetter();
            Dictionary dictionary = new Dictionary(guidGetter.getGuid(), dictType, dictCode, dictValue, 1, creationDate);

            session.save(dictionary);

            String[] dictTypeCode = parentDictTypeCode.split(" - ");

            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
            Root<Dictionary> dictionaryRoot = criteriaQuery.from(Dictionary.class);

            criteriaQuery.select(dictionaryRoot.get("dictionaryId"))
                    .where(builder.and(
                            builder.equal(dictionaryRoot.get("dictionaryType"), dictTypeCode[0]),
                            builder.equal(dictionaryRoot.get("dictionaryCode"), dictTypeCode[1])
                    ));

            Long parentDictId = session.createQuery(criteriaQuery).getSingleResult();

            DictionaryDependency dictDependency = new DictionaryDependency(guidGetter.getGuid(), dictionary.getDictionaryId(), parentDictId, creationDate);

            session.save(dictDependency);
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
    }

    public void updateDictionary(Session session, Long dictionaryId, boolean active) throws Exception{
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaUpdate<Dictionary> criteriaUpdate = builder.createCriteriaUpdate(Dictionary.class);

            Root<Dictionary> dictionaryRoot = criteriaUpdate.from(Dictionary.class);

            if(active) criteriaUpdate.set("isActive", 1);
            else criteriaUpdate.set("isActive", 0);

            criteriaUpdate.where(builder.equal(dictionaryRoot.get("dictionaryId"), dictionaryId));

            session.createQuery(criteriaUpdate).executeUpdate();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

    }
}
