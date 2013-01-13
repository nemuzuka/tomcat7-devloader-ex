tomcat7-devloader-ex
===================

Tomcat DevLoader with extra features.

Overview
--------

Sysdeo Eclipse Tomcat Launcher plugin v3.3 でtomcat7を使用する際のDevLoader拡張です。
既存のDevLoader7(http://svn.codehaus.org/tynamo/trunk/tomcat-sysdeo-devloader/)に、
・除外jar設定
・別のEclipseプロジェクトを参照している際に被参照プロジェクトのpathが記述されないことへの対応
を実施しています。

Build
-----

You can execute it using the command below:

<pre>
mvn package
</pre>

Installation
------------

Delete default DevLoader class files.

Copy devloader7-x.x.x.jar to $TOMCAT_HOME/lib
Copy conf/referencePath.conf to $TOMCAT_HOME/conf/referencePath.conf

Configurations
-------------------------
[workspace dir],[class dir]

<pre>
# for examples
C:/eclipse/workspace,/target/classes
</pre>

References
----------
* Windows8(64bit) + Eclipse3.7 + Sysdeo Eclipse Tomcat Launcher plugin 3.3 + Tomcat7での確認

