package com.pesterenan.utils;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public class VersionUtil {
  public static String getVersion() {
    String version = "N/A";
    MavenXpp3Reader reader = new MavenXpp3Reader();
    Model model;
    try {
      if ((new File("pom.xml")).exists()) {
        model = reader.read(new FileReader("pom.xml"));
      } else {
        model =
            reader.read(
                new InputStreamReader(
                    VersionUtil.class.getResourceAsStream(
                        "/META-INF/maven/com.pesterenan/MechPeste/pom.xml")));
      }
      version = model.getVersion();
      return version;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return version;
  }
}
