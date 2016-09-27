/*
--Script to setup GUID database objects
--Script will create jcvi_guid database, and guid_admin and guid_client users with default password of welcome.
--It's highly recommended that adminstrator changes the passwords before using OMETA in production.
*/
-- script for GUID Server
Create database jcvi_guid;

/*-- Grants for 'guid_admin'@'%'*/
SET old_passwords = 0;
CREATE USER guid_admin IDENTIFIED BY 'welcome';

GRANT USAGE ON *.* TO 'guid_admin'@'%' WITH MAX_USER_CONNECTIONS 5; 
GRANT ALL PRIVILEGES ON `jcvi_guid`.* TO 'guid_admin'@'%' WITH GRANT OPTION;

-- Grants for 'guid_client'@'%'
CREATE USER guid_client IDENTIFIED BY 'welcome';

GRANT USAGE ON *.* TO 'guid_client'@'%' WITH MAX_USER_CONNECTIONS 50; 

use jcvi_guid;

/* */
/*-- MySQL dump 10.13  Distrib 5.6.16-64.2, for Linux (x86_64)*/
/*--*/
/*-- Host: mysql-lan-cms.jcvi.org    Database: jcvi_guid*/
/*-- ------------------------------------------------------*/
/*-- Server version	5.1.41-log*/
/* */
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*--*/
/*-- Table structure for table `guid_block_table`*/
/*--*/

DROP TABLE IF EXISTS `max_allocated_guid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `max_allocated_guid` (
  `maxid_value` decimal(20,0) unsigned NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guid_namespace_table`
--

DROP TABLE IF EXISTS `guid_namespace_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guid_namespace_table` (
  `gname_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `gname_namespace` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `gname_creation_comment` varchar(4000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `gname_create_date` datetime NOT NULL,
  `gname_created_by` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `gname_modify_date` datetime DEFAULT NULL,
  `gname_modified_by` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  PRIMARY KEY (`gname_id`) USING BTREE,
  UNIQUE KEY `gname_namespace_uk_ind` (`gname_namespace`)
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;



DROP TABLE IF EXISTS `guid_block_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guid_block_table` (
  `gblock_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `gblock_first_guid` decimal(20,0) unsigned NOT NULL,
  `gblock_last_guid` decimal(20,0) unsigned NOT NULL,
  `gblock_namespace_id` int(10) unsigned NOT NULL,
  `gblock_creation_comment` varchar(4000) DEFAULT NULL,
  `gblock_block_size` bigint(20) unsigned NOT NULL,
  `gblock_create_date` datetime NOT NULL,
  `gblock_created_by` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `gblock_modify_date` datetime DEFAULT NULL,
  `gblock_modified_by` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  PRIMARY KEY (`gblock_id`) USING BTREE,
  UNIQUE KEY `gblock_first_guid_uk_ind` (`gblock_first_guid`),
  UNIQUE KEY `gblock_last_guid_uk_ind` (`gblock_last_guid`),
  KEY `FK_guid_block_table_namespace_id` (`gblock_namespace_id`),
  CONSTRAINT `FK_guid_block_table_namespace_id` FOREIGN KEY (`gblock_namespace_id`) REFERENCES `guid_namespace_table` (`gname_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=47170 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'jcvi_guid'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-05-20 12:09:31


GRANT INSERT, SELECT ON `jcvi_guid`.`guid_block_table` TO 'guid_client'@'%'; 
GRANT INSERT, SELECT ON `jcvi_guid`.`guid_namespace_table` TO 'guid_client'@'%'; 
GRANT SELECT, UPDATE ON `jcvi_guid`.`max_allocated_guid` TO 'guid_client'@'%';

insert into max_allocated_guid (maxid_value) values (1135913703999);
insert into guid_namespace_table (gname_id, gname_namespace, gname_creation_comment, gname_create_date, gname_created_by, gname_modify_date, gname_modified_by) values (41, 'NOTIFICATION_SERVICES', 'Notification Set IDs for JTCWO interface', '2003-10-20 16:56:28', 'GUIDBLOCKSERVERCLIENT', null, null);
insert into guid_namespace_table (gname_id, gname_namespace, gname_creation_comment, gname_create_date, gname_created_by, gname_modify_date, gname_modified_by) values (43, 'GUID_SERVLET', 'general purpose HTTP servlet for GUID access', '2003-12-03 22:29:33', 'GUIDBLOCKSERVERCLIENT', null, null);
insert into guid_namespace_table (gname_id, gname_namespace, gname_creation_comment, gname_create_date, gname_created_by, gname_modify_date, gname_modified_by) values (82, 'GUID_DEFAULT', 'Default namespace if none specified', '2004-12-06 11:10:10', 'GUIDBLOCKSERVERCLIENT', null, null);
