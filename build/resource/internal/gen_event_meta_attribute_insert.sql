-- EVENT_META_ATTRIBUTE
-- select ema.* from ifx_projects. project p, ifx_projects. event_meta_attribute ema where p.projet_is_public = 1 and p.projet_id = ema.evenma_projet_id;
SELECT
        'INSERT INTO ifx_projects.event_meta_attribute ( evenma_id, evenma_projet_id, evenma_event_type_lkuvl_id, evenma_lkuvlu_attribute_id, evenma_is_required, evenma_desc, evenma_options, evenma_create_date, evenma_actor_created_by, evenma_actor_modified_by, evenma_is_active, evenma_modified_date, evenma_is_sample_required, evenma_ontology ) VALUES ( ',
        evenma_id,  ",",
        evenma_projet_id,  ",",
        evenma_event_type_lkuvl_id,  ",",
        evenma_lkuvlu_attribute_id,  ",",
        evenma_is_required,  ",",
        CONCAT('\"',evenma_desc,'\"') , ",",
        CONCAT('\"',evenma_options,'\"') , ",",
        CONCAT('\"',evenma_create_date,'\"') , ",",
        evenma_actor_created_by, ", ",
        evenma_actor_modified_by, ", ",
        evenma_is_active , ",",
        CONCAT('\"',evenma_modified_date,'\"') ,",",
        evenma_is_sample_required ,",",
        CONCAT('\"',evenma_ontology,'\"') ,
        ' ) ;'
FROM ifx_projects.project p, ifx_projects.event_meta_attribute ema
WHERE p.projet_id = ema.evenma_projet_id
;

