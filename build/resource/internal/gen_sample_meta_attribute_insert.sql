-- SAMPLE_META_ATTRIBUTE
-- select sma.* from ifx_projects. project p, ifx_projects. sample_meta_attribute sma where p.projet_is_public = 1 and p.projet_id = sma.sampma_projet_id;
SELECT
        'INSERT INTO ifx_projects.sample_meta_attribute ( sampma_id, sampma_projet_id, sampma_lkuvlu_attribute_id, sampma_is_required, sampma_options, sampma_attribute_desc, sampma_actor_created_by, sampma_actor_modified_by, sampma_create_date, sampma_modified_date, sampma_is_active, sampma_ontology  ) VALUES ( ',
        sampma_id,  ",",
        sampma_projet_id,  ",",
        sampma_lkuvlu_attribute_id,  ",",
        sampma_is_required,  ",",
        CONCAT('\"',sampma_options,'\"') , ",",
        CONCAT('\"',sampma_attribute_desc,'\"') , ",",
        sampma_actor_created_by, ", ",
        sampma_actor_modified_by, ",",
        CONCAT('\"',sampma_create_date,'\"') , ",",
        CONCAT('\"',sampma_modified_date,'\"') , ",",
        sampma_is_active, ", ",
        CONCAT('\"',sampma_ontology,'\"'),
        ' ) ;'
FROM ifx_projects.project p, ifx_projects.sample_meta_attribute sma
WHERE p.projet_id = sma.sampma_projet_id
;

