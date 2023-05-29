# C_compiler

Author: 任柯睿 (2053635)

## Introduction

This project is a class C compiler implemented in Java. It consists of a Spring Boot backend and a Vue.js frontend. The compiler is designed to translate C programming language code into executable machine code. The project also utilizes MySQL database for storing and retrieving data.

## Features

1. C Code Compilation: The compiler can take class C code as input and perform lexical analysis, syntax analysis, semantic analysis, optimization, and target code generation, resulting in executable assembly code.
2. Error Handling: The compiler can detect and handle various errors in the input class C code, such as lexical errors, syntax errors, and semantic errors. Detailed error messages are provided to assist the user in debugging.
3. Integrated Development Environment (IDE): The project provides a user-friendly IDE where users can write and edit C code. It includes features such as code modification, copy-paste, TAB, file upload, and file download.
4. Display of Compilation Intermediate Results: During the compilation of class C code, intermediate code is saved and displayed in the frontend.
5. Database Integration: The project uses MySQL database to store and retrieve data related to the compiled programs. This includes class C code content, file tree logic, and file attributes.

## Show

![image-20230529201454182](.\asset\show1.png)

![image-20230529202024178](.\asset\show2.png)

![image-20230529202039245](.\asset\show3.png)

## Configuration

The project requires the following dependencies:

- Java Development Kit (JDK) 1.8
- Spring Boot 2.3.7.RELEASE
- Vue 2 
- MySQL

To set up the project, follow these steps:

### 1. 执行前端部分，端口号8080(前提没被占用)

```
cd online_ide
npm install
npm run build
npm run dev
```
！！！注意，在npm install或npm run build步骤中报以下错误不会影响最后项目的执行，忽略即可。 
![image-20230529200759749](.\asset\front-out.png)

### 2. 配置数据库

导入db.sql文件到数据库中。

```
source ./db.sql
```

！！！注意，由于包含中文因此可能会出现编码问题（显示乱码，但不影响前后端执行）

### 3. 执行后端部分，端口号8081(前提没被占用)

(1) 打开IDEA，导入项目，点击MAVEN窗口的Reload All Maven Project。

![image-20230529200147676](.\asset\back-out.png)

(2) 修改src/main/resources/application.properties文件中数据库设置。

![image-20230529210857684](.\asset\back-out2.png)

(3) 找到src/main/java/com/example/c_compiler/CCompilerApplication.java，点击绿色三角执行代码。

![image-20230529200430409](.\asset\back-out3.png)

(4) 成功执行后端项目。

![image-20230529200505559](.\asset\back-out4.png)

！！！注意，在配置好后端项目前，不要打开前端端口对应url，会由于后端未执行而中断。

### 4. 在浏览器上打开localhost:8080，即可访问。

！！！注意，如果遇到如下问题，即文件树显示错误，这是因为数据库传值慢于前端渲染，刷新网页即可。

<img src=".\asset\web.png" alt="image-20230529201231018" style="zoom:67%;" />




## Conclusion

The class C compiler project offers a comprehensive solution for  compiling C code. With its user-friendly integrated development  environment (IDE), error handling, compilation, and optimization  features, it assists developers in writing and compiling C programs. The integration with MySQL database enables storage, retrieval, uploading,  and downloading of relevant program data.
