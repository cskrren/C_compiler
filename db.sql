CREATE DATABASE /*!32312 IF NOT EXISTS*/ `c_compiler` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `c_compiler`;

--
-- Table structure for table `system_tree`
--

DROP TABLE IF EXISTS `system_tree`;
CREATE TABLE `system_tree` (
  `id` int(11) NOT NULL,
  `label` varchar(100) NOT NULL,
  `father_id` int(11) NOT NULL,
  `type` varchar(20) NOT NULL,
  `file_content` varchar(5000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `system_tree`
--

LOCK TABLES `system_tree` WRITE;
INSERT INTO `system_tree` VALUES (1,'good',0,'directory',''),(3,'right1.cpp',1,'file','int program(int a,int b,int c)\n{\n    return a+b+c;\n}\n\nint main()\n{\n    int a;\n    int b;\n    int c;\n    a = 1;\n    b = 3;\n    c = 5; \n    // additional code\n    a=program(a,b,c);\n    return 0;\n}\n\n'),(5,'wrong',0,'directory',''),(6,'wrong1.cpp',5,'file','/de\nint program(int a,int b,int c)\n{\n    return a+b+c;\n}\n\nint main()\n{\n    int a;\n    int b;\n    int c;\n    a = 1;\n    b = 3;\n    c = 5; \n    // additional code\n    a=program(a,b,c);\n    return 0;\n}\n\n'),(10,'right2.cpp',1,'file','int program(int a,int b,int c)\n{\n    int i;\n    int j;\n    i=0;  \n    if(a>(b+c))\n    {\n        j=a+(b*c+1);\n    }\n    else\n    {\n        j=a;\n    }\n    while(i<=100)\n    {\n        i=j*2;\n        j=i;\n    }\n    return i;\n}\n\nint demo(int a)\n{\n    a=a+2;\n    return a*2;\n}\n\nint main()\n{\n    int a[2][2];\n    a[0][0]=3;\n    a[0][1]=a[0][0]+1;\n    a[1][0]=a[0][0]+a[0][1];\n    a[1][1]=program(a[0][0],a[0][1],demo(a[1][0]));\n    return 0;\n}\n');
UNLOCK TABLES;
