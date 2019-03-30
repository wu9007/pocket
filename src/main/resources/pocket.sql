/*
 Navicat Premium Data Transfer

 Source Server         : Home
 Source Server Type    : MySQL
 Source Server Version : 80015
 Source Host           : localhost:3306
 Source Schema         : home

 Target Server Type    : MySQL
 Target Server Version : 80015
 File Encoding         : 65001

 Date: 28/03/2019 15:52:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tbl_commodity
-- ----------------------------
DROP TABLE IF EXISTS `tbl_commodity`;
CREATE TABLE `tbl_commodity`  (
  `uuid` int(32) NOT NULL,
  `name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `price` decimal(10, 2) NULL DEFAULT 0.00,
  `order_uuid` int(32) NULL DEFAULT NULL,
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tbl_commodity
-- ----------------------------
INSERT INTO `tbl_commodity` VALUES (1, '苹果', 7.60, 11);
INSERT INTO `tbl_commodity` VALUES (2, '饼干', 5.00, 22);
INSERT INTO `tbl_commodity` VALUES (3, '苹果', 7.60, 11);
INSERT INTO `tbl_commodity` VALUES (4, '饼干', 5.00, 11);
INSERT INTO `tbl_commodity` VALUES (5, '苹果', 7.60, 12);
INSERT INTO `tbl_commodity` VALUES (6, '饼干', 5.00, 12);

-- ----------------------------
-- Table structure for tbl_order
-- ----------------------------
DROP TABLE IF EXISTS `tbl_order`;
CREATE TABLE `tbl_order`  (
  `uuid` int(32) NOT NULL,
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `price` decimal(10, 2) NULL DEFAULT NULL,
  `day` date NULL DEFAULT NULL,
  `time` datetime(0) NULL DEFAULT NULL,
  `state` int(1) NULL DEFAULT NULL,
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tbl_order
-- ----------------------------
INSERT INTO `tbl_order` VALUES (1, 'A-001', 32.50, '2019-01-13', '2019-01-18 11:35:03', NULL);
INSERT INTO `tbl_order` VALUES (2, 'A-002', 12.60, '2019-01-14', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES (3, 'C-001', 500.50, '2019-03-28', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES (4, 'C-001', 500.50, '2019-03-28', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES (5, 'C-001', 500.50, '2019-03-28', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES (6, 'C-001', 500.50, '2019-03-28', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES (7, 'C-001', 500.50, '2019-03-28', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES (8, 'C-001', 500.50, '2019-03-28', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES (9, 'C-001', 500.50, '2019-03-28', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES (10, 'C-001', 500.50, '2019-03-28', '2019-01-15 13:53:12', NULL);
INSERT INTO `tbl_order` VALUES (11, 'C-007', 661.30, '2019-01-21', '2019-01-21 14:27:39', NULL);
INSERT INTO `tbl_order` VALUES (12, 'C-001', 500.50, '2019-03-28', '2019-01-15 13:59:05', NULL);
INSERT INTO `tbl_order` VALUES (1011010, 'C-001', 500.50, '2019-03-28', '2019-03-28 15:51:48', 0);
INSERT INTO `tbl_order` VALUES (1011011, 'C-001', 500.50, '2019-03-28', '2019-03-28 15:51:48', NULL);

SET FOREIGN_KEY_CHECKS = 1;
