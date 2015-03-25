# Using GXP with Ant #

The exact configuration of your build file will depend on your particular setup, but this should give you an idea of the steps involved:

  1. [Download the GXP jar](http://code.google.com/p/gxp/downloads/list) and make sure it is included in your classpath.
  1. Define a task def like so:
```
<taskdef name="gxpc" classname="com.google.gxp.compiler.ant.GxpcTask"
         classpath="path/to/gxp-0.2.2-beta.jar" />
```
  1. Compile your sourcs GXPs using this task.  This will generate a .java file for each .gxp as well as a .properties file for internationalized text.
```
<gxpc srcdir="${src.dir}"
      srcpaths="${src.dir}"
      destdir="${genfiles.dir}"
      target="com.yourdomain.yourproject.messages" />
```
  1. Copy the properties file to your classdir
```
<copy todir="${build.dir}/classes">
  <fileset dir="${src.dir}/">
    <include name="com/yourdomain/yourproject/messages_en.properties" />
  </fileset>
</copy>
```
  1. When compiling the rest of your code, include the java generated from gxp:
```
<javac ...>
  <src>
    <pathelement location="${genfiles.dir}/" />
  </src>
</javac>
```