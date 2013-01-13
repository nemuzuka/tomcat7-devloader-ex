tomcat7-devloader-ex
===================

Tomcat DevLoader with extra features.

Overview
--------
Sysdeo Eclipse Tomcat Launcher plugin v3.3 でtomcat7を使用する際のDevLoader拡張です。
既存のDevLoader7(http://svn.codehaus.org/tynamo/trunk/tomcat-sysdeo-devloader/)に、
* 除外jar設定
* 別のEclipseプロジェクトを参照している際に被参照プロジェクトのpathが記述されないことへの対応
を実施しています。

Build
-----

You can execute it using the command below:

<pre>
mvn package
</pre>

Installation
------------
* Copy devloader7-x.x.x.jar to $TOMCAT_HOME/lib
* Copy conf/referencePath.conf to $TOMCAT_HOME/conf/referencePath.conf

Configurations
-------------------------
referencePath.confの指定内容は、
[参照プロジェクトまでのpath,プロジェクトの出力ディレクトリのpath]

例えば、参照プロジェクト(app-common)が、
C:/eclipse/workspace/project
ディレクトリに配置されていて、出力ディレクトリがEclipse上で
/target/classes
と設定されている場合、referencePath.confには、
<pre>C:/eclipse/workspace/project,/target/classes</pre>
と記述します。

Loadする際に、
C:/eclipse/workspace/project/app-common/target/classes
をクラスパスに追加します。


References
----------
* Windows8(64bit) + Eclipse3.7 + Sysdeo Eclipse Tomcat Launcher plugin 3.3 + Tomcat7での確認
* Mac OS X Mountain Lion + Eclipse4.2 + Sysdeo Eclipse Tomcat Launcher plugin 3.3 + Tomcat7での確認
