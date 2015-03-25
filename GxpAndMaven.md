# Using GXP with Maven #

GXP is available in the Maven central repository:

http://repo2.maven.org/maven2/com/google/gxp/google-gxp/0.2.4-beta/

```
      <plugin>
        <artifactId>maven-antrun-plugin</artifactId>
        <executions>
          <execution>
            <phase>generate-sources</phase>
            <configuration>
              <tasks>
                  <taskdef name="gxpc" classname="com.google.gxp.compiler.ant.GxpcTask" classpathref="maven.compile.classpath" />
                  <gxpc srcdir="${basedir}\src\main\java" srcpaths="${basedir}\src\main\java" destdir="${basedir}\target\generated-sources" target="com.xxxss.s2.example.message" i18nwarn="false" />
                  <copy todir="${basedir}\src\main\java">
                        <fileset dir="${basedir}\target\generated-sources" />
                  </copy>
              </tasks>
            </configuration>
            <goals>
              <goal>run</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
```