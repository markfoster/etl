-- MySQL dump 10.13  Distrib 5.5.20, for Linux (x86_64)
--
-- Host: 10.38.1.65    Database: preview_pp
-- ------------------------------------------------------
-- Server version	5.5.25-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chapter`
--

DROP TABLE IF EXISTS `chapter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chapter` (
  `provider_id` varchar(50) NOT NULL,
  `location_id` varchar(50) NOT NULL,
  `chapter_number` varchar(5) NOT NULL,
  `score` int(11) DEFAULT NULL,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`location_id`,`chapter_number`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `location_condition`
--

DROP TABLE IF EXISTS `location_condition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_condition` (
  `provider_id` varchar(50) NOT NULL,
  `location_id` varchar(50) NOT NULL,
  `condition_id` varchar(50) NOT NULL,
  `regulated_activity_number` varchar(10) NOT NULL,
  `type` char(1) DEFAULT NULL,
  `text` longtext,
  `reason` longtext,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`location_id`,`condition_id`,`regulated_activity_number`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `location_regulated_activity`
--

DROP TABLE IF EXISTS `location_regulated_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_regulated_activity` (
  `provider_id` varchar(50) NOT NULL,
  `location_id` varchar(50) NOT NULL,
  `regulated_activity_number` varchar(10) NOT NULL,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`location_id`,`regulated_activity_number`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lookup`
--

DROP TABLE IF EXISTS `lookup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lookup` (
  `provider_id` varchar(50) NOT NULL,
  `location_id` varchar(50) NOT NULL,
  `nid` int(11) NOT NULL,
  `last_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`provider_id`,`location_id`,`nid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nominated_individual`
--

DROP TABLE IF EXISTS `nominated_individual`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nominated_individual` (
  `provider_id` varchar(50) NOT NULL,
  `regulated_activity_number` varchar(10) NOT NULL,
  `name` varchar(115) DEFAULT NULL,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`regulated_activity_number`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `outcome`
--

DROP TABLE IF EXISTS `outcome`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `outcome` (
  `provider_id` varchar(50) NOT NULL,
  `location_id` varchar(50) NOT NULL,
  `outcome_number` varchar(30) NOT NULL,
  `report_publication_date` varchar(20) NOT NULL,
  `review_reason` varchar(255) DEFAULT NULL,
  `user_experience` longtext,
  `other_evidence` longtext,
  `outcome_statement_id` varchar(30) DEFAULT NULL,
  `judgement_statement_id` varchar(30) DEFAULT NULL,
  `judgement_summary` longtext,
  `method` longtext,
  `inspection_theme` varchar(50) DEFAULT NULL,
  `judgement_reason` longtext,
  `compliance_level` varchar(50) DEFAULT NULL,
  `inspection_announced` char(1) DEFAULT NULL,
  `source` varchar(10) DEFAULT NULL,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`location_id`,`outcome_number`,`report_publication_date`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `partner`
--

DROP TABLE IF EXISTS `partner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `partner` (
  `provider_id` varchar(50) NOT NULL,
  `partner_id` varchar(50) NOT NULL,
  `name` varchar(115) DEFAULT NULL,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`partner_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `provider_condition`
--

DROP TABLE IF EXISTS `provider_condition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `provider_condition` (
  `provider_id` varchar(50) NOT NULL,
  `condition_id` varchar(50) NOT NULL,
  `regulated_activity_number` varchar(10) NOT NULL,
  `type` char(1) DEFAULT NULL,
  `text` longtext,
  `reason` longtext,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`condition_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `provider_regulated_activity`
--

DROP TABLE IF EXISTS `provider_regulated_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `provider_regulated_activity` (
  `provider_id` varchar(30) NOT NULL,
  `regulated_activity_number` varchar(10) NOT NULL,
  `action_code` varchar(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`regulated_activity_number`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='	';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registered_manager`
--

DROP TABLE IF EXISTS `registered_manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `registered_manager` (
  `provider_id` varchar(50) NOT NULL,
  `location_id` varchar(50) NOT NULL,
  `registered_manager_id` varchar(50) NOT NULL,
  `regulated_activity_number` varchar(10) NOT NULL,
  `name` varchar(115) DEFAULT NULL,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`location_id`,`registered_manager_id`,`regulated_activity_number`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registered_manager_condition`
--

DROP TABLE IF EXISTS `registered_manager_condition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `registered_manager_condition` (
  `provider_id` varchar(50) NOT NULL,
  `location_id` varchar(50) NOT NULL,
  `registered_manager_id` varchar(50) NOT NULL,
  `regulated_activity_number` varchar(10) NOT NULL,
  `condition_id` varchar(50) NOT NULL DEFAULT '',
  `text` longtext,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`location_id`,`registered_manager_id`,`regulated_activity_number`,`condition_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `report_summary`
--

DROP TABLE IF EXISTS `report_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `report_summary` (
  `provider_id` varchar(50) NOT NULL,
  `location_id` varchar(50) NOT NULL,
  `report_publication_date` varchar(20) NOT NULL,
  `text` longtext,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`location_id`,`report_publication_date`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_type`
--

DROP TABLE IF EXISTS `service_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_type` (
  `provider_id` varchar(30) NOT NULL,
  `location_id` varchar(30) NOT NULL,
  `service_type_id` varchar(30) NOT NULL,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`location_id`,`service_type_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_user_band`
--

DROP TABLE IF EXISTS `service_user_band`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_user_band` (
  `provider_id` varchar(50) NOT NULL,
  `location_id` varchar(50) NOT NULL,
  `service_user_band_id` varchar(50) NOT NULL,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`location_id`,`service_user_band_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `visit_date`
--

DROP TABLE IF EXISTS `visit_date`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visit_date` (
  `provider_id` varchar(50) NOT NULL,
  `location_id` varchar(50) NOT NULL,
  `outcome_number` varchar(50) NOT NULL,
  `report_publication_date` varchar(20) NOT NULL,
  `visit_date` varchar(20) NOT NULL,
  `action_code` char(1) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`provider_id`,`location_id`,`outcome_number`,`report_publication_date`,`visit_date`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-08-14 10:11:12
