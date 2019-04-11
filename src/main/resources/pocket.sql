/*
 Navicat Premium Data Transfer

 Source Server         : 104
 Source Server Type    : MySQL
 Source Server Version : 50725
 Source Host           : 172.0.66.104:3306
 Source Schema         : lsphunter

 Target Server Type    : MySQL
 Target Server Version : 50725
 File Encoding         : 65001

 Date: 11/04/2019 14:12:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tbl_commodity
-- ----------------------------
DROP TABLE IF EXISTS `tbl_commodity`;
CREATE TABLE `tbl_commodity`  (
  `uuid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `price` decimal(10, 2) NULL DEFAULT 0.00,
  `order_uuid` int(32) NULL DEFAULT NULL,
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tbl_commodity
-- ----------------------------
INSERT INTO `tbl_commodity` VALUES ('1', '苹果', 7.60, 11);
INSERT INTO `tbl_commodity` VALUES ('2', '饼干', 5.00, 22);
INSERT INTO `tbl_commodity` VALUES ('3', '苹果', 7.60, 11);
INSERT INTO `tbl_commodity` VALUES ('4', '饼干', 5.00, 11);
INSERT INTO `tbl_commodity` VALUES ('5', '苹果', 7.60, 12);
INSERT INTO `tbl_commodity` VALUES ('6', '饼干', 5.00, 12);

-- ----------------------------
-- Table structure for tbl_order
-- ----------------------------
DROP TABLE IF EXISTS `tbl_order`;
CREATE TABLE `tbl_order`  (
  `uuid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
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
INSERT INTO `tbl_order` VALUES ('1', 'A-001', 32.50, '2019-01-13', '2019-01-18 11:35:03', NULL);
INSERT INTO `tbl_order` VALUES ('10', 'C-001', 500.50, '2019-04-11', '2019-01-15 13:53:12', NULL);
INSERT INTO `tbl_order` VALUES ('1019999980', 'C-001', 500.50, '2019-04-11', '2019-03-25 16:20:02', 0);
INSERT INTO `tbl_order` VALUES ('1019999981', 'C-001', 500.50, '2019-04-11', '2019-03-25 16:20:02', NULL);
INSERT INTO `tbl_order` VALUES ('10199999810', 'C-001', 500.50, '2019-04-11', '2019-04-11 11:30:48', 0);
INSERT INTO `tbl_order` VALUES ('10199999811', 'C-001', 500.50, '2019-04-11', '2019-04-11 11:31:02', NULL);
INSERT INTO `tbl_order` VALUES ('10199999812', 'C-001', 500.50, '2019-04-11', '2019-04-11 11:35:29', 0);
INSERT INTO `tbl_order` VALUES ('10199999813', 'C-001', 500.50, '2019-04-11', '2019-04-11 11:35:30', NULL);
INSERT INTO `tbl_order` VALUES ('10199999814', 'C-001', 500.50, '2019-04-11', '2019-04-11 11:39:35', 0);
INSERT INTO `tbl_order` VALUES ('10199999815', 'C-001', 500.50, '2019-04-11', '2019-04-11 11:39:35', NULL);
INSERT INTO `tbl_order` VALUES ('1019999982', 'C-001', 500.50, '2019-04-11', '2019-03-25 16:23:54', 0);
INSERT INTO `tbl_order` VALUES ('1019999983', 'C-001', 500.50, '2019-04-11', '2019-03-25 16:23:55', NULL);
INSERT INTO `tbl_order` VALUES ('1019999984', 'C-001', 500.50, '2019-04-11', '2019-03-25 16:25:39', 0);
INSERT INTO `tbl_order` VALUES ('1019999985', 'C-001', 500.50, '2019-04-11', '2019-03-25 16:25:39', NULL);
INSERT INTO `tbl_order` VALUES ('1019999986', 'C-001', 500.50, '2019-04-11', '2019-04-11 11:25:27', 0);
INSERT INTO `tbl_order` VALUES ('1019999987', 'C-001', 500.50, '2019-04-11', '2019-04-11 11:25:27', NULL);
INSERT INTO `tbl_order` VALUES ('1019999988', 'C-001', 500.50, '2019-04-11', '2019-04-11 11:26:30', 0);
INSERT INTO `tbl_order` VALUES ('1019999989', 'C-001', 500.50, '2019-04-11', '2019-04-11 11:26:30', NULL);
INSERT INTO `tbl_order` VALUES ('11', 'C-007', 822.10, '2019-01-21', '2019-01-21 14:27:39', NULL);
INSERT INTO `tbl_order` VALUES ('12', 'C-001', 500.50, '2019-04-11', '2019-01-15 13:59:05', NULL);
INSERT INTO `tbl_order` VALUES ('2', 'A-002', 12.60, '2019-01-14', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES ('3', 'C-001', 500.50, '2019-04-11', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES ('4', 'C-001', 500.50, '2019-04-11', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES ('5', 'C-001', 500.50, '2019-04-11', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES ('6', 'C-001', 500.50, '2019-04-11', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES ('7', 'C-001', 500.50, '2019-04-11', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES ('8', 'C-001', 500.50, '2019-04-11', '2019-01-15 11:05:08', NULL);
INSERT INTO `tbl_order` VALUES ('9', 'C-001', 500.50, '2019-04-11', '2019-01-15 11:05:08', NULL);

SET FOREIGN_KEY_CHECKS = 1;
