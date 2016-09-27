SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `ifx_projects` DEFAULT CHARACTER SET latin1 ;
USE `ifx_projects` ;

-- -----------------------------------------------------
-- Table `ifx_projects`.`lookup_value`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`lookup_value` (
  `lkuvlu_id` BIGINT(20) NOT NULL ,
  `lkuvlu_name` VARCHAR(40) NOT NULL ,
  `lkuvlu_type` VARCHAR(255) NULL DEFAULT NULL ,
  `lkuvlu_data_type` VARCHAR(255) NULL DEFAULT NULL ,
  `lkuvlu_create_date` DATETIME NOT NULL ,
  `lkuvlu_modify_date` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`lkuvlu_id`) ,
  UNIQUE INDEX `klvl_name_uk_ind` USING BTREE (`lkuvlu_name` ASC) ,
  UNIQUE INDEX `luvl_id_uk_ind` USING BTREE (`lkuvlu_id` ASC) ,
  INDEX `lkuvl_information_ind` (`lkuvlu_id` ASC, `lkuvlu_name` ASC, `lkuvlu_type` ASC) ,
  INDEX `lkuvl_create_date_ind` (`lkuvlu_create_date` ASC) ,
  INDEX `lkuvl_modify_date_ind` USING BTREE (`lkuvlu_modify_date` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`actor_group`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`actor_group` (
  `actgrp_id` BIGINT(20) NOT NULL ,
  `actgrp_create_date` DATETIME NOT NULL ,
  `actgrp_modify_date` DATETIME NULL DEFAULT NULL ,
  `actgrp_group_name_lkuvl_id` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`actgrp_id`) ,
  INDEX `actgrp_id_pk_ind` USING BTREE (`actgrp_id` ASC) ,
  INDEX `acgrp_create_date_ind` (`actgrp_create_date` ASC) ,
  INDEX `acgrp_group_name_lkuvl_fk_ind` USING BTREE (`actgrp_group_name_lkuvl_id` ASC) ,
  INDEX `acgrp_group_name_lkuvl_fk` (`actgrp_group_name_lkuvl_id` ASC) ,
  CONSTRAINT `acgrp_group_name_lkuvl_fk`
    FOREIGN KEY (`actgrp_group_name_lkuvl_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`actor`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`actor` (
  `actor_username` VARCHAR(10) NOT NULL ,
  `actor_first_name` VARCHAR(30) NOT NULL ,
  `actor_last_name` VARCHAR(30) NOT NULL ,
  `actor_middle_name` VARCHAR(30) NULL DEFAULT NULL ,
  `actor_email_address` VARCHAR(50) NULL DEFAULT NULL ,
  `actor_acgrp_id` BIGINT(20) NOT NULL ,
  `actor_id` BIGINT(20) NOT NULL ,
  `actor_create_date` DATETIME NOT NULL ,
  `actor_modify_date` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`actor_id`) ,
  INDEX `actor_information_ind` USING BTREE (`actor_username` ASC, `actor_first_name` ASC, `actor_last_name` ASC) ,
  INDEX `actor_actgrp_fk_ind` (`actor_acgrp_id` ASC) ,
  INDEX `actor_ind` (`actor_id` ASC) ,
  INDEX `actor_create_date_ind` USING BTREE (`actor_create_date` ASC) ,
  CONSTRAINT `actor_actgrp_fk`
    FOREIGN KEY (`actor_acgrp_id` )
    REFERENCES `ifx_projects`.`actor_group` (`actgrp_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`project`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`project` (
  `projet_id` BIGINT(20) NOT NULL ,
  `projet_name` VARCHAR(255) NULL DEFAULT NULL ,
  `projet_projet_parent_id` BIGINT(20) NULL DEFAULT NULL ,
  `projet_create_date` DATETIME NOT NULL ,
  `projet_actor_created_by` BIGINT(20) NOT NULL ,
  `projet_actor_modified_by` BIGINT(20) NULL DEFAULT NULL ,
  `projet_modified_date` DATETIME NULL DEFAULT NULL ,
  `projet_level` INT(11) NULL DEFAULT NULL ,
  `projet_is_public` INT(1) NOT NULL ,
  `projet_edit_group_id` BIGINT(20) ,
  `projet_view_group_id` BIGINT(20) ,
  PRIMARY KEY (`projet_id`) ,
  UNIQUE INDEX `projet_id_uk_ind` USING BTREE (`projet_id` ASC) ,
  INDEX `projet_information_ind` USING BTREE (`projet_id` ASC, `projet_name` ASC, `projet_projet_parent_id` ASC, `projet_create_date` ASC, `projet_actor_created_by` ASC, `projet_level` ASC) ,
  INDEX `projet_projet_parent_id_fk_ind` USING BTREE (`projet_projet_parent_id` ASC) ,
  INDEX `projet_create_date_ind` USING BTREE (`projet_create_date` ASC) ,
  INDEX `projet_modified_date_ind` USING BTREE (`projet_modified_date` ASC) ,
  INDEX `projet_actor_created_by_fk_ind` USING BTREE (`projet_actor_created_by` ASC) ,
  INDEX `projet_actor_modified_by_fk_ind` USING BTREE (`projet_actor_modified_by` ASC) ,
  INDEX `projet_is_public_ind` (`projet_is_public` ASC) ,
  INDEX `projet_edit_group_id_ind` (`projet_edit_group_id` ASC) ,
  INDEX `projet_view_group_id_ind` (`projet_view_group_id` ASC) ,
  CONSTRAINT `projet_actor_created_by_fk`
    FOREIGN KEY (`projet_actor_created_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `projet_actor_modified_by_fk`
    FOREIGN KEY (`projet_actor_modified_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `projet_projet_parent_id_fk`
    FOREIGN KEY (`projet_projet_parent_id` )
    REFERENCES `ifx_projects`.`project` (`projet_id` )
    ON UPDATE NO ACTION)
  CONSTRAINT `projet_edit_group_id_fk`
    FOREIGN KEY (`projet_edit_group_id` )
    REFERENCES `ifx_projects`.`group` (`group_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `projet_view_group_id_fk`
    FOREIGN KEY (`projet_view_group_id` )
    REFERENCES `ifx_projects`.`group` (`group_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`sample`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`sample` (
  `sample_id` BIGINT(20) NOT NULL ,
  `sample_projet_id` BIGINT(20) NOT NULL ,
  `sample_name` VARCHAR(255) NOT NULL ,
  `sample_created_by` BIGINT(20) NOT NULL ,
  `sample_create_date` DATETIME NOT NULL ,
  `sample_modified_by` BIGINT(20) NULL DEFAULT NULL ,
  `sample_modified_date` DATETIME NULL DEFAULT NULL ,
  `sample_is_public` BIT NOT NULL ,
  PRIMARY KEY (`sample_id`) ,
  UNIQUE INDEX `sample_id_uk_ind` USING BTREE (`sample_id` ASC) ,
  INDEX `sample_information_ind` USING BTREE (`sample_projet_id` ASC, `sample_id` ASC, `sample_name` ASC, `sample_created_by` ASC, `sample_create_date` ASC) ,
  INDEX `sample_projet_id_fk_ind` USING BTREE (`sample_projet_id` ASC) ,
  INDEX `sample_created_by_fk_ind` USING BTREE (`sample_created_by` ASC) ,
  INDEX `sample_modified_by_fk_ind` USING BTREE (`sample_modified_by` ASC) ,
  INDEX `sample_create_date_ind` USING BTREE (`sample_create_date` ASC) ,
  INDEX `sample_modified_date_ind` USING BTREE (`sample_modified_date` ASC) ,
  INDEX `sample_is_public_ind` (`sample_is_public` ASC) ,
  CONSTRAINT `sample_created_by_fk`
    FOREIGN KEY (`sample_created_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sample_modified_by_fk`
    FOREIGN KEY (`sample_modified_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sample_projet_id_fk`
    FOREIGN KEY (`sample_projet_id` )
    REFERENCES `ifx_projects`.`project` (`projet_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`event`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`event` (
  `event_id` BIGINT(20) NOT NULL ,
  `event_projet_id` BIGINT(20) NOT NULL ,
  `event_type_lkuvl_id` BIGINT(20) NULL DEFAULT NULL ,
  `event_actor_created_by` BIGINT(20) NOT NULL ,
  `event_create_date` DATETIME NOT NULL ,
  `event_actor_modified_by` BIGINT(20) NULL DEFAULT NULL ,
  `event_modified_date` DATETIME NULL DEFAULT NULL ,
  `event_status_lkuvl_id` BIGINT(20) NULL DEFAULT NULL ,
  `event_sampl_id` BIGINT(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`event_id`) ,
  UNIQUE INDEX `event_id_ind` USING BTREE (`event_id` ASC) ,
  INDEX `event_sampl_id_fk_ind` USING BTREE (`event_sampl_id` ASC) ,
  INDEX `event_ids_ind` USING BTREE (`event_id` ASC, `event_projet_id` ASC, `event_actor_created_by` ASC, `event_actor_modified_by` ASC, `event_type_lkuvl_id` ASC, `event_status_lkuvl_id` ASC) ,
  INDEX `event_projet_id_fk_ind` USING BTREE (`event_projet_id` ASC) ,
  INDEX `event_type_lkuvl_id_fk_ind` USING BTREE (`event_type_lkuvl_id` ASC) ,
  INDEX `event_actor_created_by_fk_ind` USING BTREE (`event_actor_created_by` ASC) ,
  INDEX `event_actor_modified_by_fk_ind` USING BTREE (`event_actor_modified_by` ASC) ,
  INDEX `event_status_lkuvl_id_fk_ind` USING BTREE (`event_status_lkuvl_id` ASC) ,
  CONSTRAINT `event_actor_created_by_fk`
    FOREIGN KEY (`event_actor_created_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `event_actor_modified_by_fk`
    FOREIGN KEY (`event_actor_modified_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `event_projet_id_fk`
    FOREIGN KEY (`event_projet_id` )
    REFERENCES `ifx_projects`.`project` (`projet_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `event_sampl_id_fk`
    FOREIGN KEY (`event_sampl_id` )
    REFERENCES `ifx_projects`.`sample` (`sample_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `event_status_lkuvl_id_fk`
    FOREIGN KEY (`event_status_lkuvl_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `event_type_lkuvl_id_fk`
    FOREIGN KEY (`event_type_lkuvl_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`event_attribute`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`event_attribute` (
  `eventa_id` BIGINT(20) NOT NULL ,
  `eventa_lkuvlu_attribute_id` BIGINT(20) NOT NULL ,
  `eventa_event_id` BIGINT(20) NOT NULL ,
  `eventa_attribute_date` DATETIME NULL DEFAULT NULL ,
  `eventa_attribute_float` FLOAT NULL DEFAULT NULL ,
  `eventa_attribute_str` VARCHAR(4000) NULL DEFAULT NULL ,
  `eventa_attribute_int` INT NULL DEFAULT NULL ,
  `eventa_actor_created_by` BIGINT(20) NULL DEFAULT NULL ,
  `eventa_actor_modified_by` BIGINT(20) NULL DEFAULT NULL ,
  `eventa_create_date` DATETIME NULL DEFAULT NULL ,
  `eventa_modified_date` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`eventa_id`) ,
  INDEX `eventa_ids_ind` USING BTREE (`eventa_id` ASC, `eventa_lkuvlu_attribute_id` ASC, `eventa_event_id` ASC, `eventa_actor_created_by` ASC) ,
  INDEX `eventa_event_id_fk_ind` USING BTREE (`eventa_event_id` ASC) ,
  INDEX `eventa_lkuvlu_attribute_id_fk_ind` USING BTREE (`eventa_lkuvlu_attribute_id` ASC) ,
  INDEX `eventa_actor_created_by_fk_ind` USING BTREE (`eventa_actor_created_by` ASC) ,
  INDEX `eventa_actor_modified_by_fk_ind` USING BTREE (`eventa_actor_modified_by` ASC) ,
  CONSTRAINT `eventa_actor_created_by_fk`
    FOREIGN KEY (`eventa_actor_created_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `eventa_actor_modified_by_fk`
    FOREIGN KEY (`eventa_actor_modified_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `eventa_event_id_fk`
    FOREIGN KEY (`eventa_event_id` )
    REFERENCES `ifx_projects`.`event` (`event_id` ),
  CONSTRAINT `eventa_lkuvlu_attribute_id_fk`
    FOREIGN KEY (`eventa_lkuvlu_attribute_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`event_meta_attribute`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`event_meta_attribute` (
  `evenma_id` BIGINT(20) NOT NULL ,
  `evenma_projet_id` BIGINT(20) NOT NULL ,
  `evenma_event_type_lkuvl_id` BIGINT(20) NOT NULL ,
  `evenma_lkuvlu_attribute_id` BIGINT(20) NOT NULL ,
  `evenma_is_required` INT(1) NOT NULL ,
  `evenma_desc` VARCHAR(1000) NULL DEFAULT NULL ,
  `evenma_options` VARCHAR(1000) NULL DEFAULT NULL ,
  `evenma_create_date` DATETIME NOT NULL ,
  `evenma_actor_created_by` BIGINT(20) NOT NULL ,
  `evenma_actor_modified_by` BIGINT(20) NULL DEFAULT NULL ,
  `evenma_is_active` INT(1) NULL ,
  `evenma_modified_date` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`evenma_id`) ,
  UNIQUE INDEX `evenma_id_ind` USING BTREE (`evenma_id` ASC) ,
  INDEX `evenma_attribute_ids_ind` USING BTREE (`evenma_id` ASC, `evenma_lkuvlu_attribute_id` ASC) ,
  INDEX `evenma_lkuvlu_attribute_id_fk_ind` USING BTREE (`evenma_lkuvlu_attribute_id` ASC) ,
  INDEX `evenma_lkuvlu_event_type_fk_ind` USING BTREE (`evenma_event_type_lkuvl_id` ASC) ,
  INDEX `evenma_project_id_fk_ind` USING BTREE (`evenma_projet_id` ASC) ,
  INDEX `evenma_actor_created_by_fk_ind` USING BTREE (`evenma_actor_created_by` ASC) ,
  INDEX `evenma_actor_modified_by_fk_ind` USING BTREE (`evenma_actor_modified_by` ASC) ,
  UNIQUE INDEX `evenma_attribute_list_uk` (`evenma_projet_id` ASC, `evenma_event_type_lkuvl_id` ASC, `evenma_lkuvlu_attribute_id` ASC) ,
  CONSTRAINT `evenma_actor_created_by_fk`
    FOREIGN KEY (`evenma_actor_created_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `evenma_actor_modified_by_fk`
    FOREIGN KEY (`evenma_actor_modified_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `evenma_lkuvlu_attribute_id_fk`
    FOREIGN KEY (`evenma_lkuvlu_attribute_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` ),
  CONSTRAINT `evenma_lkuvlu_event_type_fk`
    FOREIGN KEY (`evenma_event_type_lkuvl_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` ),
  CONSTRAINT `evenma_project_id_fk`
    FOREIGN KEY (`evenma_projet_id` )
    REFERENCES `ifx_projects`.`project` (`projet_id` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`project_attribute`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`project_attribute` (
  `projea_id` BIGINT(20) NOT NULL ,
  `projea_projet_id` BIGINT(20) NOT NULL ,
  `projea_lkuvlu_attribute_id` BIGINT(20) NOT NULL ,
  `projea_attribute_date` DATETIME NULL DEFAULT NULL ,
  `projea_attribute_str` VARCHAR(4000) NULL DEFAULT NULL ,
  `projea_attribute_float` FLOAT NULL DEFAULT NULL ,
  `projea_attribute_int` INT NULL DEFAULT NULL ,
  `projea_actor_created_by` BIGINT(20) NOT NULL ,
  `projea_create_date` DATETIME NOT NULL ,
  `projea_actor_modified_by` BIGINT(20) NULL DEFAULT NULL ,
  `projea_modified_date` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`projea_id`) ,
  UNIQUE INDEX `proja_id_uk_ind` USING BTREE (`projea_id` ASC) ,
  INDEX `proja_ids_ind` USING BTREE (`projea_projet_id` ASC, `projea_id` ASC, `projea_lkuvlu_attribute_id` ASC) ,
  INDEX `projea_projet_id_fk_ind` USING BTREE (`projea_projet_id` ASC) ,
  INDEX `projea_lkuvlu_attribute_id_fk_ind` USING BTREE (`projea_lkuvlu_attribute_id` ASC) ,
  INDEX `projea_actor_created_by_fk_ind` USING BTREE (`projea_actor_created_by` ASC) ,
  INDEX `projea_actor_modified_by_fk_ind` USING BTREE (`projea_actor_modified_by` ASC) ,
  INDEX `projea_create_date_ind` USING BTREE (`projea_create_date` ASC) ,
  INDEX `projea_modified_date_ind` USING BTREE (`projea_modified_date` ASC) ,
  CONSTRAINT `projea_actor_created_by_fk`
    FOREIGN KEY (`projea_actor_created_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `projea_actor_modified_by_fk`
    FOREIGN KEY (`projea_actor_modified_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `projea_lkuvlu_attribute_id_fk`
    FOREIGN KEY (`projea_lkuvlu_attribute_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `projea_projet_id_fk`
    FOREIGN KEY (`projea_projet_id` )
    REFERENCES `ifx_projects`.`project` (`projet_id` )
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`project_meta_attribute`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`project_meta_attribute` (
  `projma_id` BIGINT(20) NOT NULL ,
  `projma_projet_id` BIGINT(20) NOT NULL ,
  `projma_lkuvlu_attribute_id` BIGINT(20) NOT NULL ,
  `projma_is_required` INT(1) NOT NULL ,
  `projma_options` VARCHAR(4000) NULL DEFAULT NULL ,
  `projma_attribute_desc` VARCHAR(1000) NULL DEFAULT NULL ,
  `projma_actor_created_by` BIGINT(20) NOT NULL ,
  `projma_create_date` DATETIME NOT NULL ,
  `projma_modified_date` DATETIME NULL DEFAULT NULL ,
  `projma_actor_modified_by` BIGINT(20) NULL DEFAULT NULL ,
  `projma_is_active` INT(1) NULL ,
  PRIMARY KEY (`projma_id`) ,
  UNIQUE INDEX `projma_id_uk_ind` USING BTREE (`projma_id` ASC) ,
  INDEX `projma_ids_fk_ind` USING BTREE (`projma_id` ASC, `projma_projet_id` ASC, `projma_lkuvlu_attribute_id` ASC, `projma_actor_created_by` ASC, `projma_options`(767) ASC) ,
  INDEX `projma_actor_created_by_fk_ind` USING BTREE (`projma_actor_created_by` ASC) ,
  INDEX `projma_actor_modified_by_fk_ind` USING BTREE (`projma_actor_modified_by` ASC) ,
  INDEX `projma_project_id_fk_ind` USING BTREE (`projma_projet_id` ASC) ,
  INDEX `projma_lkuvlu_attribute_id_fk_ind` USING BTREE (`projma_lkuvlu_attribute_id` ASC) ,
  UNIQUE INDEX `projma_attribute_list_uk` (`projma_projet_id` ASC, `projma_lkuvlu_attribute_id` ASC) ,
  CONSTRAINT `projma_actor_created_by_fk`
    FOREIGN KEY (`projma_actor_created_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `projma_actor_modified_by_fk`
    FOREIGN KEY (`projma_actor_modified_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `projma_lkuvlu_attribute_id_fk`
    FOREIGN KEY (`projma_lkuvlu_attribute_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `projma_project_id_fk`
    FOREIGN KEY (`projma_projet_id` )
    REFERENCES `ifx_projects`.`project` (`projet_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`sample_attribute`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`sample_attribute` (
  `sampla_id` BIGINT(20) NOT NULL ,
  `sampla_projet_id` BIGINT(20) NOT NULL ,
  `sampla_lkuvlu_attribute_id` BIGINT(20) NOT NULL ,
  `sampla_sample_id` BIGINT(20) NOT NULL ,
  `sampla_attribute_date` DATETIME NULL DEFAULT NULL ,
  `sampla_attribute_float` FLOAT NULL DEFAULT NULL ,
  `sampla_attribute_str` VARCHAR(4000) NULL DEFAULT NULL ,
  `sampla_attribute_int` INT NULL DEFAULT NULL ,
  `sampla_actor_created_by` BIGINT(20) NOT NULL ,
  `sampla_actor_modified_by` BIGINT(20) NULL DEFAULT NULL ,
  `sampla_create_date` DATETIME NOT NULL ,
  `sampla_modified_date` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`sampla_id`) ,
  UNIQUE INDEX `sampla_id_uk_ind` USING BTREE (`sampla_id` ASC) ,
  INDEX `sampla_information_ind` USING BTREE (`sampla_projet_id` ASC, `sampla_id` ASC, `sampla_lkuvlu_attribute_id` ASC, `sampla_sample_id` ASC, `sampla_attribute_date` ASC, `sampla_attribute_float` ASC, `sampla_attribute_str`(767) ASC, `sampla_actor_created_by` ASC, `sampla_create_date` ASC) ,
  INDEX `sampla_projet_id_fk` USING BTREE (`sampla_projet_id` ASC) ,
  INDEX `sampla_lkuvlu_attribute_id_fk_ind` USING BTREE (`sampla_lkuvlu_attribute_id` ASC) ,
  INDEX `sampla_sample_id_fk_ind` USING BTREE (`sampla_sample_id` ASC) ,
  INDEX `sampla_actor_created_by_fk` USING BTREE (`sampla_actor_created_by` ASC) ,
  INDEX `sampla_actor_modified_by_fk` USING BTREE (`sampla_actor_modified_by` ASC) ,
  INDEX `sampla_create_date_ind` USING BTREE (`sampla_create_date` ASC) ,
  INDEX `sampla_modified_date_ind` USING BTREE (`sampla_modified_date` ASC) ,
  CONSTRAINT `sampla_actor_created_by_fk`
    FOREIGN KEY (`sampla_actor_created_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sampla_actor_modified_by_fk`
    FOREIGN KEY (`sampla_actor_modified_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sampla_lkuvlu_attribute_id_fk`
    FOREIGN KEY (`sampla_lkuvlu_attribute_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sampla_projet_id_fk`
    FOREIGN KEY (`sampla_projet_id` )
    REFERENCES `ifx_projects`.`project` (`projet_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sampla_sample_id_fk`
    FOREIGN KEY (`sampla_sample_id` )
    REFERENCES `ifx_projects`.`sample` (`sample_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;


-- -----------------------------------------------------
-- Table `ifx_projects`.`sample_meta_attribute`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `ifx_projects`.`sample_meta_attribute` (
  `sampma_id` BIGINT(20) NOT NULL ,
  `sampma_projet_id` BIGINT(20) NOT NULL ,
  `sampma_lkuvlu_attribute_id` BIGINT(20) NOT NULL ,
  `sampma_is_required` INT(1) NOT NULL ,
  `sampma_options` VARCHAR(4000) NULL DEFAULT NULL ,
  `sampma_attribute_desc` VARCHAR(1000) NULL DEFAULT NULL ,
  `sampma_actor_created_by` BIGINT(20) NULL DEFAULT NULL ,
  `sampma_actor_modified_by` BIGINT(20) NULL DEFAULT NULL ,
  `sampma_create_date` DATETIME NULL DEFAULT NULL ,
  `sampma_modified_date` DATETIME NULL DEFAULT NULL ,
  `sampma_is_active` INT(1) NULL ,
  PRIMARY KEY (`sampma_id`) ,
  UNIQUE INDEX `sampma_id_uk_ind` USING BTREE (`sampma_id` ASC) ,
  INDEX `sampma_project_id_fk_ind` USING BTREE (`sampma_projet_id` ASC) ,
  INDEX `sampma_actor_created_by_fk_ind` USING BTREE (`sampma_actor_created_by` ASC) ,
  INDEX `sampma_actor_modified_by_fk_ind` USING BTREE (`sampma_actor_modified_by` ASC) ,
  INDEX `sampma_lkuvlu_attribute_id_fk_ind` USING BTREE (`sampma_lkuvlu_attribute_id` ASC) ,
  INDEX `sampma_ids_ind` USING BTREE (`sampma_id` ASC, `sampma_projet_id` ASC, `sampma_lkuvlu_attribute_id` ASC, `sampma_actor_created_by` ASC, `sampma_create_date` ASC, `sampma_options`(767) ASC) ,
  UNIQUE INDEX `sampma_attribute_list_uk` (`sampma_projet_id` ASC, `sampma_lkuvlu_attribute_id` ASC) ,
  CONSTRAINT `sampma_actor_created_by_fk`
    FOREIGN KEY (`sampma_actor_created_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sampma_actor_modified_by_fk`
    FOREIGN KEY (`sampma_actor_modified_by` )
    REFERENCES `ifx_projects`.`actor` (`actor_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sampma_lkuvlu_attribute_id_fk`
    FOREIGN KEY (`sampma_lkuvlu_attribute_id` )
    REFERENCES `ifx_projects`.`lookup_value` (`lkuvlu_id` ),
  CONSTRAINT `sampma_project_id_fk`
    FOREIGN KEY (`sampma_projet_id` )
    REFERENCES `ifx_projects`.`project` (`projet_id` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
ROW_FORMAT = COMPACT;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
