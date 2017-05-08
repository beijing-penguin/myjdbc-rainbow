# Host: 127.0.0.1  (Version 5.7.11)
# Date: 2017-05-08 11:38:18
# Generator: MySQL-Front 6.0  (Build 1.148)


#
# Structure for table "user"
#
CREATE DATABASE `myjdbc_test` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;
use myjdbc_test;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `real_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `sex` int(5) NOT NULL DEFAULT '0',
  `age` int(1) DEFAULT '0',
  `create_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `real_name` (`real_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

#
# Data for table "user"
#

INSERT INTO `user` VALUES (3,'dc',1,22,'2017-05-08 11:47:19'),(4,'test2',1,22,'2017-05-08 11:47:38'),(5,'test3',1,22,'2017-05-08 11:47:48'),(6,'test4',1,22,'2017-05-08 11:47:58'),(7,'test5',1,22,'2017-05-08 11:48:07');
