package com.pesterenan.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class VersionUtil {
    public static String getVersion() {
        String version = "N/A";
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader("pom.xml"));
            version = model.getVersion();
            return version;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return version;
    }
}
