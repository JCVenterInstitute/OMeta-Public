/*
--Script to setup ifx_projects database objects
--Script will create ifx_projects database, and ifx_projects_app and ifx_projects_adm users with default password of welcome.
--It's highly recommended that adminstrator changes the passwords before using OMETA in production.
*/

/*--Script for OMETA*/
Create database ifx_projects;
/*-- Grants for 'ifx_projects_adm'@'%'*/
SET old_passwords = 0;
CREATE USER ifx_projects_adm IDENTIFIED BY 'welcome';
GRANT ALL PRIVILEGES ON `ifx\_projects`.* TO 'ifx_projects_adm'@'%' WITH GRANT OPTION;

-- Grants for 'ifx_projects_app'@'%'
CREATE USER ifx_projects_app IDENTIFIED BY 'welcome'; 
GRANT DELETE, INSERT, SELECT, UPDATE ON `ifx\_projects`.* TO 'ifx_projects_app'@'%';


use ifx_projects;

/*-- MySQL dump 10.13  Distrib 5.6.16-64.2, for Linux (x86_64)*/
/*--*/
/*-- Host: mysql-lan-pro.jcvi.org    Database: ifx_projects*/
/*-- ------------------------------------------------------*/
/*-- Server version	5.1.39-log*/
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*--*/
/*-- Table structure for table `lookup_value`*/
/*--*/

DROP TABLE IF EXISTS `lookup_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lookup_value` (
  `lkuvlu_id` bigint(20) NOT NULL,
  `lkuvlu_name` varchar(255) NOT NULL,
  `lkuvlu_type` varchar(255) DEFAULT NULL,
  `lkuvlu_data_type` varchar(255) DEFAULT NULL,
  `lkuvlu_create_date` datetime NOT NULL,
  `lkuvlu_modify_date` datetime DEFAULT NULL,
  PRIMARY KEY (`lkuvlu_id`),
  UNIQUE KEY `klvl_name_uk_ind` (`lkuvlu_name`) USING BTREE,
  UNIQUE KEY `luvl_id_uk_ind` (`lkuvlu_id`) USING BTREE,
  KEY `lkuvl_information_ind` (`lkuvlu_id`,`lkuvlu_name`,`lkuvlu_type`),
  KEY `lkuvl_create_date_ind` (`lkuvlu_create_date`),
  KEY `lkuvl_modify_date_ind` (`lkuvlu_modify_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER lv_modify_date_trg BEFORE UPDATE ON ifx_projects.lookup_value  
  FOR EACH ROW BEGIN SET NEW.lkuvlu_modify_date = CURRENT_TIMESTAMP;   
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;


/*--*/
/*-- Table structure for table `actor`*/
/*--*/

DROP TABLE IF EXISTS `actor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `actor` (
  `actor_username` varchar(10) NOT NULL,
  `actor_first_name` varchar(30) NOT NULL,
  `actor_last_name` varchar(30) NOT NULL,
  `actor_middle_name` varchar(30) DEFAULT NULL,
  `actor_email_address` varchar(50) DEFAULT NULL,
  `actor_id` bigint(20) NOT NULL,
  `actor_create_date` datetime NOT NULL,
  `actor_modify_date` datetime DEFAULT NULL,
  PRIMARY KEY (`actor_id`),
  KEY `actor_information_ind` (`actor_username`,`actor_first_name`,`actor_last_name`) USING BTREE,
  KEY `actor_ind` (`actor_id`),
  KEY `actor_create_date_ind` (`actor_create_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER actor_modify_date_trg BEFORE UPDATE ON ifx_projects.actor  
  FOR EACH ROW BEGIN SET NEW.actor_modify_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;


/*--*/
/*-- Table structure for table `groups`*/
/*--*/

DROP TABLE IF EXISTS `groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `groups` (
  `group_id` bigint(20) NOT NULL,
  `group_name_lkuvl_id` bigint(20) NOT NULL,
  PRIMARY KEY (`group_id`),
  KEY `group_id_pk_ind` (`group_id`) USING BTREE,
  KEY `group_name_lkuvl_fk_ind` (`group_name_lkuvl_id`) USING BTREE,
  KEY `group_name_lkuvl_fk` (`group_name_lkuvl_id`),
  CONSTRAINT `group_name_lkuvl_fk` FOREIGN KEY (`group_name_lkuvl_id`) REFERENCES `lookup_value` (`lkuvlu_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

/*--*/
/*-- Table structure for table `actor_group`*/
/*--*/

DROP TABLE IF EXISTS `actor_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `actor_group` (
  `actgrp_id` bigint(20) NOT NULL,
  `actgrp_create_date` datetime NOT NULL,
  `actgrp_modify_date` datetime DEFAULT NULL,
  `actgrp_actor_id` bigint(20) DEFAULT NULL,
  `actgrp_group_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`actgrp_id`),
  KEY `actgrp_id_pk_ind` (`actgrp_id`) USING BTREE,
  KEY `acgrp_create_date_ind` (`actgrp_create_date`),
  KEY `actgrp_actgrp_actor_id_ind` (`actgrp_actor_id`) USING BTREE,
  KEY `actgrp_actgrp_group_id_ind` (`actgrp_group_id`) USING BTREE,
  CONSTRAINT `actgrp_actgrp_actor_fk` FOREIGN KEY (`actgrp_actor_id`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `actgrp_actgrp_group_fk` FOREIGN KEY (`actgrp_group_id`) REFERENCES `groups` (`group_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER actor_grp_modify_date_trg BEFORE UPDATE ON ifx_projects.actor_group  
  FOR EACH ROW BEGIN SET NEW.actgrp_modify_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

/*--*/
/*-- Table structure for table `project`*/
/*--*/

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project` (
  `projet_id` bigint(20) NOT NULL,
  `projet_name` varchar(255) DEFAULT NULL,
  `projet_projet_parent_id` bigint(20) DEFAULT NULL,
  `projet_create_date` datetime NOT NULL,
  `projet_actor_created_by` bigint(20) NOT NULL,
  `projet_actor_modified_by` bigint(20) DEFAULT NULL,
  `projet_modified_date` datetime DEFAULT NULL,
  `projet_level` int(11) DEFAULT NULL,
  `projet_is_public` int(1) NOT NULL,
  `projet_view_group_id` bigint(20) DEFAULT NULL,
  `projet_edit_group_id` bigint(20) DEFAULT NULL,
  `projet_is_secure` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`projet_id`),
  UNIQUE KEY `projet_id_uk_ind` (`projet_id`) USING BTREE,
  KEY `proje_information_ind` (`projet_id`,`projet_name`,`projet_projet_parent_id`,`projet_create_date`,`projet_actor_created_by`,`projet_level`) USING BTREE,
  KEY `projet_projet_parent_id_fk_ind` (`projet_projet_parent_id`) USING BTREE,
  KEY `projet_create_date_ind` (`projet_create_date`) USING BTREE,
  KEY `projet_modified_date_ind` (`projet_modified_date`) USING BTREE,
  KEY `projet_actor_created_by_fk_ind` (`projet_actor_created_by`) USING BTREE,
  KEY `projet_actor_modified_by_fk_ind` (`projet_actor_modified_by`) USING BTREE,
  KEY `projet_is_public_ind` (`projet_is_public`),
  KEY `projet_is_secure_ind` (`projet_is_secure`),
  KEY `projet_view_group_ind` (`projet_view_group_id`) USING BTREE,
  KEY `projet_edit_group_ind` (`projet_edit_group_id`) USING BTREE,
  CONSTRAINT `project_ibfk_1` FOREIGN KEY (`projet_edit_group_id`) REFERENCES `groups` (`group_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `project_ibfk_2` FOREIGN KEY (`projet_view_group_id`) REFERENCES `groups` (`group_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `projet_actor_created_by_fk` FOREIGN KEY (`projet_actor_created_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `projet_actor_modified_by_fk` FOREIGN KEY (`projet_actor_modified_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `projet_projet_parent_id_fk` FOREIGN KEY (`projet_projet_parent_id`) REFERENCES `project` (`projet_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER project_modify_date_trg BEFORE UPDATE ON ifx_projects.project  
  FOR EACH ROW BEGIN SET NEW.projet_modified_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`ifx_projects_adm`@`%`*/ /*!50003 TRIGGER project_cascade_update AFTER UPDATE ON ifx_projects.project FOR EACH ROW 
  BEGIN
    UPDATE ifx_projects.project_attribute AS pa SET pa.projea_modified_date=CURRENT_TIMESTAMP
      WHERE pa.projea_projet_id=NEW.projet_id;
    UPDATE ifx_projects.project_meta_attribute AS pma set pma.projma_modified_date=CURRENT_TIMESTAMP
      WHERE pma.projma_projet_id=NEW.projet_id;
    UPDATE ifx_projects.event AS e SET e.event_modified_date=CURRENT_TIMESTAMP
      WHERE e.event_projet_id=NEW.projet_id;
    UPDATE ifx_projects.event_attribute AS ea SET ea.eventa_modified_date=CURRENT_TIMESTAMP
      WHERE ea.eventa_event_id in (SELECT e.event_id FROM event e WHERE e.event_projet_id=NEW.projet_id);
    UPDATE ifx_projects.event_meta_attribute AS ema SET ema.evenma_modified_date=CURRENT_TIMESTAMP
      WHERE ema.evenma_projet_id=NEW.projet_id;
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `project_meta_attribute`
--

DROP TABLE IF EXISTS `project_meta_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_meta_attribute` (
  `projma_id` bigint(20) NOT NULL,
  `projma_projet_id` bigint(20) NOT NULL,
  `projma_lkuvlu_attribute_id` bigint(20) NOT NULL,
  `projma_is_required` int(1) NOT NULL,
  `projma_options` varchar(4000) DEFAULT NULL,
  `projma_attribute_desc` varchar(1000) DEFAULT NULL,
  `projma_actor_created_by` bigint(20) NOT NULL,
  `projma_create_date` datetime NOT NULL,
  `projma_modified_date` datetime DEFAULT NULL,
  `projma_actor_modified_by` bigint(20) DEFAULT NULL,
  `projma_is_active` int(1) DEFAULT NULL,
  `projma_label` varchar(100) DEFAULT NULL,
  `projma_ontology` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`projma_id`),
  UNIQUE KEY `projma_id_uk_ind` (`projma_id`) USING BTREE,
  UNIQUE KEY `projma_attribute_list_uk` (`projma_projet_id`,`projma_lkuvlu_attribute_id`),
  KEY `projma_ids_fk_ind` (`projma_id`,`projma_projet_id`,`projma_lkuvlu_attribute_id`,`projma_actor_created_by`,`projma_options`(767)) USING BTREE,
  KEY `projma_actor_created_by_fk_ind` (`projma_actor_created_by`) USING BTREE,
  KEY `projma_actor_modified_by_fk_ind` (`projma_actor_modified_by`) USING BTREE,
  KEY `projma_project_id_fk_ind` (`projma_projet_id`) USING BTREE,
  KEY `projma_lkuvlu_attribute_id_fk_ind` (`projma_lkuvlu_attribute_id`) USING BTREE,
  KEY `projma_ontology` (`projma_ontology`),
  CONSTRAINT `projma_actor_created_by_fk` FOREIGN KEY (`projma_actor_created_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `projma_actor_modified_by_fk` FOREIGN KEY (`projma_actor_modified_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `projma_lkuvlu_attribute_id_fk` FOREIGN KEY (`projma_lkuvlu_attribute_id`) REFERENCES `lookup_value` (`lkuvlu_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `projma_project_id_fk` FOREIGN KEY (`projma_projet_id`) REFERENCES `project` (`projet_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER pma_modify_date_trg BEFORE UPDATE ON ifx_projects.project_meta_attribute  
  FOR EACH ROW BEGIN SET NEW.projma_modified_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;


/*--*/
/*-- Table structure for table `project_attribute`*/
/*--*/

DROP TABLE IF EXISTS `project_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_attribute` (
  `projea_id` bigint(20) NOT NULL,
  `projea_projet_id` bigint(20) NOT NULL,
  `projea_lkuvlu_attribute_id` bigint(20) NOT NULL,
  `projea_attribute_date` datetime DEFAULT NULL,
  `projea_attribute_str` varchar(4000) DEFAULT NULL,
  `projea_attribute_float` float DEFAULT NULL,
  `projea_attribute_int` int(11) DEFAULT NULL,
  `projea_actor_created_by` bigint(20) NOT NULL,
  `projea_create_date` datetime NOT NULL,
  `projea_actor_modified_by` bigint(20) DEFAULT NULL,
  `projea_modified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`projea_id`),
  UNIQUE KEY `proja_id_uk_ind` (`projea_id`) USING BTREE,
  KEY `proja_ids_ind` (`projea_projet_id`,`projea_id`,`projea_lkuvlu_attribute_id`) USING BTREE,
  KEY `projea_projet_id_fk_ind` (`projea_projet_id`) USING BTREE,
  KEY `projea_lkuvlu_attribute_id_fk_ind` (`projea_lkuvlu_attribute_id`) USING BTREE,
  KEY `projea_actor_created_by_fk_ind` (`projea_actor_created_by`) USING BTREE,
  KEY `projea_actor_modified_by_fk_ind` (`projea_actor_modified_by`) USING BTREE,
  KEY `projea_create_date_ind` (`projea_create_date`) USING BTREE,
  KEY `projea_modified_date_ind` (`projea_modified_date`) USING BTREE,
  CONSTRAINT `projea_actor_created_by_fk` FOREIGN KEY (`projea_actor_created_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `projea_actor_modified_by_fk` FOREIGN KEY (`projea_actor_modified_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `projea_lkuvlu_attribute_id_fk` FOREIGN KEY (`projea_lkuvlu_attribute_id`) REFERENCES `lookup_value` (`lkuvlu_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `projea_projet_id_fk` FOREIGN KEY (`projea_projet_id`) REFERENCES `project` (`projet_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER pa_modify_date_trg BEFORE UPDATE ON ifx_projects.project_attribute  
  FOR EACH ROW BEGIN SET NEW.projea_modified_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;


--
-- Table structure for table `sample`
--

DROP TABLE IF EXISTS `sample`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sample` (
  `sample_id` bigint(20) NOT NULL,
  `sample_projet_id` bigint(20) NOT NULL,
  `sample_name` varchar(255) NOT NULL,
  `sample_created_by` bigint(20) NOT NULL,
  `sample_create_date` datetime NOT NULL,
  `sample_modified_by` bigint(20) DEFAULT NULL,
  `sample_modified_date` datetime DEFAULT NULL,
  `sample_is_public` bit(1) NOT NULL,
  `sample_sample_parent_id` bigint(20) DEFAULT NULL,
  `sample_level` int(11) DEFAULT NULL,
  PRIMARY KEY (`sample_id`),
  UNIQUE KEY `sample_id_uk_ind` (`sample_id`) USING BTREE,
  KEY `sample_information_ind` (`sample_projet_id`,`sample_id`,`sample_name`,`sample_created_by`,`sample_create_date`) USING BTREE,
  KEY `sample_projet_id_fk_ind` (`sample_projet_id`) USING BTREE,
  KEY `sample_created_by_fk_ind` (`sample_created_by`) USING BTREE,
  KEY `sample_modified_by_fk_ind` (`sample_modified_by`) USING BTREE,
  KEY `sample_create_date_ind` (`sample_create_date`) USING BTREE,
  KEY `sample_modified_date_ind` (`sample_modified_date`) USING BTREE,
  KEY `sample_is_public_ind` (`sample_is_public`),
  CONSTRAINT `sample_created_by_fk` FOREIGN KEY (`sample_created_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sample_modified_by_fk` FOREIGN KEY (`sample_modified_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sample_projet_id_fk` FOREIGN KEY (`sample_projet_id`) REFERENCES `project` (`projet_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER sample_modify_date_trg BEFORE UPDATE ON ifx_projects.sample  
  FOR EACH ROW BEGIN SET NEW.sample_modified_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`ifx_projects_adm`@`%`*/ /*!50003 TRIGGER sample_cascade_update AFTER UPDATE ON ifx_projects.sample FOR EACH ROW 
  BEGIN
    UPDATE ifx_projects.sample_attribute AS sa SET sa.sampla_modified_date=CURRENT_TIMESTAMP
      WHERE sa.sampla_sample_id=NEW.sample_id;
    UPDATE ifx_projects.sample_meta_attribute AS sma set sma.sampma_modified_date=CURRENT_TIMESTAMP
      WHERE sma.sampma_projet_id=NEW.sample_projet_id;
    UPDATE ifx_projects.event AS e SET e.event_modified_date=CURRENT_TIMESTAMP
      WHERE e.event_projet_id=NEW.sample_projet_id AND e.event_sampl_id=NEW.sample_id;
    UPDATE ifx_projects.event_attribute AS ea SET ea.eventa_modified_date=CURRENT_TIMESTAMP
      WHERE ea.eventa_event_id in (SELECT e.event_id FROM event e WHERE e.event_projet_id=NEW.sample_projet_id AND e.event_sampl_id=NEW.sample_id);
    UPDATE ifx_projects.event_meta_attribute AS ema SET ema.evenma_modified_date=CURRENT_TIMESTAMP
      WHERE ema.evenma_projet_id=NEW.sample_projet_id;
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;


--
-- Table structure for table `sample_meta_attribute`
--

DROP TABLE IF EXISTS `sample_meta_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sample_meta_attribute` (
  `sampma_id` bigint(20) NOT NULL,
  `sampma_projet_id` bigint(20) NOT NULL,
  `sampma_lkuvlu_attribute_id` bigint(20) NOT NULL,
  `sampma_is_required` int(1) NOT NULL,
  `sampma_options` varchar(4000) DEFAULT NULL,
  `sampma_attribute_desc` varchar(1000) DEFAULT NULL,
  `sampma_actor_created_by` bigint(20) DEFAULT NULL,
  `sampma_actor_modified_by` bigint(20) DEFAULT NULL,
  `sampma_create_date` datetime DEFAULT NULL,
  `sampma_modified_date` datetime DEFAULT NULL,
  `sampma_is_active` int(1) DEFAULT NULL,
  `sampma_label` varchar(100) DEFAULT NULL,
  `sampma_ontology` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`sampma_id`),
  UNIQUE KEY `sampma_id_uk_ind` (`sampma_id`) USING BTREE,
  UNIQUE KEY `sampma_attribute_list_uk` (`sampma_projet_id`,`sampma_lkuvlu_attribute_id`),
  KEY `sampma_project_id_fk_ind` (`sampma_projet_id`) USING BTREE,
  KEY `sampma_actor_created_by_fk_ind` (`sampma_actor_created_by`) USING BTREE,
  KEY `sampma_actor_modified_by_fk_ind` (`sampma_actor_modified_by`) USING BTREE,
  KEY `sampma_lkuvlu_attribute_id_fk_ind` (`sampma_lkuvlu_attribute_id`) USING BTREE,
  KEY `sampma_ids_ind` (`sampma_id`,`sampma_projet_id`,`sampma_lkuvlu_attribute_id`,`sampma_actor_created_by`,`sampma_create_date`,`sampma_options`(767)) USING BTREE,
  KEY `sampma_ontology` (`sampma_ontology`),
  CONSTRAINT `sampma_actor_created_by_fk` FOREIGN KEY (`sampma_actor_created_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sampma_actor_modified_by_fk` FOREIGN KEY (`sampma_actor_modified_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sampma_lkuvlu_attribute_id_fk` FOREIGN KEY (`sampma_lkuvlu_attribute_id`) REFERENCES `lookup_value` (`lkuvlu_id`),
  CONSTRAINT `sampma_project_id_fk` FOREIGN KEY (`sampma_projet_id`) REFERENCES `project` (`projet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER sma_modify_date_trg BEFORE UPDATE ON ifx_projects.sample_meta_attribute  
  FOR EACH ROW BEGIN SET NEW.sampma_modified_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Dumping routines for database 'ifx_projects'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

--
-- Table structure for table `sample_attribute`
--

DROP TABLE IF EXISTS `sample_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sample_attribute` (
  `sampla_id` bigint(20) NOT NULL,
  `sampla_projet_id` bigint(20) NOT NULL,
  `sampla_lkuvlu_attribute_id` bigint(20) NOT NULL,
  `sampla_sample_id` bigint(20) NOT NULL,
  `sampla_attribute_date` datetime DEFAULT NULL,
  `sampla_attribute_float` float DEFAULT NULL,
  `sampla_attribute_str` varchar(4000) DEFAULT NULL,
  `sampla_attribute_int` int(11) DEFAULT NULL,
  `sampla_actor_created_by` bigint(20) NOT NULL,
  `sampla_actor_modified_by` bigint(20) DEFAULT NULL,
  `sampla_create_date` datetime NOT NULL,
  `sampla_modified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`sampla_id`),
  UNIQUE KEY `sampla_id_uk_ind` (`sampla_id`) USING BTREE,
  UNIQUE KEY `sampla_sa_unique_ind` (`sampla_projet_id`,`sampla_sample_id`,`sampla_lkuvlu_attribute_id`),
  KEY `sampla_information_ind` (`sampla_projet_id`,`sampla_id`,`sampla_lkuvlu_attribute_id`,`sampla_sample_id`,`sampla_attribute_date`,`sampla_attribute_float`,`sampla_attribute_str`(767),`sampla_actor_created_by`,`sampla_create_date`) USING BTREE,
  KEY `sampla_projet_id_fk` (`sampla_projet_id`) USING BTREE,
  KEY `sampla_lkuvlu_attribute_id_fk_ind` (`sampla_lkuvlu_attribute_id`) USING BTREE,
  KEY `sampla_sample_id_fk_ind` (`sampla_sample_id`) USING BTREE,
  KEY `sampla_actor_created_by_fk` (`sampla_actor_created_by`) USING BTREE,
  KEY `sampla_actor_modified_by_fk` (`sampla_actor_modified_by`) USING BTREE,
  KEY `sampla_create_date_ind` (`sampla_create_date`) USING BTREE,
  KEY `sampla_modified_date_ind` (`sampla_modified_date`) USING BTREE,
  CONSTRAINT `sampla_actor_created_by_fk` FOREIGN KEY (`sampla_actor_created_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sampla_actor_modified_by_fk` FOREIGN KEY (`sampla_actor_modified_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sampla_lkuvlu_attribute_id_fk` FOREIGN KEY (`sampla_lkuvlu_attribute_id`) REFERENCES `lookup_value` (`lkuvlu_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sampla_projet_id_fk` FOREIGN KEY (`sampla_projet_id`) REFERENCES `project` (`projet_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sampla_sample_id_fk` FOREIGN KEY (`sampla_sample_id`) REFERENCES `sample` (`sample_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER sa_modify_date_trg BEFORE UPDATE ON ifx_projects.sample_attribute  
  FOR EACH ROW BEGIN SET NEW.sampla_modified_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;



--
-- Table structure for table `event`
--

DROP TABLE IF EXISTS `event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event` (
  `event_id` bigint(20) NOT NULL,
  `event_projet_id` bigint(20) NOT NULL,
  `event_type_lkuvl_id` bigint(20) DEFAULT NULL,
  `event_actor_created_by` bigint(20) NOT NULL,
  `event_create_date` datetime NOT NULL,
  `event_actor_modified_by` bigint(20) DEFAULT NULL,
  `event_modified_date` datetime DEFAULT NULL,
  `event_status_lkuvl_id` bigint(20) DEFAULT NULL,
  `event_sampl_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`event_id`),
  UNIQUE KEY `event_id_ind` (`event_id`) USING BTREE,
  KEY `event_sampl_id_fk_ind` (`event_sampl_id`) USING BTREE,
  KEY `event_ids_ind` (`event_id`,`event_projet_id`,`event_actor_created_by`,`event_actor_modified_by`,`event_type_lkuvl_id`,`event_status_lkuvl_id`) USING BTREE,
  KEY `event_projet_id_fk_ind` (`event_projet_id`) USING BTREE,
  KEY `event_type_lkuvl_id_fk_ind` (`event_type_lkuvl_id`) USING BTREE,
  KEY `event_actor_created_by_fk_ind` (`event_actor_created_by`) USING BTREE,
  KEY `event_actor_modified_by_fk_ind` (`event_actor_modified_by`) USING BTREE,
  KEY `event_status_lkuvl_id_fk_ind` (`event_status_lkuvl_id`) USING BTREE,
  CONSTRAINT `event_actor_created_by_fk` FOREIGN KEY (`event_actor_created_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `event_actor_modified_by_fk` FOREIGN KEY (`event_actor_modified_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `event_projet_id_fk` FOREIGN KEY (`event_projet_id`) REFERENCES `project` (`projet_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `event_sampl_id_fk` FOREIGN KEY (`event_sampl_id`) REFERENCES `sample` (`sample_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `event_status_lkuvl_id_fk` FOREIGN KEY (`event_status_lkuvl_id`) REFERENCES `lookup_value` (`lkuvlu_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `event_type_lkuvl_id_fk` FOREIGN KEY (`event_type_lkuvl_id`) REFERENCES `lookup_value` (`lkuvlu_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER event_modify_date_trg BEFORE UPDATE ON ifx_projects.event  
  FOR EACH ROW BEGIN SET NEW.event_modified_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;


--
-- Table structure for table `event_meta_attribute`
--

DROP TABLE IF EXISTS `event_meta_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_meta_attribute` (
  `evenma_id` bigint(20) NOT NULL,
  `evenma_projet_id` bigint(20) NOT NULL,
  `evenma_event_type_lkuvl_id` bigint(20) NOT NULL,
  `evenma_lkuvlu_attribute_id` bigint(20) NOT NULL,
  `evenma_is_required` int(1) NOT NULL,
  `evenma_desc` varchar(1000) DEFAULT NULL,
  `evenma_options` varchar(1000) DEFAULT NULL,
  `evenma_create_date` datetime NOT NULL,
  `evenma_actor_created_by` bigint(20) NOT NULL,
  `evenma_actor_modified_by` bigint(20) DEFAULT NULL,
  `evenma_is_active` int(1) DEFAULT NULL,
  `evenma_modified_date` datetime DEFAULT NULL,
  `evenma_is_sample_required` int(1) DEFAULT '0',
  `evenma_label` varchar(100) DEFAULT NULL,
  `evenma_ontology` varchar(100) DEFAULT NULL,
  `evenma_order` int(2) DEFAULT NULL,
  PRIMARY KEY (`evenma_id`),
  UNIQUE KEY `evenma_id_ind` (`evenma_id`) USING BTREE,
  UNIQUE KEY `evenma_attribute_list_uk` (`evenma_projet_id`,`evenma_event_type_lkuvl_id`,`evenma_lkuvlu_attribute_id`),
  KEY `evenma_attribute_ids_ind` (`evenma_id`,`evenma_lkuvlu_attribute_id`) USING BTREE,
  KEY `evenma_lkuvlu_attribute_id_fk_ind` (`evenma_lkuvlu_attribute_id`) USING BTREE,
  KEY `evenma_lkuvlu_event_type_fk_ind` (`evenma_event_type_lkuvl_id`) USING BTREE,
  KEY `evenma_project_id_fk_ind` (`evenma_projet_id`) USING BTREE,
  KEY `evenma_actor_created_by_fk_ind` (`evenma_actor_created_by`) USING BTREE,
  KEY `evenma_actor_modified_by_fk_ind` (`evenma_actor_modified_by`) USING BTREE,
  KEY `evenma_ontology` (`evenma_ontology`),
  CONSTRAINT `evenma_actor_created_by_fk` FOREIGN KEY (`evenma_actor_created_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `evenma_actor_modified_by_fk` FOREIGN KEY (`evenma_actor_modified_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `evenma_lkuvlu_attribute_id_fk` FOREIGN KEY (`evenma_lkuvlu_attribute_id`) REFERENCES `lookup_value` (`lkuvlu_id`),
  CONSTRAINT `evenma_lkuvlu_event_type_fk` FOREIGN KEY (`evenma_event_type_lkuvl_id`) REFERENCES `lookup_value` (`lkuvlu_id`),
  CONSTRAINT `evenma_project_id_fk` FOREIGN KEY (`evenma_projet_id`) REFERENCES `project` (`projet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER ema_modify_date_trg BEFORE UPDATE ON ifx_projects.event_meta_attribute  
  FOR EACH ROW BEGIN SET NEW.evenma_modified_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;



--
-- Table structure for table `event_attribute`
--

DROP TABLE IF EXISTS `event_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_attribute` (
  `eventa_id` bigint(20) NOT NULL,
  `eventa_lkuvlu_attribute_id` bigint(20) NOT NULL,
  `eventa_event_id` bigint(20) NOT NULL,
  `eventa_attribute_date` datetime DEFAULT NULL,
  `eventa_attribute_float` float DEFAULT NULL,
  `eventa_attribute_str` varchar(4000) DEFAULT NULL,
  `eventa_attribute_int` int(11) DEFAULT NULL,
  `eventa_actor_created_by` bigint(20) DEFAULT NULL,
  `eventa_actor_modified_by` bigint(20) DEFAULT NULL,
  `eventa_create_date` datetime DEFAULT NULL,
  `eventa_modified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`eventa_id`),
  KEY `eventa_ids_ind` (`eventa_id`,`eventa_lkuvlu_attribute_id`,`eventa_event_id`,`eventa_actor_created_by`) USING BTREE,
  KEY `eventa_event_id_fk_ind` (`eventa_event_id`) USING BTREE,
  KEY `eventa_lkuvlu_attribute_id_fk_ind` (`eventa_lkuvlu_attribute_id`) USING BTREE,
  KEY `eventa_actor_created_by_fk_ind` (`eventa_actor_created_by`) USING BTREE,
  KEY `eventa_actor_modified_by_fk_ind` (`eventa_actor_modified_by`) USING BTREE,
  CONSTRAINT `eventa_actor_created_by_fk` FOREIGN KEY (`eventa_actor_created_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `eventa_actor_modified_by_fk` FOREIGN KEY (`eventa_actor_modified_by`) REFERENCES `actor` (`actor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `eventa_event_id_fk` FOREIGN KEY (`eventa_event_id`) REFERENCES `event` (`event_id`),
  CONSTRAINT `eventa_lkuvlu_attribute_id_fk` FOREIGN KEY (`eventa_lkuvlu_attribute_id`) REFERENCES `lookup_value` (`lkuvlu_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`hkim`@`%`*/ /*!50003 TRIGGER ea_modify_date_trg BEFORE UPDATE ON ifx_projects.event_attribute  
  FOR EACH ROW BEGIN SET NEW.eventa_modified_date = CURRENT_TIMESTAMP;   
END */ ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;



/*
  DEFAULT LOOKUP VALUES AND ACTOR GROUPS
*/

INSERT INTO `lookup_value` (`lkuvlu_id`, `lkuvlu_name`, `lkuvlu_type`, `lkuvlu_data_type`, `lkuvlu_create_date`, `lkuvlu_modify_date`) VALUES
(1111111111111, 'General-Edit', 'Edit Group', 'string', '2014-05-22 14:44:50', NULL),
(1111111111112, 'General-View', 'Access Group', 'string', '2014-05-22 14:44:50', NULL),
(1111111111113, 'General-Admin', 'Access Group', 'string', '2014-05-22 14:44:50', NULL),
(1111111111114, 'Active', 'Event Status', 'string', '2011-02-08 00:00:00', NULL),
(1111111111115, 'Inactive', 'Event Status', 'string', '2011-02-08 00:00:00', NULL),
(1135914704994, 'Complete', 'Attribute', 'int', '2014-05-28 13:05:23', NULL),
(1135914704999, 'Grant', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705004, 'ProjectURL', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705009, 'Category', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705014, 'Project Group', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705019, 'Project Leader', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705024, 'Project Name', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705029, 'Project Status', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705034, 'Project Code', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705039, 'Anticipated final destination', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705044, 'Genus', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705049, 'Species', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705054, 'Strain', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705059, 'Sample Status', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705064, 'Organism', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705069, 'Taxonomy ID', 'Attribute', 'int', '2014-05-28 13:05:23', NULL),
(1135914705074, 'Project ID', 'Attribute', 'int', '2014-05-28 13:05:23', NULL),
(1135914705079, 'Isolate Repository Accession', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705084, 'Superkingdom', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705089, 'Isolate Repository Status', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705094, 'Sample Status Comment', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705099, 'Isolate Repository Type', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705104, 'Kingdom', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705109, 'Phylum', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705114, 'Order', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705119, 'Family', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705124, 'Class', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705129, 'annotation accession', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705134, 'annotation date', 'Attribute', 'date', '2014-05-28 13:05:23', NULL),
(1135914705139, 'annotation status', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705144, 'sra accession', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705149, 'sra date', 'Attribute', 'date', '2014-05-28 13:05:23', NULL),
(1135914705154, 'sra status', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705159, 'wgs accession', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705164, 'wgs date', 'Attribute', 'date', '2014-05-28 13:05:23', NULL),
(1135914705169, 'wgs status', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705174, 'dbSNP accession', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705179, 'dbSNP date', 'Attribute', 'date', '2014-05-28 13:05:23', NULL),
(1135914705184, 'dbSNP status', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705189, 'SNP FTP', 'Attribute', 'string', '2014-05-28 13:05:23', NULL),
(1135914705194, 'ProjectRegistration', 'Event Type', 'string', '2014-05-28 13:05:23', NULL),
(1135914705199, 'SampleRegistration', 'Event Type', 'string', '2014-05-28 13:05:23', NULL),
(1135914705204, 'Annotation', 'Event Type', 'string', '2014-05-28 13:05:23', NULL),
(1135914705209, 'Short Read Archive', 'Event Type', 'string', '2014-05-28 13:05:23', NULL),
(1135914705214, 'WGS', 'Event Type', 'string', '2014-05-28 13:05:23', NULL),
(1135914705219, 'SampleUpdate', 'Event Type', 'string', '2014-05-28 13:05:23', NULL),
(1135914705224, 'ProjectUpdate', 'Event Type', 'string', '2014-05-28 13:05:23', NULL),
(1135914705229, 'dbSNP', 'Event Type', 'string', '2014-05-28 13:05:23', NULL);

INSERT INTO `groups` (`group_id`, `group_name_lkuvl_id`) VALUES
(1111111111118, 1111111111111),
(1111111111119, 1111111111112),
(1111111111120, 1111111111113);

INSERT INTO `actor` (`actor_username`, `actor_first_name`, `actor_last_name`, `actor_middle_name`, `actor_email_address`, `actor_id`, `actor_create_date`, `actor_modify_date`) VALUES
('testuser', 'test', 'test', 'user', 'test@test.com', 1111111111124, '2014-05-22 14:51:36', NULL);

INSERT INTO `actor_group` (`actgrp_id`, `actgrp_create_date`, `actgrp_modify_date`, `actgrp_actor_id`, `actgrp_group_id`) VALUES
(1111111111121, '2014-05-22 00:00:00', NULL, 1111111111124, 1111111111118),
(1111111111122, '2014-05-22 14:51:36', NULL, 1111111111124, 1111111111119),
(1111111111123, '2014-05-22 14:51:36', NULL, 1111111111124, 1111111111120);


INSERT INTO `project` (`projet_id`, `projet_name`, `projet_projet_parent_id`, `projet_create_date`, `projet_actor_created_by`, `projet_actor_modified_by`, `projet_modified_date`, `projet_level`, `projet_is_public`, `projet_view_group_id`, `projet_edit_group_id`, `projet_is_secure`) VALUES
(1135914704034, 'testProject', NULL, '2014-05-22 14:54:08', 1111111111124, NULL, NULL, 1, 1, 1111111111119, 1111111111118, 0);

INSERT INTO `event_meta_attribute` (`evenma_id`, `evenma_projet_id`, `evenma_event_type_lkuvl_id`, `evenma_lkuvlu_attribute_id`, `evenma_is_required`, `evenma_desc`, `evenma_options`, `evenma_create_date`, `evenma_actor_created_by`, `evenma_actor_modified_by`, `evenma_is_active`, `evenma_modified_date`, `evenma_is_sample_required`, `evenma_label`, `evenma_ontology`, `evenma_order`)
VALUES
  (1135914705419, 1135914704034, 1135914705194, 1135914705004, 0, 'Project Page URL', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705424, 1135914704034, 1135914705194, 1135914705009, 0, 'NIAID Category', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705429, 1135914704034, 1135914705194, 1135914705014, 0, 'Project group', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705434, 1135914704034, 1135914705194, 1135914705019, 1, 'Name of person leading project', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705439, 1135914704034, 1135914705194, 1135914705024, 1, 'Full project name', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705444, 1135914704034, 1135914705194, 1135914705029, 1, 'Current Status of Project', 'Ongoing;Completed;Deprecated', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705449, 1135914704034, 1135914705194, 1135914705034, 1, 'Internal Project or Grant Code', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705454, 1135914704034, 1135914705194, 1135914705039, 0, 'Data repositories upon which the project data will be publicly available', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705459, 1135914704034, 1135914705199, 1135914705044, 0, 'Genus Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705464, 1135914704034, 1135914705199, 1135914705049, 0, 'Species Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705469, 1135914704034, 1135914705199, 1135914705054, 0, 'Strain Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705474, 1135914704034, 1135914705199, 1135914705059, 1, 'Sample Status', 'Waiting for sample;Received;Sequencing;Analysis;Closure;Completed;Deprecated', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705479, 1135914704034, 1135914705199, 1135914705064, 1, 'Full organism name', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705484, 1135914704034, 1135914705199, 1135914705069, 0, 'NCBI Taxon ID', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705489, 1135914704034, 1135914705199, 1135914705074, 0, 'NCBI Project ID', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705494, 1135914704034, 1135914705199, 1135914705079, 0, 'Isolate accession number', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705499, 1135914704034, 1135914705199, 1135914705084, 0, 'Superkingdom Classifcation', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705504, 1135914704034, 1135914705199, 1135914705089, 0, 'Status of DNA respository registration', 'Registered;Pending;Not Required', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705509, 1135914704034, 1135914705199, 1135914705094, 0, 'Comment of Sample Status', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705514, 1135914704034, 1135914705199, 1135914705099, 0, 'Repository Type', 'BEI;BCCM;STEC;NARSA;NCPF;FGSC;OTHER', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705519, 1135914704034, 1135914705199, 1135914705104, 0, 'Kingdom Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705524, 1135914704034, 1135914705199, 1135914705109, 0, 'Phylum Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705529, 1135914704034, 1135914705199, 1135914705114, 0, 'Order Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705534, 1135914704034, 1135914705199, 1135914705119, 0, 'Family Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705539, 1135914704034, 1135914705199, 1135914705124, 0, 'Class Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705544, 1135914704034, 1135914705204, 1135914705129, 0, 'Annotation accession ID start:end', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705549, 1135914704034, 1135914705204, 1135914705134, 1, 'Annotation event date', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705554, 1135914704034, 1135914705204, 1135914705139, 1, 'Annotation event status', 'Pending;Submitted;Published;Not required', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705559, 1135914704034, 1135914705209, 1135914705144, 0, 'As generated by SRA', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705564, 1135914704034, 1135914705209, 1135914705149, 0, 'SRA event date', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705569, 1135914704034, 1135914705209, 1135914705154, 0, 'SRA event status', 'Submitted;Published;Not Required', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705574, 1135914704034, 1135914705214, 1135914705159, 0, 'WGS accession ID', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705579, 1135914704034, 1135914705214, 1135914705164, 1, 'WGS event date', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705584, 1135914704034, 1135914705214, 1135914705169, 1, 'WGS event status', 'Pending;Submitted;Published;Not required', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705589, 1135914704034, 1135914705219, 1135914705044, 0, 'Genus Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705594, 1135914704034, 1135914705219, 1135914705049, 0, 'Species Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705599, 1135914704034, 1135914705219, 1135914705054, 0, 'Strain Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705604, 1135914704034, 1135914705219, 1135914705059, 0, 'Sample Status', 'Waiting for sample;Received;Sequencing;Analysis;Closure;Completed;Deprecated', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705609, 1135914704034, 1135914705219, 1135914705064, 0, 'Full organism name', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705614, 1135914704034, 1135914705219, 1135914705069, 0, 'NCBI Taxon ID', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705619, 1135914704034, 1135914705219, 1135914705074, 0, 'NCBI Project ID', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705624, 1135914704034, 1135914705219, 1135914705169, 0, 'wgs status', 'Pending;Submitted;Published;Not required', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705629, 1135914704034, 1135914705219, 1135914705079, 0, 'Isolate accession number', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705634, 1135914704034, 1135914705219, 1135914705084, 0, 'Superkingdom Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705639, 1135914704034, 1135914705219, 1135914705089, 0, 'Status of DNA respository registration', 'Registered;Pending;Not Required', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705644, 1135914704034, 1135914705219, 1135914705094, 0, 'Comment of Sample Status', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705649, 1135914704034, 1135914705219, 1135914705099, 0, 'Repository Type', 'BEI;BCCM;STEC;NARSA;NCPF;FGSC;OTHER', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705654, 1135914704034, 1135914705219, 1135914705104, 0, 'Kingdom Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705659, 1135914704034, 1135914705219, 1135914705109, 0, 'Phylum Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705664, 1135914704034, 1135914705219, 1135914705114, 0, 'Order Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705669, 1135914704034, 1135914705219, 1135914705119, 0, 'Family Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705674, 1135914704034, 1135914705219, 1135914705124, 0, 'Class Classification', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705679, 1135914704034, 1135914705224, 1135914705009, 0, 'NIAID Category', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705684, 1135914704034, 1135914705224, 1135914705014, 0, 'Project group', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705689, 1135914704034, 1135914705224, 1135914705019, 0, 'Name of person leading project', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705694, 1135914704034, 1135914705224, 1135914705024, 0, 'Full project name', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705699, 1135914704034, 1135914705224, 1135914705029, 0, 'Current Status of Project', 'Ongoing;Completed;Deprecated', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705704, 1135914704034, 1135914705224, 1135914705034, 0, 'Internal Project or Grant Code', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705709, 1135914704034, 1135914705224, 1135914705039, 0, 'Data repositories upon which the project data will be publicly available', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 0, NULL, NULL, NULL),
  (1135914705714, 1135914704034, 1135914705229, 1135914705174, 0, 'dbSNP assigned accession', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705719, 1135914704034, 1135914705229, 1135914705179, 0, 'dbSNP submission date', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705724, 1135914704034, 1135914705229, 1135914705184, 0, 'dbSNP submission status', 'Pending;Submitted;Published;Not required', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL),
  (1135914705729, 1135914704034, 1135914705229, 1135914705189, 0, 'Other link to SNP analysis', '', '2014-05-28 13:05:23', 1111111111124, NULL, 1, NULL, 1, NULL, NULL, NULL);

INSERT INTO `project_meta_attribute` (`projma_id`, `projma_projet_id`, `projma_lkuvlu_attribute_id`, `projma_is_required`, `projma_options`, `projma_attribute_desc`, `projma_actor_created_by`, `projma_create_date`, `projma_modified_date`, `projma_actor_modified_by`, `projma_is_active`, `projma_label`, `projma_ontology`)
VALUES
  (1135914705239, 1135914704034, 1135914704994, 1, '0;1', 'Whether project is complete', 1111111111124, '2014-05-28 13:05:23', NULL, NULL, 1, NULL, NULL),
  (1135914705244, 1135914704034, 1135914705029, 1, 'Ongoing;Completed;Deprecated', 'Current Status of Project', 1111111111124, '2014-05-28 13:05:23', NULL, NULL, 1, NULL, NULL),
  (1135914705249, 1135914704034, 1135914705004, 0, '', 'Project Page URL', 1111111111124, '2014-05-28 13:05:23', NULL, NULL, 1, NULL, NULL),
  (1135914705254, 1135914704034, 1135914705009, 0, '', 'NIAID Category', 1111111111124, '2014-05-28 13:05:23', NULL, NULL, 1, NULL, NULL),
  (1135914705259, 1135914704034, 1135914704999, 1, '', 'Grant identifier.', 1111111111124, '2014-05-28 13:05:23', NULL, NULL, 1, NULL, NULL),
  (1135914705264, 1135914704034, 1135914705014, 0, '', 'Project group', 1111111111124, '2014-05-28 13:05:23', NULL, NULL, 1, NULL, NULL),
  (1135914705269, 1135914704034, 1135914705019, 1, '', 'Name of person leading project', 1111111111124, '2014-05-28 13:05:23', NULL, NULL, 1, NULL, NULL),
  (1135914705274, 1135914704034, 1135914705024, 1, '', 'Full project name', 1111111111124, '2014-05-28 13:05:23', NULL, NULL, 1, NULL, NULL),
  (1135914705279, 1135914704034, 1135914705034, 1, '', 'Internal Project or Grant Code', 1111111111124, '2014-05-28 13:05:23', NULL, NULL, 1, NULL, NULL),
  (1135914705284, 1135914704034, 1135914705039, 0, '', 'Data repositories upon which the project data will be publicly available', 1111111111124, '2014-05-28 13:05:23', NULL, NULL, 1, NULL, NULL);


INSERT INTO `sample_meta_attribute` (`sampma_id`, `sampma_projet_id`, `sampma_lkuvlu_attribute_id`, `sampma_is_required`, `sampma_options`, `sampma_attribute_desc`, `sampma_actor_created_by`, `sampma_actor_modified_by`, `sampma_create_date`, `sampma_modified_date`, `sampma_is_active`, `sampma_label`, `sampma_ontology`)
VALUES
  (1135914705289, 1135914704034, 1135914705044, 0, '', 'Genus Classification', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705294, 1135914704034, 1135914705049, 0, '', 'Species Classification', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705299, 1135914704034, 1135914705054, 0, '', 'Strain Classification', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705304, 1135914704034, 1135914705059, 0, 'Waiting for sample;Received;Sequencing;Analysis;Closure;Completed;Deprecated', 'Sample Status', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705309, 1135914704034, 1135914705064, 0, '', 'Full organism name', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705314, 1135914704034, 1135914705069, 0, '', 'NCBI Taxon ID', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705319, 1135914704034, 1135914705074, 0, '', 'NCBI Project ID', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705324, 1135914704034, 1135914705134, 0, '', 'annotation date', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705329, 1135914704034, 1135914705139, 0, 'Pending;Submitted;Published;Not required', 'annotation status', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705334, 1135914704034, 1135914705149, 0, '', 'sra date', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705339, 1135914704034, 1135914705154, 0, 'Submitted;Published;Not required', 'sra status', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705344, 1135914704034, 1135914705164, 0, '', 'wgs date', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705349, 1135914704034, 1135914705169, 0, 'Pending;Submitted;Published;Not required', 'wgs status', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705354, 1135914704034, 1135914705079, 0, '', 'Isolate accession number', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705359, 1135914704034, 1135914705084, 0, '', 'Superkingdom Classification', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705364, 1135914704034, 1135914705089, 0, 'Registered;Pending;Not Required', 'Status of DNA respository registration', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705369, 1135914704034, 1135914705094, 0, '', 'Comment of Sample Status', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705374, 1135914704034, 1135914705179, 0, '', 'dbSNP date', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705379, 1135914704034, 1135914705184, 0, 'Pending;Submitted;Published;Not required', 'dbSNP status', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705384, 1135914704034, 1135914705099, 0, 'BEI;BCCM;STEC;NARSA;NCPF;FGSC;OTHER', 'Repository Type', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705389, 1135914704034, 1135914705189, 0, '', 'Other link to SNP analysis', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705394, 1135914704034, 1135914705104, 0, '', 'Kingdom Classification', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705399, 1135914704034, 1135914705109, 0, '', 'Phylum Classification', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705404, 1135914704034, 1135914705114, 0, '', 'Order Classification', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705409, 1135914704034, 1135914705119, 0, '', 'Family Classification', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL),
  (1135914705414, 1135914704034, 1135914705124, 0, '', 'Class Classification', 1111111111124, NULL, '2014-05-28 13:05:23', NULL, 1, NULL, NULL);

