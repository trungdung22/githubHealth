# Java Project GitRepo Health stats

This project goal is to determine current health stats of Git repository.
Max Health round from 0 to 1 based on: 
- Number of contributors (who commit) of repo. 
- Number of commit per day. 
- Number of commit ratio per Dev.
- Average  Duration of open issue. 
- Average duration of pull request need to merge. 

## RUN
- Use maven to install all dependencies in pom.xml 
- Sample run cmd from intelliJ:
"C:\Program Files\Java\jdk-13\bin\java.exe" 
"-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA 2019.2.3\lib\idea_rt.jar=59701:C:\Program Files\JetBrains\IntelliJ IDEA 2019.2.3\bin" 
-Dfile.encoding=UTF-8 
-classpath "C:\Users\Dung Do\IdeaProjects\intellji\target\classes;C:\Users\Dung Do\.m2\repository\org\apache\httpcomponents\httpclient\4.5.9\httpclient-4.5.9.jar
;C:\Users\Dung Do\.m2\repository\org\apache\httpcomponents\httpcore\4.4.11\httpcore-4.4.11.jar;C:\Users\Dung Do\.m2\repository\commons-logging\commons-logging\1.2\commons-logging-1.2.jar
;C:\Users\Dung Do\.m2\repository\commons-codec\commons-codec\1.11\commons-codec-1.11.jar;C:\Users\Dung Do\.m2\repository\org\apache\commons\commons-lang3\3.0\commons-lang3-3.0.jar
;C:\Users\Dung Do\.m2\repository\org\apache\commons\commons-text\1.6\commons-text-1.6.jar;C:\Users\Dung Do\.m2\repository\org\json\json\20190722\json-20190722.jar
;C:\Users\Dung Do\.m2\repository\org\apache\commons\commons-collections4\4.0\commons-collections4-4.0.jar" 
com.quod.Application 2019-09-01T01:00:00Z 2019-09-10T04:00:00Z

## Dependencies
 - apache commons-lang : use for data manipulation etc: String, number, date, collections etc. 
 - json-simple: use for parsing string to json objects. 
 - java-net lib: lib support for http request, streaming and memory buffer. 

## Open points & discussion: 
 - Multi-threading and concurrency / big data frameword need to be implement to speed up urls download, manipulate big collections/list.
 - Implement Generic Mapping to support different kind of json structure.   
 - More repo attributes type.
 - Imprve some hard code/doc. 
 - etc.