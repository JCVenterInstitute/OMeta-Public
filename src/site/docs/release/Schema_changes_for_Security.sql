--group
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`group` (
  `group_id` BIGINT(20) NOT NULL ,
  `group_name_lkuvl_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`group_id`) ,
  INDEX `group_id_pk_ind` USING BTREE (`group_id` ASC) ,
  INDEX `group_name_lkuvl_fk_ind` USING BTREE (`group_name_lkuvl_id` ASC) ,
  INDEX `group_name_lkuvl_fk` (`group_name_lkuvl_id` ASC) ,
  CONSTRAINT `group_name_lkuvl_fk`
    FOREIGN KEY (`group_name_lkuvl_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;

--project
ALTER TABLE `ifx_projects`.`project`
	ADD COLUMN projet_view_group_id BIGINT(20),
	ADD CONSTRAINT UNIQUE INDEX projet_view_group_ind USING BTREE (projet_view_group_id ASC) ,
	ADD CONSTRAINT `projet_view_group_id_fk`
    		FOREIGN KEY (`projet_view_group_id` )
    		REFERENCES `ifx_projects`.`group` (`group_id` )
    		ON DELETE NO ACTION
    		ON UPDATE NO ACTION
;
ALTER TABLE `ifx_projects`.`project`
	ADD COLUMN projet_edit_group_id BIGINT(20),
	ADD UNIQUE INDEX projet_edit_group_ind USING BTREE (projet_edit_group_id ASC) ,
	ADD CONSTRAINT projet_edit_group_id_fk
    		FOREIGN KEY (projet_edit_group_id )
    		REFERENCES `ifx_projects`.`group` (group_id)
    		ON DELETE NO ACTION
    		ON UPDATE NO ACTION
;

ALTER TABLE project ADD projet_is_secure INT(1) NOT NULL DEFAULT 0;
ALTER TABLE project ADD INDEX projet_is_secure_ind (projet_is_secure ASC);

--actor_group
--ALTER TABLE 'ifx_projects'.'actor_group' DROP FOREIGN KEY acgrp_group_name_lkuvl_fk;
--ALTER TABLE 'ifx_projects'.'actor_group' DROP 'actgrp_group_name_lkuvl_id';
--ALTER TABLE `actor_group`
--	ADD COLUMN actgrp_actor_id BIGINT(20),
--	ADD UNIQUE INDEX actgrp_actgrp_actor_id_ind USING BTREE (actgrp_actor_id ASC) ,
--	ADD CONSTRAINT actgrp_actgrp_actor_fk
--   		FOREIGN KEY (actgrp_actor_id)
--    		REFERENCES `ifx_projects`.`actor` (actor_id)
--    		ON DELETE NO ACTION
--    		ON UPDATE NO ACTION
--;
--ALTER TABLE `actor_group`
--	ADD COLUMN actgrp_group_id BIGINT(20),
--	ADD UNIQUE INDEX actgrp_actgrp_group_id_ind USING BTREE (actgrp_group_id ASC) ,
--	ADD CONSTRAINT actgrp_actgrp_group_fk
--    		FOREIGN KEY (actgrp_group_id)
--   		REFERENCES `ifx_projects`.`group` (actor_id)
--    		ON DELETE NO ACTION
--    		ON UPDATE NO ACTION
--;

ALTER TABLE  `actor` DROP FOREIGN KEY  `actor_actgrp_fk` ;

DROP TABLE actor_group;

CREATE  TABLE IF NOT EXISTS actor_group (
  actgrp_id BIGINT(20) NOT NULL ,
  actgrp_create_date DATETIME NOT NULL ,
  actgrp_modify_date DATETIME NULL DEFAULT NULL ,
  actgrp_actor_id BIGINT(20) NULL DEFAULT NULL ,
  actgrp_group_id BIGINT(20) NULL DEFAULT NULL,
  PRIMARY KEY (actgrp_id) ,
  INDEX actgrp_id_pk_ind USING BTREE (actgrp_id ASC) ,
  INDEX acgrp_create_date_ind (actgrp_create_date ASC),
  INDEX actgrp_actgrp_actor_id_ind USING BTREE (actgrp_actor_id ASC) ,
  CONSTRAINT actgrp_actgrp_actor_fk
	FOREIGN KEY (actgrp_actor_id)
	REFERENCES actor (actor_id)
	ON DELETE NO ACTION
	ON UPDATE NO ACTION,
  INDEX actgrp_actgrp_group_id_ind USING BTREE (actgrp_group_id ASC) ,
  CONSTRAINT actgrp_actgrp_group_fk
	FOREIGN KEY (actgrp_group_id)
	REFERENCES ifx_projects.group (group_id)
	ON DELETE NO ACTION
	ON UPDATE NO ACTION)
	ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


--If actor table has any records in it, pre-existing reference actor_group_ids have to be added prior to adding foreign key
--SELECT actor_acgrp_id FROM actor;
--INSERT INTO actor_group (actgrp_id, actgrp_create_date) VALUES (1128147435169, now());

ALTER TABLE  `actor` ADD FOREIGN KEY (  `actor_acgrp_id` ) REFERENCES  `ifx_projects`.`actor_group` (
`actgrp_id`
) ON DELETE NO ACTION ON UPDATE NO ACTION ;




